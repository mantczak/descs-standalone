package edu.put.ma.descs;

import static edu.put.ma.utils.StringUtils.NEW_LINE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.Getter;

import org.apache.commons.collections4.CollectionUtils;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.SVDSuperimposer;
import org.biojava.nbio.structure.StructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.put.ma.descs.algorithms.AlignmentAcceptanceMode;
import edu.put.ma.descs.algorithms.CommonAlgorithm;
import edu.put.ma.descs.algorithms.ComparisonAlgorithm;
import edu.put.ma.descs.contacts.AtomsStorage;
import edu.put.ma.descs.contacts.AtomsStorageFactory;
import edu.put.ma.descs.contacts.ExpressionValidatorImpl;
import edu.put.ma.model.AlignedDuplexesPair;
import edu.put.ma.model.Alignment;
import edu.put.ma.model.AlignmentFactory;
import edu.put.ma.model.ComparisonResult;
import edu.put.ma.model.ComparisonResultFactory;
import edu.put.ma.model.DescriptorsPair;
import edu.put.ma.model.ExtendedAlignment;
import edu.put.ma.model.ExtendedAlignmentFactory;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.model.Residue;
import edu.put.ma.model.ResiduesDictionary;
import edu.put.ma.model.RmsdModel;
import edu.put.ma.utils.PreconditionUtils;
import edu.put.ma.utils.ResidueUtils;

public class DescriptorsComparatorImpl implements DescriptorsComparator {

    private static final boolean INSERT_NON_ALIGNED_RESIDUES_OF_SECOND_DESCRIPTOR = true;

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorsComparatorImpl.class);

    private final ComparisonAlgorithm comparisonAlgorithm;

    @Getter
    private final SimilarDescriptorsVerifier similarDescriptorsVerifier;

    private final AtomsStorage firstDescriptorAtomsStorage;

    private final AtomsStorage secondDescriptorAtomsStorage;

    private final ImmutableList<String> alignmentAtomNames;

    @Getter
    private final double maximalRmsdThresholdPerDuplexPair;

    private final AlignmentAcceptanceMode alignmentAcceptanceMode;

    @Getter
    private Map<Integer, List<AlignedDuplexesPair>> duplexPairsSimilarityContainer;

    public DescriptorsComparatorImpl(final ComparisonAlgorithm comparisonAlgorithm,
            final SimilarDescriptorsVerifier similarDescriptorsVerifier,
            final double maximalRmsdThresholdPerDuplexPair, final ImmutableList<String> alignmentAtomNames,
            final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        this.comparisonAlgorithm = comparisonAlgorithm;
        this.similarDescriptorsVerifier = similarDescriptorsVerifier;
        this.maximalRmsdThresholdPerDuplexPair = maximalRmsdThresholdPerDuplexPair;
        this.alignmentAtomNames = alignmentAtomNames;
        this.alignmentAcceptanceMode = alignmentAcceptanceMode;
        this.firstDescriptorAtomsStorage = AtomsStorageFactory.construct();
        this.secondDescriptorAtomsStorage = AtomsStorageFactory.construct();
        this.duplexPairsSimilarityContainer = Maps.newHashMap();
    }

    @Override
    public ComparisonResult compare(final DescriptorsPair descriptorsPair) {
        firstDescriptorAtomsStorage.prepareStorage();
        secondDescriptorAtomsStorage.prepareStorage();
        ComparisonResult comparisonResult = null;
        if (descriptorsPair.areSingleElementDescriptors()) {
            comparisonResult = compareOriginElementsOfDescriptors(descriptorsPair);
        } else {
            final ExtendedAlignment extendedOriginElementsAlignment = getOriginElementsAlignment(descriptorsPair);
            if (extendedOriginElementsAlignment != null) {
                computeDuplexPairsSimilarityMatrix(descriptorsPair,
                        extendedOriginElementsAlignment.getCurrentAlignment());
                LOGGER.info(getDuplexPairsString());
                if (canBeStructurallySimilar(descriptorsPair)) {
                    final ExtendedAlignment alignment = comparisonAlgorithm.extendAlignment(this,
                            descriptorsPair, extendedOriginElementsAlignment, alignmentAcceptanceMode);
                    LOGGER.info(alignment.getAlignedDuplexesPairsString());
                    final String sequenceAlignment = computeSequenceAlignment(descriptorsPair, alignment,
                            INSERT_NON_ALIGNED_RESIDUES_OF_SECOND_DESCRIPTOR);
                    comparisonResult = alignment.getComparisonResult();
                    comparisonResult.setSequenceAlignment(sequenceAlignment);
                }
            }
        }
        return comparisonResult;
    }

    @Override
    public String getDuplexPairsString() {
        final StringBuilder sb = new StringBuilder("Duplex pairs structural similarity matrix:\n");
        sb.append(duplexPairsSimilarityContainer.toString());
        return sb.toString();
    }

    @Override
    public int getAlignmentAtomsCount() {
        return CollectionUtils.size(alignmentAtomNames);
    }

    @Override
    public Alignment constructExtension(final DescriptorsPair descriptorsPair,
            final Alignment currentAlignment, final List<AlignedDuplexesPair> alignedDuplexPairs,
            final ComparisonPrecision precision) {
        final int alignedDuplexPairsCount = CollectionUtils.size(alignedDuplexPairs);
        if (alignedDuplexPairsCount > 1) {
            return constructExtensionBasedOnAlignedDuplexPairs(descriptorsPair, currentAlignment,
                    alignedDuplexPairs);
        } else {
            final MoleculeType moleculeType = descriptorsPair.getMoleculeType();
            final List<Group> newResiduesForFirstDescriptor = Lists.newArrayList();
            final List<Group> newResiduesForSecondDescriptor = Lists.newArrayList();
            constructExtensionBasedOnAlignedDuplexesPair(descriptorsPair, currentAlignment,
                    alignedDuplexPairs, newResiduesForFirstDescriptor, newResiduesForSecondDescriptor);
            final boolean rmsdShouldBeConsidered = ComparisonPrecision.ALL_RULES_CONSIDERED
                    .atLeast(precision);
            if (rmsdShouldBeConsidered) {
                final List<Atom> newAtomsForFirstDescriptor = Lists.newArrayList();
                final List<Atom> newAtomsForSecondDescriptor = Lists.newArrayList();
                final List<Atom> firstDescriptorNewAtoms = getAlignmentAtomsOfResidues(moleculeType,
                        alignmentAtomNames, firstDescriptorAtomsStorage, newResiduesForFirstDescriptor);
                final List<Atom> secondDescriptorNewAtoms = getAlignmentAtomsOfResidues(moleculeType,
                        alignmentAtomNames, secondDescriptorAtomsStorage, newResiduesForSecondDescriptor);
                if (alignedDuplexPairsCount > 1) {
                    CollectionUtils.addAll(newAtomsForFirstDescriptor, firstDescriptorNewAtoms);
                    CollectionUtils.addAll(newAtomsForSecondDescriptor, secondDescriptorNewAtoms);
                } else {
                    CollectionUtils.addAll(newAtomsForFirstDescriptor,
                            currentAlignment.identifyNewAtomsForFirstDescriptor(firstDescriptorNewAtoms));
                    CollectionUtils.addAll(newAtomsForSecondDescriptor,
                            currentAlignment.identifyNewAtomsForSecondDescriptor(secondDescriptorNewAtoms));
                }
                return AlignmentFactory.construct(newResiduesForFirstDescriptor, newAtomsForFirstDescriptor,
                        newResiduesForSecondDescriptor, newAtomsForSecondDescriptor);
            } else {
                final List<Atom> empty = edu.put.ma.utils.CollectionUtils.emptyList();
                return AlignmentFactory.construct(newResiduesForFirstDescriptor, empty,
                        newResiduesForSecondDescriptor, empty);
            }
        }
    }

    @Override
    public Alignment constructExtension(final DescriptorsPair descriptorsPair,
            final Alignment currentAlignment, final AlignedDuplexesPair alignedDuplexesPair,
            final ComparisonPrecision precision) {
        return constructExtension(descriptorsPair, currentAlignment,
                Arrays.asList(new AlignedDuplexesPair[] { alignedDuplexesPair }), precision);
    }

    public static final void rotateAndShiftAtom(final Atom atom, final SVDSuperimposer superimposer) {
        Calc.rotate(atom, superimposer.getRotation());
        Calc.shift(atom, superimposer.getTranslation());
    }

    public static final RmsdModel computeAlignmentRmsd(final List<Atom> targetAtoms,
            final List<Atom> otherAtoms) {
        return computeAlignmentRmsd(targetAtoms, otherAtoms, false);
    }

    public static final double computeAlignmentRmsdOnly(final List<Atom> targetAtoms,
            final List<Atom> otherAtoms) {
        double result = Double.MAX_VALUE;
        final RmsdModel rmsdModel = computeAlignmentRmsd(targetAtoms, otherAtoms, true);
        if (rmsdModel != null) {
            result = rmsdModel.getAlignmentRmsd();
        }
        return result;
    }

    public static final boolean canBeStructurallySimilar(final DescriptorsPair descriptorsPair,
            final int achieveableElementsCount, final SimilarDescriptorsVerifier similarDescriptorsVerifier) {
        final double promisingElementsRatio = CommonAlgorithm.getRatio(achieveableElementsCount,
                descriptorsPair.getFirstDescriptorElementsCount(),
                descriptorsPair.getSecondDescriptorElementsCount());
        return similarDescriptorsVerifier.isMinimalAlignedElementsRatioAchieved(promisingElementsRatio);
    }

    private boolean canBeStructurallySimilar(final DescriptorsPair descriptorsPair) {
        final int promisingFirstDescriptorOtherElementsCount = CollectionUtils
                .size(duplexPairsSimilarityContainer) + 1;
        return canBeStructurallySimilar(descriptorsPair, promisingFirstDescriptorOtherElementsCount,
                similarDescriptorsVerifier);
    }

    private void constructExtensionBasedOnAlignedDuplexesPair(final DescriptorsPair descriptorsPair,
            final Alignment currentAlignment, final List<AlignedDuplexesPair> alignedDuplexPairs,
            final List<Group> newResiduesForFirstDescriptor, final List<Group> newResiduesForSecondDescriptor) {
        final AlignedDuplexesPair alignedDuplexesPair = alignedDuplexPairs.get(0);
        final int firstDescriptorOtherElementIndex = alignedDuplexesPair
                .getFirstDescriptorOtherElementIndex();
        final List<Group> firstDescriptorOtherElementResidues = descriptorsPair
                .getFirstDescriptorOtherElementResiduesByIndex(firstDescriptorOtherElementIndex);
        CollectionUtils.addAll(newResiduesForFirstDescriptor,
                currentAlignment.identifyNewResiduesForFirstDescriptor(firstDescriptorOtherElementResidues));
        final int secondDescriptorOtherElementIndex = alignedDuplexesPair
                .getSecondDescriptorOtherElementIndex();
        final List<Group> secondDescriptorOtherElementResidues = descriptorsPair
                .getSecondDescriptorOtherElementResiduesByIndex(secondDescriptorOtherElementIndex);
        CollectionUtils
                .addAll(newResiduesForSecondDescriptor, currentAlignment
                        .identifyNewResiduesForSecondDescriptor(secondDescriptorOtherElementResidues));
        final int firstDescriptorNewResiduesCount = org.apache.commons.collections4.CollectionUtils
                .size(newResiduesForFirstDescriptor);
        final int secondDescriptorNewResiduesCount = org.apache.commons.collections4.CollectionUtils
                .size(newResiduesForSecondDescriptor);
        if (firstDescriptorNewResiduesCount != secondDescriptorNewResiduesCount) {
            unifyResidues(firstDescriptorOtherElementResidues, newResiduesForFirstDescriptor,
                    secondDescriptorOtherElementResidues, newResiduesForSecondDescriptor, currentAlignment);
        }
    }

    private Alignment constructExtensionBasedOnAlignedDuplexPairs(final DescriptorsPair descriptorsPair,
            final Alignment currentAlignment, final List<AlignedDuplexesPair> alignedDuplexPairs) {
        final List<AlignedDuplexesPair> alignedDPs = Lists.newArrayList(alignedDuplexPairs);
        for (AlignedDuplexesPair alignedDuplexesPair : alignedDPs) {
            final Alignment extension = this.constructExtension(descriptorsPair, currentAlignment,
                    alignedDuplexesPair, ComparisonPrecision.ALL_RULES_CONSIDERED);
            if (extension.getAlignedResiduesCount() > 0) {
                currentAlignment.extend(extension, this.getAlignmentAtomsCount(),
                        descriptorsPair.getMoleculeType());
            } else {
                alignedDuplexPairs.remove(alignedDuplexesPair);
            }
        }
        return currentAlignment;
    }

    private void computeDuplexPairsSimilarityMatrix(final DescriptorsPair descriptorsPair,
            final Alignment originElementsAlignment) {
        final int firstDescriptorOtherElementsCount = descriptorsPair.getFirstDescriptorElementsCount() - 1;
        final int secondDescriptorOtherElementsCount = descriptorsPair.getSecondDescriptorElementsCount() - 1;
        final MoleculeType moleculeType = descriptorsPair.getMoleculeType();
        duplexPairsSimilarityContainer = edu.put.ma.utils.CollectionUtils
                .prepareMap(duplexPairsSimilarityContainer);
        for (int firstDescriptorOtherElementIndex = 0; firstDescriptorOtherElementIndex < firstDescriptorOtherElementsCount; firstDescriptorOtherElementIndex++) {
            final List<Group> firstDescriptorOtherElementResidues = descriptorsPair
                    .getFirstDescriptorOtherElementResiduesByIndex(firstDescriptorOtherElementIndex);
            final List<Group> newResiduesForFirstDescriptor = originElementsAlignment
                    .identifyNewResiduesForFirstDescriptor(firstDescriptorOtherElementResidues);
            final List<Atom> firstDescriptorNewAtoms = getAlignmentAtomsOfResidues(moleculeType,
                    alignmentAtomNames, firstDescriptorAtomsStorage, newResiduesForFirstDescriptor);
            final List<Atom> firstDescriptorExtendedAlignmentAtoms = originElementsAlignment
                    .extendFirstDescriptorAtomsAndReturnAsNewObject(firstDescriptorNewAtoms);
            for (int secondDescriptorOtherElementIndex = 0; secondDescriptorOtherElementIndex < secondDescriptorOtherElementsCount; secondDescriptorOtherElementIndex++) {
                final List<Group> secondDescriptorOtherElementResidues = descriptorsPair
                        .getSecondDescriptorOtherElementResiduesByIndex(secondDescriptorOtherElementIndex);
                final List<Group> newResiduesForSecondDescriptor = originElementsAlignment
                        .identifyNewResiduesForSecondDescriptor(secondDescriptorOtherElementResidues);
                final List<Atom> secondDescriptorNewAtoms = getAlignmentAtomsOfResidues(moleculeType,
                        alignmentAtomNames, secondDescriptorAtomsStorage, newResiduesForSecondDescriptor);
                final List<Atom> secondDescriptorExtendedAlignmentAtoms = originElementsAlignment
                        .extendSecondDescriptorAtomsAndReturnAsNewObject(secondDescriptorNewAtoms);
                final double duplexesPairAlignmentRmsd = computeAlignmentRmsdOnly(
                        firstDescriptorExtendedAlignmentAtoms, secondDescriptorExtendedAlignmentAtoms);
                if (similarDescriptorsVerifier.areDuplexesPairStructurallySimilar(duplexesPairAlignmentRmsd)) {
                    addAlignedDuplexesPair(firstDescriptorOtherElementIndex,
                            secondDescriptorOtherElementIndex,
                            ResidueUtils.ensureCommonDoubleFormat(duplexesPairAlignmentRmsd));
                }
            }
        }
        for (Map.Entry<Integer, List<AlignedDuplexesPair>> entry : duplexPairsSimilarityContainer.entrySet()) {
            Collections.sort(entry.getValue());
        }
    }

    private void addAlignedDuplexesPair(final int firstDescriptorOtherElementIndex,
            final int secondDescriptorOtherElementIndex, final double duplexesPairAlignmentRmsd) {
        if (!duplexPairsSimilarityContainer.containsKey(firstDescriptorOtherElementIndex)) {
            final List<AlignedDuplexesPair> alignedDuplexesPairs = Lists.newArrayList();
            duplexPairsSimilarityContainer.put(firstDescriptorOtherElementIndex, alignedDuplexesPairs);
        }
        duplexPairsSimilarityContainer.get(firstDescriptorOtherElementIndex).add(
                new AlignedDuplexesPair(firstDescriptorOtherElementIndex, secondDescriptorOtherElementIndex,
                        duplexesPairAlignmentRmsd));
    }

    private ExtendedAlignment getOriginElementsAlignment(final DescriptorsPair descriptorsPair) {
        final List<Group> firstDescriptorOriginElementResidues = descriptorsPair
                .getFirstDescriptorOriginElementResidues();
        final List<Group> secondDescriptorOriginElementResidues = descriptorsPair
                .getSecondDescriptorOriginElementResidues();
        PreconditionUtils.checkIfInputListsHaveEqualSizes(firstDescriptorOriginElementResidues,
                secondDescriptorOriginElementResidues,
                "There is no possibility to verify structural similarity of origin elements",
                "residues number");
        final MoleculeType moleculeType = descriptorsPair.getMoleculeType();
        final List<Atom> firstDescriptorAlignmentAtoms = getAlignmentAtomsOfResidues(moleculeType,
                alignmentAtomNames, firstDescriptorAtomsStorage, firstDescriptorOriginElementResidues);
        final List<Atom> secondDescriptorAlignmentAtoms = getAlignmentAtomsOfResidues(moleculeType,
                alignmentAtomNames, secondDescriptorAtomsStorage, secondDescriptorOriginElementResidues);
        final RmsdModel originElementsPairAlignmentRmsdModel = computeAlignmentRmsd(
                firstDescriptorAlignmentAtoms, secondDescriptorAlignmentAtoms);
        final double originElementsPairAlignmentRmsd = originElementsPairAlignmentRmsdModel
                .getAlignmentRmsd();
        final boolean areOriginElementsPairStructurallySimilar = similarDescriptorsVerifier
                .areOriginElementsPairStructurallySimilar(originElementsPairAlignmentRmsd);
        ExtendedAlignment extendedOriginElementsAlignment = null;
        if (areOriginElementsPairStructurallySimilar) {
            final Alignment originElementsAlignment = AlignmentFactory.construct(
                    firstDescriptorOriginElementResidues, firstDescriptorAlignmentAtoms,
                    secondDescriptorOriginElementResidues, secondDescriptorAlignmentAtoms);
            final double alignedElementsRatio = CommonAlgorithm.getRatio(1,
                    descriptorsPair.getFirstDescriptorElementsCount(),
                    descriptorsPair.getSecondDescriptorElementsCount());
            final double alignedResiduesRatio = CommonAlgorithm.getRatio(
                    originElementsAlignment.getAlignedResiduesCount(),
                    descriptorsPair.getFirstDescriptorResiduesCount(),
                    descriptorsPair.getSecondDescriptorResiduesCount());
            final ComparisonResult comparisonResult = ComparisonResultFactory.construct(
                    similarDescriptorsVerifier, originElementsPairAlignmentRmsd,
                    originElementsPairAlignmentRmsdModel.getSuperimposer(), alignedElementsRatio,
                    alignedResiduesRatio);
            extendedOriginElementsAlignment = ExtendedAlignmentFactory.construct(originElementsAlignment,
                    comparisonResult);
        }
        return extendedOriginElementsAlignment;
    }

    private ComparisonResult compareOriginElementsOfDescriptors(final DescriptorsPair descriptorsPair) {
        ComparisonResult comparisonResult = null;
        final List<Group> firstDescriptorOriginElementResidues = descriptorsPair
                .getFirstDescriptorOriginElementResidues();
        final List<Group> secondDescriptorOriginElementResidues = descriptorsPair
                .getSecondDescriptorOriginElementResidues();
        PreconditionUtils.checkIfInputListsHaveEqualSizes(firstDescriptorOriginElementResidues,
                secondDescriptorOriginElementResidues,
                "There is no possibility to verify structural similarity of origin elements",
                "residues number");
        final MoleculeType moleculeType = descriptorsPair.getMoleculeType();
        final String sequenceAlignment = getSequence(firstDescriptorOriginElementResidues, moleculeType)
                + NEW_LINE + getSequence(secondDescriptorOriginElementResidues, moleculeType);
        final List<Atom> firstDescriptorAlignmentAtoms = getAlignmentAtomsOfResidues(moleculeType,
                alignmentAtomNames, firstDescriptorAtomsStorage, firstDescriptorOriginElementResidues);
        final List<Atom> secondDescriptorAlignmentAtoms = getAlignmentAtomsOfResidues(moleculeType,
                alignmentAtomNames, secondDescriptorAtomsStorage, secondDescriptorOriginElementResidues);
        comparisonResult = compareOriginElementsOfDescriptors(firstDescriptorAlignmentAtoms,
                secondDescriptorAlignmentAtoms, similarDescriptorsVerifier, sequenceAlignment);
        return comparisonResult;
    }

    private static final void unifyResidues(final List<Group> firstDescriptorOtherElementResidues,
            final List<Group> newResiduesForFirstDescriptor,
            final List<Group> secondDescriptorOtherElementResidues,
            final List<Group> newResiduesForSecondDescriptor, final Alignment currentAlignment) {
        newResiduesForFirstDescriptor.clear();
        newResiduesForSecondDescriptor.clear();
        for (int residueIndex = 0; residueIndex < CollectionUtils.size(firstDescriptorOtherElementResidues); residueIndex++) {
            final Group firstDescriptorOtherElementResidue = firstDescriptorOtherElementResidues
                    .get(residueIndex);
            final Group secondDescriptorOtherElementResidue = secondDescriptorOtherElementResidues
                    .get(residueIndex);
            if ((!currentAlignment.isResidueCoveredByFirstDescriptor(firstDescriptorOtherElementResidue))
                    && (!currentAlignment
                            .isResidueCoveredBySecondDescriptor(secondDescriptorOtherElementResidue))) {
                newResiduesForFirstDescriptor.add(0, firstDescriptorOtherElementResidue);
                newResiduesForSecondDescriptor.add(0, secondDescriptorOtherElementResidue);
            }
        }
    }

    private static final String getSequence(final List<Group> residues, final MoleculeType moleculeType) {
        final StringBuilder sequenceBuilder = new StringBuilder();
        for (Group residue : residues) {
            final Residue residueEntry = ResiduesDictionary.getResidueEntry(residue.getPDBName(),
                    moleculeType);
            sequenceBuilder.append(residueEntry.getSingleLetterCode());
        }
        return sequenceBuilder.toString();
    }

    private static final RmsdModel computeAlignmentRmsd(final List<Atom> targetAtoms,
            final List<Atom> otherAtoms, final boolean returnRmsdOnly) {
        final int targetAtomsCount = CollectionUtils.size(targetAtoms);
        final int otherAtomsCount = CollectionUtils.size(otherAtoms);
        RmsdModel result = null;
        if (targetAtomsCount == otherAtomsCount) {
            final List<Atom> otherAtomsCopy = copyAlignmentAtoms(otherAtoms);
            final Atom[] targetAtomsArray = edu.put.ma.utils.CollectionUtils.toArray(targetAtoms,
                    new Atom[targetAtomsCount]);
            final Atom[] otherAtomsArray = edu.put.ma.utils.CollectionUtils.toArray(otherAtomsCopy,
                    new Atom[otherAtomsCount]);
            final SVDSuperimposer superimposer = superimpose(targetAtomsArray, otherAtomsArray);
            final double alignmentRmsd = computeRmsd(targetAtomsArray, otherAtomsArray, superimposer);
            if (returnRmsdOnly) {
                result = new RmsdModel(alignmentRmsd);
            } else {
                result = new RmsdModel(alignmentRmsd, superimposer);
            }
        }
        return result;
    }

    private static final ComparisonResult compareOriginElementsOfDescriptors(
            final List<Atom> firstDescriptorAlignmentAtoms, final List<Atom> secondDescriptorAlignmentAtoms,
            final SimilarDescriptorsVerifier similarDescriptorsVerifier, final String sequenceAlignment) {
        PreconditionUtils.checkIfInputListsHaveEqualSizes(firstDescriptorAlignmentAtoms,
                secondDescriptorAlignmentAtoms, "There is no possibility to superimpose atom sets", "size");
        final List<Atom> secondDescriptorAlignmentAtomsCopy = copyAlignmentAtoms(secondDescriptorAlignmentAtoms);
        final Atom[] firstDescriptorAlignmentAtomsArray = edu.put.ma.utils.CollectionUtils.toArray(
                firstDescriptorAlignmentAtoms, new Atom[CollectionUtils.size(firstDescriptorAlignmentAtoms)]);
        final Atom[] secondDescriptorAlignmentAtomsArray = edu.put.ma.utils.CollectionUtils.toArray(
                secondDescriptorAlignmentAtomsCopy,
                new Atom[CollectionUtils.size(secondDescriptorAlignmentAtoms)]);
        final SVDSuperimposer superimposer = superimpose(firstDescriptorAlignmentAtomsArray,
                secondDescriptorAlignmentAtomsArray);
        final double originElementsPairAlignmentRmsd = computeRmsd(firstDescriptorAlignmentAtomsArray,
                secondDescriptorAlignmentAtomsArray, superimposer);
        return ComparisonResultFactory.construct(similarDescriptorsVerifier, originElementsPairAlignmentRmsd,
                superimposer, sequenceAlignment);
    }

    private static final List<Atom> getAlignmentAtomsOfResidues(final MoleculeType moleculeType,
            final ImmutableList<String> alignmentAtomNames, final AtomsStorage atomsStorage,
            final List<Group> residues) {
        final Pattern virtualAtomNamesPattern = ExpressionValidatorImpl
                .getVirtualAtomNamesPattern(moleculeType);
        final int consideredAtomsCount = CollectionUtils.size(alignmentAtomNames);
        final int residuesCount = CollectionUtils.size(residues);
        final int alignmentAtomsNo = consideredAtomsCount * residuesCount;
        final List<Atom> alignmentAtomsOfResidues = Lists.newArrayListWithExpectedSize(alignmentAtomsNo);
        for (Group residue : residues) {
            final Residue residueEntry = ResiduesDictionary.getResidueEntry(residue.getPDBName(),
                    moleculeType);
            for (String atomName : alignmentAtomNames) {
                final Atom alignmentAtom = atomsStorage.getAtom(residue, residueEntry, atomName,
                        virtualAtomNamesPattern);
                if (alignmentAtom != null) {
                    alignmentAtomsOfResidues.add(alignmentAtom);
                }
            }
        }
        return alignmentAtomsOfResidues;
    }

    private static final double computeRmsd(final Atom[] firstAtomsArray, final Atom[] secondAtomsArray,
            final SVDSuperimposer superimposer) {
        rotateAndShiftAtomsArray(secondAtomsArray, superimposer);
        try {
            return SVDSuperimposer.getRMS(firstAtomsArray, secondAtomsArray);
        } catch (StructureException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Double.MAX_VALUE;
    }

    private static final void rotateAndShiftAtomsArray(final Atom[] atomsArray,
            final SVDSuperimposer superimposer) {
        for (Atom atom : atomsArray) {
            rotateAndShiftAtom(atom, superimposer);
            ResidueUtils.formatCoordinates(atom);
        }
    }

    private static final List<Atom> copyAlignmentAtoms(final List<Atom> alignmentAtoms) {
        final List<Atom> alignmentAtomsCopy = edu.put.ma.utils.CollectionUtils
                .constructEmptyListWithCapacity(alignmentAtoms);
        for (Atom atom : alignmentAtoms) {
            alignmentAtomsCopy.add((Atom) atom.clone());
        }
        return alignmentAtomsCopy;
    }

    private static final SVDSuperimposer superimpose(final Atom[] firstAtomsArray,
            final Atom[] secondAtomsArray) {
        SVDSuperimposer superimposer = null;
        try {
            superimposer = new SVDSuperimposer(firstAtomsArray, secondAtomsArray);
        } catch (StructureException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return superimposer;
    }

    private static final String computeSequenceAlignment(final DescriptorsPair descriptorsPair,
            final ExtendedAlignment currentAlignment, final boolean insertNonAlignedResiduesOfSecondDescriptor) {
        final List<Group> firstDescriptorResidues = descriptorsPair.getFirstDescriptorResidues();
        final List<Group> secondDescriptorResidues = descriptorsPair.getSecondDescriptorResidues();
        final List<Group> firstDescriptorAlignedResidues = currentAlignment.getCurrentAlignment()
                .getFirstDescriptorResidues();
        final List<Group> secondDescriptorAlignedResidues = currentAlignment.getCurrentAlignment()
                .getSecondDescriptorResidues();
        final StringBuilder firstDescriptorAlignment = new StringBuilder();
        final StringBuilder secondDescriptorAlignment = new StringBuilder();
        final Map<Group, Integer> secondDescriptorAlignmentMap = computeAlignmentBasedOnFirstDescriptor(
                descriptorsPair.getMoleculeType(), firstDescriptorResidues, firstDescriptorAlignedResidues,
                secondDescriptorAlignedResidues, firstDescriptorAlignment, secondDescriptorAlignment);
        if (insertNonAlignedResiduesOfSecondDescriptor) {
            insertNonAlignedResiduesOfSecondDescriptor(descriptorsPair.getMoleculeType(),
                    secondDescriptorResidues, secondDescriptorAlignedResidues, secondDescriptorAlignmentMap,
                    firstDescriptorAlignment, secondDescriptorAlignment);
        }
        return new StringBuilder(firstDescriptorAlignment.toString()).append(NEW_LINE)
                .append(secondDescriptorAlignment.toString()).toString();
    }

    private static final void insertNonAlignedResiduesOfSecondDescriptor(final MoleculeType moleculeType,
            final List<Group> secondDescriptorResidues, final List<Group> secondDescriptorAlignedResidues,
            final Map<Group, Integer> secondDescriptorAlignmentMap,
            final StringBuilder firstDescriptorAlignment, final StringBuilder secondDescriptorAlignment) {
        final int secondDescriptorResiduesCount = CollectionUtils.size(secondDescriptorResidues);
        int nonAlignedResiduesBeginning = 0;
        int nonAlignedResiduesEnd = 0;
        for (int residueIndex = 0; residueIndex < secondDescriptorResiduesCount; residueIndex++) {
            final Group residue = secondDescriptorResidues.get(residueIndex);
            if (secondDescriptorAlignedResidues.contains(residue)) {
                if (nonAlignedResiduesBeginning != nonAlignedResiduesEnd) {
                    final int nonAlignedResiduesCount = nonAlignedResiduesEnd - nonAlignedResiduesBeginning;
                    final int alignedResidueIndex = secondDescriptorAlignmentMap.get(residue);
                    extendAlignmentWithNonAlignedResidues(moleculeType, secondDescriptorResidues,
                            firstDescriptorAlignment, secondDescriptorAlignment, nonAlignedResiduesBeginning,
                            nonAlignedResiduesEnd, alignedResidueIndex);
                    updateSecondDescriptorAlignmentMap(secondDescriptorAlignmentMap, nonAlignedResiduesCount,
                            alignedResidueIndex);
                }
                nonAlignedResiduesBeginning = nonAlignedResiduesEnd = residueIndex + 1;
            } else {
                nonAlignedResiduesEnd++;
            }
        }
        if (nonAlignedResiduesBeginning != nonAlignedResiduesEnd) {
            for (int nonAlignedResidueIndex = nonAlignedResiduesBeginning; nonAlignedResidueIndex < nonAlignedResiduesEnd; nonAlignedResidueIndex++) {
                extendAlignmentBySingleResidue(moleculeType,
                        secondDescriptorResidues.get(nonAlignedResidueIndex), firstDescriptorAlignment,
                        secondDescriptorAlignment, -1);
            }
        }
    }

    private static final void extendAlignmentWithNonAlignedResidues(final MoleculeType moleculeType,
            final List<Group> secondDescriptorResidues, final StringBuilder firstDescriptorAlignment,
            final StringBuilder secondDescriptorAlignment, int nonAlignedResiduesBeginning,
            int nonAlignedResiduesEnd, final int alignedResidueIndex) {
        for (int nonAlignedResidueIndex = nonAlignedResiduesEnd - 1; nonAlignedResidueIndex >= nonAlignedResiduesBeginning; nonAlignedResidueIndex--) {
            extendAlignmentBySingleResidue(moleculeType,
                    secondDescriptorResidues.get(nonAlignedResidueIndex), firstDescriptorAlignment,
                    secondDescriptorAlignment, alignedResidueIndex);
        }
    }

    private static final void updateSecondDescriptorAlignmentMap(
            final Map<Group, Integer> secondDescriptorAlignmentMap, final int nonAlignedResiduesCount,
            final int alignedResidueIndex) {
        for (Map.Entry<Group, Integer> entry : secondDescriptorAlignmentMap.entrySet()) {
            if (entry.getValue() >= alignedResidueIndex) {
                entry.setValue(entry.getValue() + nonAlignedResiduesCount);
            }
        }
    }

    private static final void extendAlignmentBySingleResidue(final MoleculeType moleculeType,
            final Group nonAlignedResidue, final StringBuilder firstDescriptorAlignment,
            final StringBuilder secondDescriptorAlignment, final int alignedResidueIndex) {
        final Residue nonAlignedResidueEntry = ResiduesDictionary.getResidueEntry(
                nonAlignedResidue.getPDBName(), moleculeType);
        if (alignedResidueIndex >= 0) {
            firstDescriptorAlignment.insert(alignedResidueIndex, '.');
            secondDescriptorAlignment.insert(alignedResidueIndex,
                    nonAlignedResidueEntry.getSingleLetterCode());
        } else {
            firstDescriptorAlignment.append('.');
            secondDescriptorAlignment.append(nonAlignedResidueEntry.getSingleLetterCode());
        }
    }

    private static final Map<Group, Integer> computeAlignmentBasedOnFirstDescriptor(
            final MoleculeType moleculeType, final List<Group> firstDescriptorResidues,
            final List<Group> firstDescriptorAlignedResidues,
            final List<Group> secondDescriptorAlignedResidues, final StringBuilder firstDescriptorAlignment,
            final StringBuilder secondDescriptorAlignment) {
        final Map<Group, Integer> secondDescriptorAlignmentMap = Maps.newHashMap();
        int alignmentIndex = 0;
        for (Group firstDescriptorResidue : firstDescriptorResidues) {
            final Residue firstDescriptorResidueEntry = ResiduesDictionary.getResidueEntry(
                    firstDescriptorResidue.getPDBName(), moleculeType);
            firstDescriptorAlignment.append(firstDescriptorResidueEntry.getSingleLetterCode());
            if (firstDescriptorAlignedResidues.contains(firstDescriptorResidue)) {
                final int firstDescriptorResidueIndex = firstDescriptorAlignedResidues
                        .indexOf(firstDescriptorResidue);
                final Group secondDescriptorResidue = secondDescriptorAlignedResidues
                        .get(firstDescriptorResidueIndex);
                final Residue secondDescriptorResidueEntry = ResiduesDictionary.getResidueEntry(
                        secondDescriptorResidue.getPDBName(), moleculeType);
                secondDescriptorAlignment.append(secondDescriptorResidueEntry.getSingleLetterCode());
                secondDescriptorAlignmentMap.put(secondDescriptorResidue, alignmentIndex);
            } else {
                secondDescriptorAlignment.append('.');
            }
            alignmentIndex++;
        }
        return secondDescriptorAlignmentMap;
    }
}

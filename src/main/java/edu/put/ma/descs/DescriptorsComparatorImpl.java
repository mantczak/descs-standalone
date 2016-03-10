package edu.put.ma.descs;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorsComparatorImpl.class);

    private final ComparisonAlgorithm comparisonAlgorithm;

    @Getter
    private final SimilarDescriptorsVerifier similarDescriptorsVerifier;

    private final AtomsStorage firstDescriptorAtomsStorage;

    private final AtomsStorage secondDescriptorAtomsStorage;

    private final ImmutableList<String> alignmentAtomNames;

    @Getter
    private final double maximalRmsdThresholdPerDuplexPair;

    @Getter
    private Map<Integer, List<AlignedDuplexesPair>> duplexPairsSimilarityContainer;

    public DescriptorsComparatorImpl(final ComparisonAlgorithm comparisonAlgorithm,
            final SimilarDescriptorsVerifier similarDescriptorsVerifier,
            final double maximalRmsdThresholdPerDuplexPair, final ImmutableList<String> alignmentAtomNames) {
        this.comparisonAlgorithm = comparisonAlgorithm;
        this.similarDescriptorsVerifier = similarDescriptorsVerifier;
        this.maximalRmsdThresholdPerDuplexPair = maximalRmsdThresholdPerDuplexPair;
        this.alignmentAtomNames = alignmentAtomNames;
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
                            descriptorsPair, extendedOriginElementsAlignment);
                    LOGGER.info(alignment.getAlignedDuplexesPairsString());
                    comparisonResult = alignment.getComparisonResult();
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
        final Alignment newAlignment = null;
        final MoleculeType moleculeType = descriptorsPair.getMoleculeType();
        final int alignedDuplexPairsCount = CollectionUtils.size(alignedDuplexPairs);
        final List<Group> newResiduesForFirstDescriptor = Lists.newArrayList();
        final List<Group> newResiduesForSecondDescriptor = Lists.newArrayList();
        if (alignedDuplexPairsCount > 1) {
            constructExtensionBasedOnAlignedDuplexPairs(descriptorsPair, currentAlignment,
                    alignedDuplexPairs, newResiduesForFirstDescriptor, newResiduesForSecondDescriptor);
        } else {
            constructExtensionBasedOnAlignedDuplexesPair(descriptorsPair, currentAlignment,
                    alignedDuplexPairs, newResiduesForFirstDescriptor, newResiduesForSecondDescriptor);
        }
        final int newResiduesForFirstDescriptorCount = CollectionUtils.size(newResiduesForFirstDescriptor);
        final int newResiduesForSecondDescriptorCount = CollectionUtils.size(newResiduesForSecondDescriptor);
        if (newResiduesForFirstDescriptorCount > 0 && newResiduesForSecondDescriptorCount > 0
                && newResiduesForFirstDescriptorCount == newResiduesForSecondDescriptorCount) {
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
                if (CollectionUtils.size(newAtomsForFirstDescriptor) == CollectionUtils
                        .size(newAtomsForSecondDescriptor)) {
                    return AlignmentFactory.construct(newResiduesForFirstDescriptor,
                            newAtomsForFirstDescriptor, newResiduesForSecondDescriptor,
                            newAtomsForSecondDescriptor);
                }
            } else {
                final List<Atom> empty = edu.put.ma.utils.CollectionUtils.emptyList();
                return AlignmentFactory.construct(newResiduesForFirstDescriptor, empty,
                        newResiduesForSecondDescriptor, empty);
            }

        }
        return newAlignment;
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
    }

    private void constructExtensionBasedOnAlignedDuplexPairs(final DescriptorsPair descriptorsPair,
            final Alignment currentAlignment, final List<AlignedDuplexesPair> alignedDuplexPairs,
            final List<Group> allResiduesForFirstDescriptor, final List<Group> allResiduesForSecondDescriptor) {
        CollectionUtils.addAll(allResiduesForFirstDescriptor, currentAlignment.getFirstDescriptorResidues());
        CollectionUtils
                .addAll(allResiduesForSecondDescriptor, currentAlignment.getSecondDescriptorResidues());
        for (AlignedDuplexesPair alignedDuplexesPair : alignedDuplexPairs) {
            final int firstDescriptorOtherElementIndex = alignedDuplexesPair
                    .getFirstDescriptorOtherElementIndex();
            final List<Group> firstDescriptorOtherElementResidues = descriptorsPair
                    .getFirstDescriptorOtherElementResiduesByIndex(firstDescriptorOtherElementIndex);
            final List<Group> newResiduesForFirstDescriptorOfOtherElement = edu.put.ma.utils.CollectionUtils
                    .identifyNewElements(allResiduesForFirstDescriptor, firstDescriptorOtherElementResidues);
            allResiduesForFirstDescriptor.addAll(newResiduesForFirstDescriptorOfOtherElement);
            final int secondDescriptorOtherElementIndex = alignedDuplexesPair
                    .getSecondDescriptorOtherElementIndex();
            final List<Group> secondDescriptorOtherElementResidues = descriptorsPair
                    .getSecondDescriptorOtherElementResiduesByIndex(secondDescriptorOtherElementIndex);
            final List<Group> newResiduesForSecondDescriptorOfOtherElement = edu.put.ma.utils.CollectionUtils
                    .identifyNewElements(allResiduesForSecondDescriptor, secondDescriptorOtherElementResidues);
            allResiduesForSecondDescriptor.addAll(newResiduesForSecondDescriptorOfOtherElement);
        }
        final Comparator<Group> comparator = new Comparator<Group>() {
            public int compare(final Group firstResidue, final Group secondResidue) {
                return firstResidue.getResidueNumber().compareTo(secondResidue.getResidueNumber());
            }
        };
        Collections.sort(allResiduesForFirstDescriptor, comparator);
        Collections.sort(allResiduesForSecondDescriptor, comparator);
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
        final List<Atom> firstDescriptorAlignmentAtoms = getAlignmentAtomsOfResidues(moleculeType,
                alignmentAtomNames, firstDescriptorAtomsStorage, firstDescriptorOriginElementResidues);
        final List<Atom> secondDescriptorAlignmentAtoms = getAlignmentAtomsOfResidues(moleculeType,
                alignmentAtomNames, secondDescriptorAtomsStorage, secondDescriptorOriginElementResidues);
        comparisonResult = compareOriginElementsOfDescriptors(firstDescriptorAlignmentAtoms,
                secondDescriptorAlignmentAtoms, similarDescriptorsVerifier);
        return comparisonResult;
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
            final SimilarDescriptorsVerifier similarDescriptorsVerifier) {
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
                superimposer);
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
}

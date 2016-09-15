package edu.put.ma.model;

import static edu.put.ma.descs.DescriptorsBuilderImpl.DEFAULT_NEIBHOURHOOD_SZE;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

import org.apache.commons.collections4.CollectionUtils;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.SVDSuperimposer;
import org.biojava.nbio.structure.Structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.put.ma.access.ResiduesAccess;
import edu.put.ma.access.ResiduesAccessFactory;
import edu.put.ma.descs.DescriptorResidueType;
import edu.put.ma.descs.DescriptorsComparatorImpl;
import edu.put.ma.descs.UnappropriateDescriptorException;
import edu.put.ma.gaps.GapsDistribution;
import edu.put.ma.structure.StructureExtension;
import edu.put.ma.utils.PreconditionUtils;
import edu.put.ma.utils.ResidueUtils;

public class DescriptorImpl implements Descriptor {

    private static final boolean NOT_CONSIDER_ORIGIN_RESIDUE_INDEX = false;

    private static final boolean CONSIDER_ORIGIN_RESIDUE_INDEX = true;

    @Getter
    private final int segmentsCount;

    private final ImmutableList<Integer> elementsCenters;

    @Getter
    private final int elementsCount;

    private final ImmutableList<Integer> inContactResidues;

    @Getter
    private final int residuesCount;

    @Getter
    private final String id;

    private int originResidueIndex;

    @Getter
    private int neighbourhoodSize;

    @Getter
    private boolean valid;

    @Getter
    private Structure structure;

    private String position;

    private String sequence;

    public DescriptorImpl(final StructureExtension moleculeStructure, final int modelIndex,
            final ModelProperties modelProperties, final int originResidueIndex,
            final List<Integer> elementsCenters, final List<Integer> inContactResidues)
            throws UnappropriateDescriptorException {
        this.originResidueIndex = originResidueIndex;
        this.elementsCenters = ImmutableList.copyOf(elementsCenters);
        this.elementsCount = CollectionUtils.size(elementsCenters);
        this.inContactResidues = ImmutableList.copyOf(inContactResidues);
        this.residuesCount = CollectionUtils.size(inContactResidues);
        this.neighbourhoodSize = modelProperties.getGapsDistribution().getNeibhourhoodSize();
        this.id = build3d(moleculeStructure, modelIndex, modelProperties);
        this.segmentsCount = computeSegmentsCount(moleculeStructure.getMoleculeType());
    }

    public DescriptorImpl(final StructureExtension descriptorStructure,
            final ResiduesAccess descriptorResiduesAccess, final boolean copyStructure)
            throws UnappropriateDescriptorException {
        this.residuesCount = descriptorResiduesAccess.getResiduesAccessIndexesSize();
        this.id = descriptorStructure.getInputFileBasename();
        final List<Integer> descriptorElementCenters = setUpDescriptorStructure(descriptorStructure,
                descriptorResiduesAccess, copyStructure);
        this.elementsCenters = ImmutableList.copyOf(descriptorElementCenters);
        this.elementsCount = CollectionUtils.size(elementsCenters);
        this.inContactResidues = ImmutableList.copyOf(descriptorResiduesAccess.getResiduesAccessIndexes());
        this.segmentsCount = computeSegmentsCount(descriptorStructure.getMoleculeType());
        this.valid = validateStructure(descriptorStructure.getMoleculeType(), id);
    }

    @Override
    public String toString() {
        return toString(CONSIDER_ORIGIN_RESIDUE_INDEX);
    }

    @Override
    public String toStringWithoutOriginResidueIndex() {
        return toString(NOT_CONSIDER_ORIGIN_RESIDUE_INDEX);
    }

    @Override
    public List<Group> getResidues() {
        final List<Group> residues = Lists.newArrayList();
        final List<Chain> model = structure.getModel(0);
        for (Chain chain : model) {
            for (Group currentResidue : chain.getAtomGroups()) {
                residues.add(currentResidue);
            }
        }
        return Collections.unmodifiableList(residues);
    }

    @Override
    public List<Group> getOriginElementResidues(final ResiduesAccess residuesAccess) {
        return residuesAccess.getElementResiduesByCenterIndex(originResidueIndex, structure.getModel(0),
                neighbourhoodSize);
    }

    @Override
    public void rotateAndShift(final SVDSuperimposer superimposer) {
        final List<Chain> model = structure.getModel(0);
        for (Chain chain : model) {
            for (Group residue : chain.getAtomGroups()) {
                for (org.biojava.nbio.structure.Atom atom : residue.getAtoms()) {
                    DescriptorsComparatorImpl.rotateAndShiftAtom(atom, superimposer);
                }
            }
        }
    }

    @Override
    public List<Group> getOtherElementResiduesByIndex(final int otherElementIndex,
            final ResiduesAccess residuesAccess) {
        final int originResidueElementCentersIndex = elementsCenters.indexOf(this.originResidueIndex);
        final int otherElementCentersIndex = (otherElementIndex >= originResidueElementCentersIndex) ? otherElementIndex + 1
                : otherElementIndex;
        PreconditionUtils.checkIfIndexInRange(otherElementCentersIndex, 0, elementsCount, "Other element");
        return residuesAccess.getElementResiduesByCenterIndex(elementsCenters.get(otherElementCentersIndex),
                structure.getModel(0), neighbourhoodSize);
    }

    public static final Descriptor constructDescriptor(final StructureExtension extendedStructure,
            final ResiduesAccess residuesAccess, final boolean copyStructure)
            throws UnappropriateDescriptorException {
        final Descriptor descriptor = new DescriptorImpl(extendedStructure, residuesAccess, copyStructure);
        if (!descriptor.isValid()) {
            throw new UnappropriateDescriptorException(String.format("%s descriptor is invalid",
                    descriptor.getId()));
        }
        return descriptor;
    }

    private String toString(final boolean considerOriginResidueIndex) {
        final StringBuilder result = new StringBuilder();
        if (considerOriginResidueIndex) {
            result.append(String.valueOf(originResidueIndex + 1)).append("\t");
        }
        result.append(id).append("\t").append(segmentsCount).append("\t").append(elementsCount).append("\t")
                .append(residuesCount).append("\t").append(position).append("\t").append(sequence);
        return result.toString();
    }

    private List<Integer> setUpDescriptorStructure(final StructureExtension descriptorStructure,
            final ResiduesAccess descriptorResiduesAccess, final boolean copyStructure)
            throws UnappropriateDescriptorException {
        final List<Integer> descriptorElementCenters = Lists.newArrayList();
        final int modelIndex = 0;
        final List<Chain> model = descriptorStructure.getModelByIndex(modelIndex);
        if (copyStructure) {
            structure = descriptorStructure.cloneModelWithoutResiduesByIndex(modelIndex);
        } else {
            structure = descriptorStructure.getStructure();
        }
        int originResiduesCount = 0;
        for (int residueIndex = 0; residueIndex < residuesCount; residueIndex++) {
            final int chainIndex = descriptorResiduesAccess.getChainIndex(residueIndex);
            final Group residue = descriptorResiduesAccess.cloneResidueByIndex(residueIndex, model);
            if (residue != null) {
                originResiduesCount = setElementCenter(copyStructure, descriptorElementCenters,
                        originResiduesCount, residueIndex, chainIndex, residue);
            }
        }
        if (originResiduesCount == 0) {
            throw new UnappropriateDescriptorException(String.format(
                    "There is no origin residue - exactly one should be defined in descriptor %s", id));
        } else if (originResiduesCount > 1) {
            throw new UnappropriateDescriptorException(String.format(
                    "There are too many origin residues [%d] in descriptor %s - only one should be defined",
                    originResiduesCount, id));
        }
        return descriptorElementCenters;
    }

    private int setElementCenter(final boolean copyStructure, final List<Integer> descriptorElementCenters,
            final int originResiduesCount, final int residueIndex, final int chainIndex, final Group residue)
            throws UnappropriateDescriptorException {
        int newOriginResiduesCount = originResiduesCount;
        final org.biojava.nbio.structure.Atom firstAtom = residue.getAtom(0);
        final DescriptorResidueType descriptorResidueType = DescriptorResidueType.fromFlag(firstAtom
                .getTempFactor());
        if ((descriptorResidueType == DescriptorResidueType.ORIGIN_CENTER)
                || (descriptorResidueType == DescriptorResidueType.OTHER_CENTER)) {
            descriptorElementCenters.add(residueIndex);
        }
        if (descriptorResidueType == DescriptorResidueType.ORIGIN_CENTER) {
            this.originResidueIndex = residueIndex;
            this.neighbourhoodSize = (int) firstAtom.getOccupancy();
            if (this.neighbourhoodSize <= 0) {
                throw new UnappropriateDescriptorException(String.format(
                        "Illegal neighbourhood size in descriptor %s - should be positive number", id));
            }
            newOriginResiduesCount++;
        }
        if (copyStructure) {
            structure.getChain(chainIndex).addGroup(residue);
        }
        return newOriginResiduesCount;
    }

    private String build3d(final StructureExtension extendedStructure, final int modelIndex,
            final ModelProperties modelProperties) throws UnappropriateDescriptorException {
        final int realModelIndex = Math.max(0, modelIndex);
        structure = extendedStructure.cloneModelWithoutResiduesByIndex(realModelIndex);
        final List<Chain> model = extendedStructure.getModelByIndex(realModelIndex);
        final int inContactResiduesCount = CollectionUtils.size(inContactResidues);
        String residueKey = null;
        int atomNo = 1;
        int coveredResiduesCount = 0;
        final ResiduesAccess residuesAccess = modelProperties.getResiduesAccess();
        final GapsDistribution gapsDistribution = modelProperties.getGapsDistribution();
        for (int residueIndex = 0; residueIndex < inContactResiduesCount; residueIndex++) {
            final int inContactResidueIndex = inContactResidues.get(residueIndex).intValue();
            final int chainIndex = residuesAccess.getChainIndex(inContactResidueIndex);
            final Group residue = residuesAccess.cloneResidueByIndex(inContactResidueIndex, model);
            if (residue != null) {
                atomNo = processResidueAtoms(atomNo, gapsDistribution, inContactResidueIndex, residue);
                if (originResidueIndex == inContactResidueIndex) {
                    residueKey = ResidueUtils.getResidueKey(residue);
                }
                structure.getChain(chainIndex).addGroup(residue);
                coveredResiduesCount++;
            }
        }
        final String descriptorId = getDescriptorId(extendedStructure, modelIndex, residueKey);
        this.valid = (inContactResiduesCount == coveredResiduesCount)
                && validateStructure(extendedStructure.getMoleculeType(), descriptorId);
        return descriptorId;
    }

    private int processResidueAtoms(final int atomNo, final GapsDistribution gapsDistribution,
            final int inContactResidueIndex, final Group residue) {
        int currentAtomNo = atomNo;
        for (org.biojava.nbio.structure.Atom atom : residue.getAtoms()) {
            if (elementsCenters.contains(inContactResidueIndex)) {
                atom.setTempFactor((originResidueIndex == inContactResidueIndex) ? DescriptorResidueType.ORIGIN_CENTER
                        .getFlag() : DescriptorResidueType.OTHER_CENTER.getFlag());
                if (originResidueIndex == inContactResidueIndex) {
                    atom.setOccupancy(gapsDistribution.getNeibhourhoodSize());
                }
            }
            atom.setPDBserial(currentAtomNo++);
        }
        return currentAtomNo;
    }

    private boolean validateStructure(final MoleculeType moleculeType, final String descId)
            throws UnappropriateDescriptorException {
        final List<Chain> model = structure.getModel(0);
        final ResiduesAccess residuesAccess = ResiduesAccessFactory.construct(model);
        final List<Integer> elementCenters = Lists.newArrayList();
        final int newNeighbourhoodSize = initElementCentersAndReturnNeighbourhoodSize(model, residuesAccess,
                elementCenters);
        final int residuesCountInAllContinousElements = getResiduesCountInAllContinousElements(moleculeType,
                model, residuesAccess, elementCenters, newNeighbourhoodSize, descId);
        if (residuesCount != residuesCountInAllContinousElements) {
            throw new UnappropriateDescriptorException(String.format(
                    "Structure %s contains residues [%s] that are not covered by elements definition",
                    descId, residuesCount - residuesCountInAllContinousElements));
        }
        return true;
    }

    private int computeSegmentsCount(final MoleculeType moleculeType) {
        int result = 1;
        final List<Chain> model = structure.getModel(0);
        Group previousResidue = null;
        final StringBuilder positionBuilder = new StringBuilder();
        final StringBuilder segmentSequenceBuilder = new StringBuilder();
        final StringBuilder sequenceBuilder = new StringBuilder();
        for (Chain chain : model) {
            for (Group currentResidue : chain.getAtomGroups()) {
                if ((currentResidue != null)
                        && (previousResidue != null)
                        && (!ResidueUtils.areResiduesConnected(moleculeType, previousResidue, currentResidue))) {
                    result++;
                    updatePosition(positionBuilder, currentResidue, previousResidue);
                    extendSequence(sequenceBuilder, segmentSequenceBuilder);
                    segmentSequenceBuilder.delete(0, segmentSequenceBuilder.length());
                } else if (previousResidue == null) {
                    updatePosition(positionBuilder, currentResidue, previousResidue);
                }
                final Residue residueEntry = ResiduesDictionary.getResidueEntry(currentResidue.getPDBName(),
                        moleculeType);
                segmentSequenceBuilder.append(residueEntry.getSingleLetterCode());
                previousResidue = currentResidue;
            }
        }
        updatePosition(positionBuilder, null, previousResidue);
        position = positionBuilder.toString().replaceAll("_", "");
        extendSequence(sequenceBuilder, segmentSequenceBuilder);
        sequence = sequenceBuilder.toString();
        return result;
    }

    private static final void extendSequence(final StringBuilder sequenceBuilder,
            final StringBuilder segmentSequenceBuilder) {
        if (sequenceBuilder.length() > 0) {
            sequenceBuilder.append(", ");
        }
        sequenceBuilder.append(segmentSequenceBuilder.toString());
    }

    private static final void updatePosition(final StringBuilder positionBuilder, final Group currentResidue,
            final Group previousResidue) {
        if (previousResidue != null) {
            positionBuilder.append("-").append(previousResidue.getResidueNumber().printFull());
            if (currentResidue != null) {
                positionBuilder.append(", ");
            }
        }
        if (currentResidue != null) {
            positionBuilder.append(currentResidue.getResidueNumber().printFull());
        }
    }

    private static final String getDescriptorId(final StructureExtension extendedStructure,
            final int modelIndex, final String residueKey) {
        final StringBuilder idsb = new StringBuilder(extendedStructure.getInputFileBasename());
        if (modelIndex >= 0) {
            idsb.append("_").append(String.valueOf(modelIndex + 1));
        }
        idsb.append("_").append(residueKey);
        return idsb.toString();
    }

    private static final int getResiduesCountInAllContinousElements(final MoleculeType moleculeType,
            final List<Chain> model, final ResiduesAccess residuesAccess, final List<Integer> elementCenters,
            final int neighbourhoodSize, final String descId) throws UnappropriateDescriptorException {
        boolean atLeastOneUncontinousElementOccured = false;
        final List<Integer> coveredResidues = Lists.newArrayList();
        for (Integer elementCenter : elementCenters) {
            final int leftBoundaryIndex = elementCenter.intValue() - neighbourhoodSize;
            final int rightBoundaryIndex = elementCenter.intValue() + neighbourhoodSize;
            final int connectedResiduesCount = getConnectedResiduesCount(moleculeType, model, residuesAccess,
                    leftBoundaryIndex, rightBoundaryIndex);
            if (connectedResiduesCount != (neighbourhoodSize << 1)) {
                atLeastOneUncontinousElementOccured = true;
                break;
            } else {
                insertResiduesOfNewElement(coveredResidues, leftBoundaryIndex, rightBoundaryIndex);
            }
        }
        if (atLeastOneUncontinousElementOccured) {
            throw new UnappropriateDescriptorException(
                    String.format(
                            "Descriptor %s contains at least one uncontinous element! This is prohibited, so will be ignored",
                            descId));
        }
        return CollectionUtils.size(coveredResidues);
    }

    private static final void insertResiduesOfNewElement(final List<Integer> coveredResidues,
            final int leftBoundaryIndex, final int rightBoundaryIndex) {
        for (int residueIndex = leftBoundaryIndex; residueIndex <= rightBoundaryIndex; residueIndex++) {
            if (!coveredResidues.contains(residueIndex)) {
                coveredResidues.add(residueIndex);
            }
        }
    }

    private static final int getConnectedResiduesCount(final MoleculeType moleculeType,
            final List<Chain> model, final ResiduesAccess residuesAccess, final int leftBoundaryIndex,
            final int rightBoundaryIndex) {
        int connectedResiduesCount = 0;
        for (int residueIndex = leftBoundaryIndex + 1; residueIndex <= rightBoundaryIndex; residueIndex++) {
            final Group previousResidue = residuesAccess.getResidueByIndex(residueIndex - 1, model);
            final Group currentResidue = residuesAccess.getResidueByIndex(residueIndex, model);
            if (ResidueUtils.areResiduesConnected(moleculeType, previousResidue, currentResidue)) {
                connectedResiduesCount++;
            }
        }
        return connectedResiduesCount;
    }

    private static final int initElementCentersAndReturnNeighbourhoodSize(final List<Chain> model,
            final ResiduesAccess residuesAccess, final List<Integer> elementCenters) {
        int neighbourhoodSize = DEFAULT_NEIBHOURHOOD_SZE;
        for (int residueIndex = 0; residueIndex < residuesAccess.getResiduesAccessIndexesSize(); residueIndex++) {
            final Group currentResidue = residuesAccess.getResidueByIndex(residueIndex, model);
            final org.biojava.nbio.structure.Atom firstAtom = currentResidue.getAtom(0);
            final DescriptorResidueType descriptorResidueType = DescriptorResidueType.fromFlag(firstAtom
                    .getTempFactor());
            if ((descriptorResidueType == DescriptorResidueType.ORIGIN_CENTER)
                    || (descriptorResidueType == DescriptorResidueType.OTHER_CENTER)) {
                elementCenters.add(residueIndex);
            }
            if (descriptorResidueType == DescriptorResidueType.ORIGIN_CENTER) {
                neighbourhoodSize = (int) firstAtom.getOccupancy();
            }
        }
        return neighbourhoodSize;
    }
}

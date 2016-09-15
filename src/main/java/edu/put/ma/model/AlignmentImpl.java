package edu.put.ma.model;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.commons.math3.stat.StatUtils;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.primitives.Doubles;

import edu.put.ma.descs.DescriptorsComparatorImpl;
import edu.put.ma.utils.CollectionUtils;
import edu.put.ma.utils.ResidueUtils;

public class AlignmentImpl implements Alignment {

    private static final int MAXIMAL_NUMBER_OF_ADJACENT_RESIDUES = 2;

    @Getter
    private final List<Group> firstDescriptorResidues;

    @Getter
    private final List<Atom> firstDescriptorAtoms;

    @Getter
    private final List<Group> secondDescriptorResidues;

    @Getter
    private final List<Atom> secondDescriptorAtoms;

    AlignmentImpl(final List<Group> firstDescriptorResidues, final List<Atom> firstDescriptorAtoms,
            final List<Group> secondDescriptorResidues, final List<Atom> secondDescriptorAtoms) {
        this();
        init(firstDescriptorResidues, firstDescriptorAtoms, secondDescriptorResidues, secondDescriptorAtoms);
    }

    private AlignmentImpl() {
        this.firstDescriptorResidues = Lists.newArrayList();
        this.firstDescriptorAtoms = Lists.newArrayList();
        this.secondDescriptorResidues = Lists.newArrayList();
        this.secondDescriptorAtoms = Lists.newArrayList();
    }

    private AlignmentImpl(final AlignmentImpl alignment) {
        this.firstDescriptorResidues = Lists.newArrayList(alignment.firstDescriptorResidues);
        this.firstDescriptorAtoms = Lists.newArrayList(alignment.firstDescriptorAtoms);
        this.secondDescriptorResidues = Lists.newArrayList(alignment.secondDescriptorResidues);
        this.secondDescriptorAtoms = Lists.newArrayList(alignment.secondDescriptorAtoms);
    }

    @Override
    public Alignment copy() {
        return new AlignmentImpl(this);
    }

    @Override
    public int getAlignedResiduesCount() {
        return org.apache.commons.collections4.CollectionUtils.size(firstDescriptorResidues);
    }

    @Override
    public List<Atom> extendFirstDescriptorAtomsAndReturnAsNewObject(final List<Atom> newAtoms) {
        return CollectionUtils.extendAndReturnAsNewObject(firstDescriptorAtoms, newAtoms);
    }

    @Override
    public List<Atom> extendSecondDescriptorAtomsAndReturnAsNewObject(final List<Atom> newAtoms) {
        return CollectionUtils.extendAndReturnAsNewObject(secondDescriptorAtoms, newAtoms);
    }

    @Override
    public void removeFromFirstDescriptor(final List<Group> firstDescriptorResidues,
            final List<Atom> firstDescriptorAtoms) {
        CollectionUtils.remove(this.firstDescriptorResidues, firstDescriptorResidues);
        CollectionUtils.remove(this.firstDescriptorAtoms, firstDescriptorAtoms);
    }

    @Override
    public void removeFromSecondDescriptor(final List<Group> secondDescriptorResidues,
            final List<Atom> secondDescriptorAtoms) {
        CollectionUtils.remove(this.secondDescriptorResidues, secondDescriptorResidues);
        CollectionUtils.remove(this.secondDescriptorAtoms, secondDescriptorAtoms);
    }

    @Override
    public List<Group> identifyNewResiduesForFirstDescriptor(final List<Group> newResidues) {
        return CollectionUtils.identifyNewElements(firstDescriptorResidues, newResidues);
    }

    @Override
    public List<Group> identifyNewResiduesForSecondDescriptor(final List<Group> newResidues) {
        return CollectionUtils.identifyNewElements(secondDescriptorResidues, newResidues);
    }

    @Override
    public List<Atom> identifyNewAtomsForFirstDescriptor(final List<Atom> newAtoms) {
        return CollectionUtils.identifyNewElements(firstDescriptorAtoms, newAtoms);
    }

    @Override
    public List<Atom> identifyNewAtomsForSecondDescriptor(final List<Atom> newAtoms) {
        return CollectionUtils.identifyNewElements(secondDescriptorAtoms, newAtoms);
    }

    @Override
    public List<Segment> extend(final Alignment extension, final int alignmentAtomsCount,
            final MoleculeType moleculeType) {
        extendPreservingOrder(this, (AlignmentImpl) extension, alignmentAtomsCount);
        return ensureChainConsistency(this, alignmentAtomsCount, moleculeType);
    }

    @Override
    public void setExtension(final Alignment extension) {
        final AlignmentImpl extensionImpl = (AlignmentImpl) extension;
        setFirstDescriptor(extensionImpl.firstDescriptorResidues, extensionImpl.firstDescriptorAtoms);
        setSecondDescriptor(extensionImpl.secondDescriptorResidues, extensionImpl.secondDescriptorAtoms);
    }

    @Override
    public void remove(final Alignment extension) {
        final AlignmentImpl extensionImpl = (AlignmentImpl) extension;
        removeFromFirstDescriptor(extensionImpl.firstDescriptorResidues, extensionImpl.firstDescriptorAtoms);
        removeFromSecondDescriptor(extensionImpl.secondDescriptorResidues,
                extensionImpl.secondDescriptorAtoms);
    }

    @Override
    public RmsdModel computeAlignmentRmsd() {
        return DescriptorsComparatorImpl.computeAlignmentRmsd(firstDescriptorAtoms, secondDescriptorAtoms);
    }

    @Override
    public void revertSegmentStates(final List<Segment> segmentStates) {
        for (Segment segmentState : segmentStates) {
            CollectionUtils.setFromSpecificPosition(secondDescriptorResidues, segmentState.residues,
                    segmentState.startIndexOfResidues);
            CollectionUtils.setFromSpecificPosition(secondDescriptorAtoms, segmentState.atoms,
                    segmentState.startIndexOfAtoms);
        }
    }

    @Override
    public boolean isResidueCoveredByFirstDescriptor(final Group residue) {
        return firstDescriptorResidues.contains(residue);
    }

    @Override
    public boolean isResidueCoveredBySecondDescriptor(final Group residue) {
        return secondDescriptorResidues.contains(residue);
    }

    private void init(final List<Group> firstDescriptorResidues, final List<Atom> firstDescriptorAtoms,
            final List<Group> secondDescriptorResidues, final List<Atom> secondDescriptorAtoms) {
        CollectionUtils.init(this.firstDescriptorResidues, firstDescriptorResidues);
        CollectionUtils.init(this.firstDescriptorAtoms, firstDescriptorAtoms);
        CollectionUtils.init(this.secondDescriptorResidues, secondDescriptorResidues);
        CollectionUtils.init(this.secondDescriptorAtoms, secondDescriptorAtoms);
    }

    private void setFirstDescriptor(final List<Group> firstDescriptorResidues,
            final List<Atom> firstDescriptorAtoms) {
        update(this.firstDescriptorResidues, this.firstDescriptorAtoms, firstDescriptorResidues,
                firstDescriptorAtoms);
    }

    private void setSecondDescriptor(final List<Group> secondDescriptorResidues,
            final List<Atom> secondDescriptorAtoms) {
        update(this.secondDescriptorResidues, this.secondDescriptorAtoms, secondDescriptorResidues,
                secondDescriptorAtoms);
    }

    @RequiredArgsConstructor
    public static class Segment {

        final int startIndexOfResidues;

        final int startIndexOfAtoms;

        final List<Group> residues;

        final List<Atom> atoms;
    }

    private static final void update(final List<Group> currentResidues, final List<Atom> currentAtoms,
            final List<Group> newResidues, final List<Atom> newAtoms) {
        currentResidues.clear();
        org.apache.commons.collections4.CollectionUtils.addAll(currentResidues, newResidues);
        currentAtoms.clear();
        org.apache.commons.collections4.CollectionUtils.addAll(currentAtoms, newAtoms);
    }

    private static final void extendPreservingOrder(final AlignmentImpl current,
            final AlignmentImpl extension, final int alignmentAtomsCount) {
        final int currentResiduesCount = org.apache.commons.collections4.CollectionUtils
                .size(current.firstDescriptorResidues);
        final Group firstNewResidue = extension.firstDescriptorResidues.get(0);
        int residueIndex = 0;
        for (residueIndex = 0; residueIndex < currentResiduesCount; residueIndex++) {
            final Group residueWithHigherResidueNumber = current.firstDescriptorResidues.get(residueIndex);
            if (residueWithHigherResidueNumber.getResidueNumber().compareTo(
                    firstNewResidue.getResidueNumber()) > 0) {
                CollectionUtils.extendFromSpecificPosition(current.firstDescriptorResidues,
                        extension.firstDescriptorResidues, residueIndex);
                CollectionUtils.extendFromSpecificPosition(current.firstDescriptorAtoms,
                        extension.firstDescriptorAtoms, residueIndex * alignmentAtomsCount);
                CollectionUtils.extendFromSpecificPosition(current.secondDescriptorResidues,
                        extension.secondDescriptorResidues, residueIndex);
                CollectionUtils.extendFromSpecificPosition(current.secondDescriptorAtoms,
                        extension.secondDescriptorAtoms, residueIndex * alignmentAtomsCount);
                break;
            }
        }
        if (residueIndex == currentResiduesCount) {
            CollectionUtils.extend(current.firstDescriptorResidues, extension.firstDescriptorResidues);
            CollectionUtils.extend(current.firstDescriptorAtoms, extension.firstDescriptorAtoms);
            CollectionUtils.extend(current.secondDescriptorResidues, extension.secondDescriptorResidues);
            CollectionUtils.extend(current.secondDescriptorAtoms, extension.secondDescriptorAtoms);
        }
    }

    private static final List<Segment> ensureChainConsistency(final AlignmentImpl current,
            final int alignmentAtomsCount, final MoleculeType moleculeType) {
        final List<Segment> segmentStates = Lists.newArrayList();
        final int currentResiduesCount = org.apache.commons.collections4.CollectionUtils
                .size(current.firstDescriptorResidues);
        int currentSegmentStartIndex = 0, currentSegmentEndIndex = 0;
        for (int residueIndex = 1; residueIndex < currentResiduesCount; residueIndex++) {
            final Group previousResidue = current.firstDescriptorResidues.get(residueIndex - 1);
            final Group currentResidue = current.firstDescriptorResidues.get(residueIndex);
            if (!ResidueUtils.areResiduesConnected(moleculeType, previousResidue, currentResidue)) {
                currentSegmentEndIndex = residueIndex - 1;
                if (!isSegmentConsistent(current, currentSegmentStartIndex, currentSegmentEndIndex,
                        moleculeType)) {
                    ensureSegmentConsistency(current, alignmentAtomsCount, moleculeType, segmentStates,
                            currentSegmentStartIndex, currentSegmentEndIndex);
                }
                currentSegmentStartIndex = currentSegmentEndIndex = residueIndex;
            }
        }
        currentSegmentEndIndex = currentResiduesCount - 1;
        if (!isSegmentConsistent(current, currentSegmentStartIndex, currentSegmentEndIndex, moleculeType)) {
            ensureSegmentConsistency(current, alignmentAtomsCount, moleculeType, segmentStates,
                    currentSegmentStartIndex, currentSegmentEndIndex);
        }
        return segmentStates;
    }

    private static final void ensureSegmentConsistency(final AlignmentImpl current,
            final int alignmentAtomsCount, final MoleculeType moleculeType,
            final List<Segment> segmentStates, int currentSegmentStartIndex, int currentSegmentEndIndex) {
        final Segment segment = verifySegmentConsistency(current, alignmentAtomsCount,
                currentSegmentStartIndex, currentSegmentEndIndex, moleculeType);
        if (segment != null) {
            segmentStates.add(segment);
        }
    }

    private static final boolean isSegmentConsistent(final AlignmentImpl current,
            final int currentSegmentStartIndex, final int currentSegmentEndIndex,
            final MoleculeType moleculeType) {
        boolean result = true;
        for (int residueIndex = currentSegmentStartIndex; residueIndex < currentSegmentEndIndex; residueIndex++) {
            final Group currentResidue = current.secondDescriptorResidues.get(residueIndex);
            final Group nextResidue = current.secondDescriptorResidues.get(residueIndex + 1);
            if (!ResidueUtils.areResiduesConnected(moleculeType, currentResidue, nextResidue)) {
                result = false;
                break;
            }
        }
        return result;
    }

    private static final Segment verifySegmentConsistency(final AlignmentImpl current,
            final int alignmentAtomsCount, final int currentSegmentStartIndex,
            final int currentSegmentEndIndex, final MoleculeType moleculeType) {
        final List<Group> previousSegmentResidues = Lists.newArrayList(current.secondDescriptorResidues
                .subList(currentSegmentStartIndex, currentSegmentEndIndex + 1));
        final int segmentResiduesCount = org.apache.commons.collections4.CollectionUtils
                .size(previousSegmentResidues);
        final List<List<Integer>> adjacentResidues = initAdjacentResidues(segmentResiduesCount);
        final List<Integer> residuesDistribution = computeBasicDistributionOfResidues(current, moleculeType,
                segmentResiduesCount, adjacentResidues);
        final List<Integer> newResiduesDistribution = computeDetailedDistributionOfResidues(current,
                moleculeType, segmentResiduesCount, adjacentResidues, residuesDistribution);
        if (!CollectionUtils.equalStringRepresentationsOfLists(residuesDistribution, newResiduesDistribution)) {
            final List<Atom> previousSegmentAtoms = Lists.newArrayList(current.secondDescriptorAtoms.subList(
                    currentSegmentStartIndex * alignmentAtomsCount, (currentSegmentEndIndex + 1)
                            * alignmentAtomsCount));
            final Segment segment = new Segment(currentSegmentStartIndex, currentSegmentStartIndex
                    * alignmentAtomsCount, previousSegmentResidues, previousSegmentAtoms);
            final List<Integer> residueIndexes = Lists.newArrayList(ContiguousSet.create(
                    Range.closed(0, segmentResiduesCount - 1), DiscreteDomain.integers()).asList());
            final List<Integer> nonAdjacentResidues = identifyNonAdjacentResidues(segmentResiduesCount,
                    adjacentResidues, residueIndexes);
            final List<List<Integer>> subsets = computeSubsets(previousSegmentResidues, adjacentResidues,
                    residueIndexes);
            final List<Double> subsetIndexAverages = computeSubsetIndexAverages(subsets);
            final List<Integer> subsetsOrdering = CollectionUtils.sortAndReturnOrdering(subsetIndexAverages);
            CollectionUtils.sortAccordingToOrdering(subsets, subsetsOrdering);
            expandSubsetsWithNonAdjacentResidues(previousSegmentResidues, nonAdjacentResidues, subsets);
            final List<Group> newSegmentResidues = computeNewSegmentResiduesBasedOnSubsets(
                    previousSegmentResidues, segmentResiduesCount, subsets);
            fillResidueIndexes(previousSegmentResidues, residueIndexes, newSegmentResidues);
            final List<Integer> residuesOrdering = CollectionUtils.sortAndReturnOrdering(residueIndexes);
            CollectionUtils.sortAccordingToOrdering(current.secondDescriptorResidues.subList(
                    currentSegmentStartIndex, currentSegmentEndIndex + 1), residuesOrdering);
            orderAtoms(current, alignmentAtomsCount, currentSegmentStartIndex, currentSegmentEndIndex,
                    residuesOrdering);
            return segment;
        }
        return null;
    }

    private static final void fillResidueIndexes(final List<Group> previousSegmentResidues,
            final List<Integer> residueIndexes, final List<Group> newSegmentResidues) {
        for (Group residue : previousSegmentResidues) {
            residueIndexes.add(newSegmentResidues.indexOf(residue));
        }
    }

    private static final List<Group> computeNewSegmentResiduesBasedOnSubsets(
            final List<Group> previousSegmentResidues, final int segmentResiduesCount,
            final List<List<Integer>> subsets) {
        final List<Group> newSegmentResidues = Lists.newArrayListWithCapacity(segmentResiduesCount);
        for (List<Integer> subset : subsets) {
            final List<Group> subsetResidues = Lists
                    .newArrayListWithCapacity(org.apache.commons.collections4.CollectionUtils.size(subset));
            for (Integer residueIndex : subset) {
                subsetResidues.add(previousSegmentResidues.get(residueIndex));
            }
            org.apache.commons.collections4.CollectionUtils.addAll(newSegmentResidues, subsetResidues);
        }
        return newSegmentResidues;
    }

    private static final void expandSubsetsWithNonAdjacentResidues(final List<Group> previousSegmentResidues,
            final List<Integer> nonAdjacentResidues, final List<List<Integer>> subsets) {
        for (int residueIndex : nonAdjacentResidues) {
            int selectedSubsetIndex = -1;
            int subsetIndex = 0;
            for (List<Integer> subset : subsets) {
                if (previousSegmentResidues.get(subset.get(0)).getResidueNumber()
                        .compareTo(previousSegmentResidues.get(residueIndex).getResidueNumber()) > 0) {
                    selectedSubsetIndex = subsetIndex;
                    break;
                }
                subsetIndex++;
            }
            if (selectedSubsetIndex == -1) {
                subsets.add(ImmutableList.of(residueIndex));
            } else {
                subsets.add(selectedSubsetIndex, ImmutableList.of(residueIndex));
            }
        }
    }

    private static final List<Double> computeSubsetIndexAverages(final List<List<Integer>> subsets) {
        final List<Double> subsetIndexAverages = Lists
                .newArrayListWithCapacity(org.apache.commons.collections4.CollectionUtils.size(subsets));
        for (List<Integer> subset : subsets) {
            subsetIndexAverages.add(StatUtils.mean(Doubles.toArray(subset)));
        }
        return subsetIndexAverages;
    }

    private static final List<List<Integer>> computeSubsets(final List<Group> previousSegmentResidues,
            final List<List<Integer>> adjacentResidues, final List<Integer> residueIndexes) {
        final List<List<Integer>> subsets = Lists.newArrayList();
        while (!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(residueIndexes)) {
            final List<Integer> subset = identifySubset(previousSegmentResidues, adjacentResidues,
                    residueIndexes);
            subsets.add(subset);
            CollectionUtils.remove(residueIndexes, subset);
        }
        return subsets;
    }

    private static final List<Integer> identifySubset(final List<Group> previousSegmentResidues,
            final List<List<Integer>> adjacentResidues, final List<Integer> residueIndexes) {
        int residueIndex = findIndexOfResidueWithMinimalSequenceNumber(previousSegmentResidues,
                residueIndexes);
        final List<Integer> subset = Lists.newArrayList();
        subset.add(residueIndex);
        boolean finish = true;
        do {
            finish = true;
            for (Integer adjacentResidueIndex : adjacentResidues.get(residueIndex)) {
                if (!subset.contains(adjacentResidueIndex)) {
                    subset.add(adjacentResidueIndex);
                    residueIndex = adjacentResidueIndex;
                    finish = false;
                }
            }
        } while (!finish);
        return subset;
    }

    private static final int findIndexOfResidueWithMinimalSequenceNumber(
            final List<Group> previousSegmentResidues, final List<Integer> residueIndexes) {
        int residueIndex = residueIndexes.get(0);
        Group residueWithMinimalSequenceNumber = previousSegmentResidues.get(residueIndex);
        for (int nextResidueIndex = 1; nextResidueIndex < org.apache.commons.collections4.CollectionUtils
                .size(residueIndexes); nextResidueIndex++) {
            final Group otherResidue = previousSegmentResidues.get(residueIndexes.get(nextResidueIndex));
            if (otherResidue.getResidueNumber()
                    .compareTo(residueWithMinimalSequenceNumber.getResidueNumber()) < 0) {
                residueIndex = residueIndexes.get(nextResidueIndex);
                residueWithMinimalSequenceNumber = otherResidue;
            }
        }
        return residueIndex;
    }

    private static final List<Integer> identifyNonAdjacentResidues(final int segmentResiduesCount,
            final List<List<Integer>> adjacentResidues, final List<Integer> residueIndexes) {
        final List<Integer> nonAdjacentResidues = Lists.newArrayList();
        for (int residueIndex = segmentResiduesCount - 1; residueIndex >= 0; residueIndex--) {
            if (org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(adjacentResidues
                    .get(residueIndex))) {
                residueIndexes.remove(residueIndex);
                nonAdjacentResidues.add(residueIndex);
            }
        }
        return nonAdjacentResidues;
    }

    private static final List<Integer> computeDetailedDistributionOfResidues(final AlignmentImpl current,
            final MoleculeType moleculeType, final int segmentResiduesCount,
            final List<List<Integer>> adjacentResidues, final List<Integer> residuesDistribution) {
        final List<Integer> newResiduesDistribution = Lists.newArrayList(residuesDistribution);
        for (int residueIndex = 0; residueIndex < segmentResiduesCount
                - (MAXIMAL_NUMBER_OF_ADJACENT_RESIDUES + 1); residueIndex++) {
            if (residuesDistribution.get(residueIndex).intValue() < MAXIMAL_NUMBER_OF_ADJACENT_RESIDUES) {
                analyseNonFullyAdjacentResidue(current, moleculeType, segmentResiduesCount, adjacentResidues,
                        residuesDistribution, newResiduesDistribution, residueIndex);
            }
        }
        return newResiduesDistribution;
    }

    private static final void analyseNonFullyAdjacentResidue(final AlignmentImpl current,
            final MoleculeType moleculeType, final int segmentResiduesCount,
            final List<List<Integer>> adjacentResidues, final List<Integer> residuesDistribution,
            final List<Integer> newResiduesDistribution, final int residueIndex) {
        final Group currentResidue = current.secondDescriptorResidues.get(residueIndex);
        for (int nextResidueIndex = residueIndex + MAXIMAL_NUMBER_OF_ADJACENT_RESIDUES; nextResidueIndex < segmentResiduesCount; nextResidueIndex++) {
            if (residuesDistribution.get(nextResidueIndex).intValue() < MAXIMAL_NUMBER_OF_ADJACENT_RESIDUES) {
                final Group nextResidue = current.secondDescriptorResidues.get(nextResidueIndex);
                if (ResidueUtils.areResiduesConnected(moleculeType, currentResidue, nextResidue)) {
                    newResiduesDistribution.set(residueIndex, newResiduesDistribution.get(residueIndex)
                            .intValue() + 1);
                    newResiduesDistribution.set(nextResidueIndex,
                            newResiduesDistribution.get(nextResidueIndex).intValue() + 1);
                    adjacentResidues.get(residueIndex).add(nextResidueIndex);
                    adjacentResidues.get(nextResidueIndex).add(residueIndex);
                }
            }
        }
    }

    private static final List<Integer> computeBasicDistributionOfResidues(final AlignmentImpl current,
            final MoleculeType moleculeType, final int segmentResiduesCount,
            final List<List<Integer>> adjacentResidues) {
        final List<Integer> residuesDistribution = Lists.newArrayList(Collections.nCopies(
                segmentResiduesCount, 0));
        for (int residueIndex = 0; residueIndex < segmentResiduesCount - 1; residueIndex++) {
            final Group currentResidue = current.secondDescriptorResidues.get(residueIndex);
            final int nextResidueIndex = residueIndex + 1;
            final Group nextResidue = current.secondDescriptorResidues.get(nextResidueIndex);
            if (ResidueUtils.areResiduesConnected(moleculeType, currentResidue, nextResidue)) {
                residuesDistribution.set(residueIndex, residuesDistribution.get(residueIndex).intValue() + 1);
                residuesDistribution.set(nextResidueIndex, residuesDistribution.get(nextResidueIndex)
                        .intValue() + 1);
                adjacentResidues.get(residueIndex).add(nextResidueIndex);
                adjacentResidues.get(nextResidueIndex).add(residueIndex);
            }
        }
        return residuesDistribution;
    }

    private static final List<List<Integer>> initAdjacentResidues(final int segmentResiduesCount) {
        final List<List<Integer>> adjacentResidues = Lists.newArrayListWithCapacity(segmentResiduesCount);
        for (int residueIndex = 0; residueIndex < segmentResiduesCount; residueIndex++) {
            final List<Integer> adjacentResiduesContainer = Lists
                    .newArrayListWithCapacity(MAXIMAL_NUMBER_OF_ADJACENT_RESIDUES);
            adjacentResidues.add(adjacentResiduesContainer);
        }
        return adjacentResidues;
    }

    private static final void orderAtoms(final AlignmentImpl current, final int alignmentAtomsCount,
            final int currentSegmentStartIndex, final int currentSegmentEndIndex, final List<Integer> ordering) {
        final List<List<Atom>> segmentAtoms = Lists.newArrayListWithExpectedSize(alignmentAtomsCount);
        for (int containerIndex = 0; containerIndex < alignmentAtomsCount; containerIndex++) {
            final List<Atom> container = Lists.newArrayList();
            segmentAtoms.add(container);
        }
        for (int atomIndex = currentSegmentStartIndex * alignmentAtomsCount; atomIndex < (currentSegmentEndIndex + 1)
                * alignmentAtomsCount; atomIndex++) {
            final int containerIndex = atomIndex % alignmentAtomsCount;
            segmentAtoms.get(containerIndex).add(current.secondDescriptorAtoms.get(atomIndex));
        }
        for (List<Atom> atoms : segmentAtoms) {
            CollectionUtils.sortAccordingToOrdering(atoms, ordering);
        }
        for (int separatedAtomIndex = 0; separatedAtomIndex < org.apache.commons.collections4.CollectionUtils
                .size(segmentAtoms.get(0)); separatedAtomIndex++) {
            for (int atomIndex = 0; atomIndex < alignmentAtomsCount; atomIndex++) {
                current.secondDescriptorAtoms.set((currentSegmentStartIndex + separatedAtomIndex)
                        * alignmentAtomsCount + atomIndex, segmentAtoms.get(atomIndex)
                        .get(separatedAtomIndex));
            }
        }
    }
}

package edu.put.ma.descs.algorithms;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.put.ma.descs.ComparisonPrecision;
import edu.put.ma.model.AlignedDuplexesPair;
import edu.put.ma.model.DescriptorsPair;
import edu.put.ma.model.ExtendedAlignment;
import edu.put.ma.model.ExtendedAlignmentImpl;

public class HungarianMethodDrivenSearch extends CommonAlgorithm {

    private static final double MAXIMAL_COST = 1000.0;

    public enum AlgorithmType {
        FIRST, SECOND, THIRD;
    }

    private final HungarianMethod hungarianMethod;

    private final AlgorithmType type;

    private double maximalRmsdThresholdPerDuplexPair;

    private double[][] costs;

    private int[][] assignment;

    private List<List<AlignedDuplexesPair>> assignments;

    public HungarianMethodDrivenSearch(final boolean firstAlignmentOnly, final ComparisonPrecision precision,
            final double rmsdThreshold, final AlgorithmType type) {
        super(firstAlignmentOnly, precision);
        this.maximalRmsdThresholdPerDuplexPair = rmsdThreshold;
        this.type = type;
        this.hungarianMethod = new HungarianMethodImpl();
        this.assignments = Lists.newArrayList();
    }

    @Override
    void extendAlignment(final DescriptorsPair descriptorsPair, final ExtendedAlignment currentAlignment,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs,
            final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        this.assignments = edu.put.ma.utils.CollectionUtils.prepareList(assignments);
        updateMaximalRmsdThresholdPerDuplexPair();
        extend(descriptorsPair, currentAlignment, allAlignedDuplexesPairs, alignmentAcceptanceMode);
    }

    private void extend(final DescriptorsPair descriptorsPair, final ExtendedAlignment currentAlignment,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs,
            final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        final int firstDescriptorOtherElementsCount = descriptorsPair.getFirstDescriptorElementsCount() - 1;
        final int secondDescriptorOtherElementsCount = descriptorsPair.getSecondDescriptorElementsCount() - 1;
        final int minOtherElementsCount = Math.min(firstDescriptorOtherElementsCount,
                secondDescriptorOtherElementsCount);
        final int maxOtherElementsCount = Math.max(firstDescriptorOtherElementsCount,
                secondDescriptorOtherElementsCount);
        final int maxFeasibleOtherElementsCount = (int) Math.floor((0.8 * (maxOtherElementsCount + 1)) - 1);
        final List<AlignedDuplexesPair> currentAlignedDuplexPairs = Lists.newArrayList();
        for (int feasibleOtherElementsCount = minOtherElementsCount; feasibleOtherElementsCount >= maxFeasibleOtherElementsCount; feasibleOtherElementsCount--) {
            final int promisingOtherElementsCountOfFirstDescriptor = CollectionUtils
                    .size(allAlignedDuplexesPairs);
            final Map<Integer, Map<Integer, Integer>> alignedDuplexPairsAccessMap = Maps
                    .newHashMapWithExpectedSize(promisingOtherElementsCountOfFirstDescriptor);
            this.costs = constructCostMatrix(descriptorsPair, maxOtherElementsCount
                    + (minOtherElementsCount - feasibleOtherElementsCount), minOtherElementsCount,
                    maxOtherElementsCount, allAlignedDuplexesPairs, alignedDuplexPairsAccessMap);
            this.assignment = solveMaximumSizeAssignmentProblem(hungarianMethod, costs);
            final List<AlignedDuplexesPair> newAssignment = extendBasedOnAssignment(descriptorsPair,
                    Lists.newArrayList(currentAlignedDuplexPairs), allAlignedDuplexesPairs,
                    feasibleOtherElementsCount, alignedDuplexPairsAccessMap);
            if (!CollectionUtils.sizeIsEmpty(newAssignment)) {
                addNewAssignment(newAssignment);
                if (firstAlignmentOnly) {
                    break;
                }
            }
        }
        if (type == AlgorithmType.THIRD) {
            introducePartialAssignments(maxFeasibleOtherElementsCount);
        }
        analyseAlignments(descriptorsPair, currentAlignment, alignmentAcceptanceMode);
    }

    private void introducePartialAssignments(int maxFeasibleOtherElementsCount) {
        final List<List<AlignedDuplexesPair>> generatedAssignments = Lists.newArrayList(assignments);
        for (List<AlignedDuplexesPair> assignment : generatedAssignments) {
            final int assignmentSize = CollectionUtils.size(assignment);
            for (int feasiblePartialAssignmentSize = assignmentSize - 1; feasiblePartialAssignmentSize >= maxFeasibleOtherElementsCount; feasiblePartialAssignmentSize--) {
                final double maxTotalRmsdThreshold = feasiblePartialAssignmentSize
                        * maximalRmsdThresholdPerDuplexPair;
                final int numberOfRejectedDuplexPairs = assignmentSize - feasiblePartialAssignmentSize;
                if (numberOfRejectedDuplexPairs > 0) {
                    identifyPartialAssignments(Lists.newArrayList(assignment), numberOfRejectedDuplexPairs,
                            maxTotalRmsdThreshold, feasiblePartialAssignmentSize);
                }
            }
        }
    }

    private void identifyPartialAssignments(final List<AlignedDuplexesPair> assignment,
            final int numberOfRejectedDuplexPairs, final double maxTotalRmsdThreshold,
            final int feasiblePartialAssignmentSize) {
        if (numberOfRejectedDuplexPairs == 0) {
            insertPartialAssignmentEnsuringAssignmentSizesOrder(assignment, feasiblePartialAssignmentSize,
                    maxTotalRmsdThreshold);
        } else {
            for (int alignedPairIndex = 0; alignedPairIndex < CollectionUtils.size(assignment); alignedPairIndex++) {
                final AlignedDuplexesPair alignedDuplexesPair = assignment.remove(alignedPairIndex);
                identifyPartialAssignments(assignment, numberOfRejectedDuplexPairs - 1,
                        maxTotalRmsdThreshold, feasiblePartialAssignmentSize);
                assignment.add(alignedPairIndex, alignedDuplexesPair);
            }
        }
    }

    private void insertPartialAssignmentEnsuringAssignmentSizesOrder(
            final List<AlignedDuplexesPair> assignment, final int assignmentSize,
            final double maxTotalRmsdThreshold) {
        if ((isNewAssignment(assignment, assignmentSize))
                && (Double.compare(ExtendedAlignmentImpl.computeTotalRmsd(assignment), maxTotalRmsdThreshold) <= 0)) {
            final int offset = findIndexOfAssignmentWithLowerSize(assignmentSize);
            addNewAssignment(offset, Lists.newArrayList(assignment));
        }
    }

    private int findIndexOfAssignmentWithLowerSize(int newAssignmentSize) {
        int offset = -1;
        int assignmentIndex = 0;
        for (List<AlignedDuplexesPair> assignment : assignments) {
            final int assignmentSize = CollectionUtils.size(assignment);
            if (assignmentSize < newAssignmentSize) {
                offset = assignmentIndex;
                break;
            }
            assignmentIndex++;
        }
        return offset;
    }

    private boolean isNewAssignment(final List<AlignedDuplexesPair> newAssignment, final int newAssignmentSize) {
        boolean isNew = true;
        for (List<AlignedDuplexesPair> assignment : assignments) {
            final int assignmentSize = CollectionUtils.size(assignment);
            if ((newAssignmentSize > assignmentSize)
                    || ((newAssignmentSize == assignmentSize) && (CollectionUtils.isEqualCollection(
                            assignment, newAssignment)))) {
                if (newAssignmentSize == assignmentSize) {
                    isNew = false;
                }
                break;
            }
        }
        return isNew;
    }

    private void analyseAlignments(final DescriptorsPair descriptorsPair,
            final ExtendedAlignment currentAlignment, final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        final List<List<AlignedDuplexesPair>> currentAssignments = Lists.newArrayList(assignments);
        while (!CollectionUtils.sizeIsEmpty(currentAssignments)) {
            final List<AlignedDuplexesPair> longerAssignment = currentAssignments.remove(0);
            final ExtendedAlignment currentAlignmentCopy = currentAlignment.copy();
            descriptorsComparator.constructExtension(descriptorsPair,
                    currentAlignmentCopy.getCurrentAlignment(), longerAssignment, precision);
            currentAlignmentCopy.setAlignedDuplexPairs(longerAssignment);
            final boolean newLongestAlignmentFound = updateLongestAlignment(descriptorsPair,
                    currentAlignmentCopy, precision, !CREATE_NEW_INSTANCE_OF_CURRENT_ALIGNMENT,
                    alignmentAcceptanceMode);
            if ((newLongestAlignmentFound)
                    || ((!newLongestAlignmentFound) && (longestAlignment.isFirstAlignmentFound()))) {
                filterSubAlignments(longerAssignment, currentAssignments);
            }
        }
    }

    private void filterSubAlignments(final List<AlignedDuplexesPair> longerAssignment,
            final List<List<AlignedDuplexesPair>> currentAssignments) {
        final int currentAssignmentSize = CollectionUtils.size(longerAssignment);
        final int assignmentsCount = CollectionUtils.size(currentAssignments);
        for (int assignmentIndex = assignmentsCount - 1; assignmentIndex >= 0; assignmentIndex--) {
            final List<AlignedDuplexesPair> currentAssignment = currentAssignments.get(assignmentIndex);
            final int assignmentSize = CollectionUtils.size(currentAssignment);
            if ((assignmentSize < currentAssignmentSize)
                    && (CollectionUtils.containsAll(longerAssignment, currentAssignment))) {
                currentAssignments.remove(assignmentIndex);
            }
        }
    }

    private List<AlignedDuplexesPair> extendBasedOnAssignment(final DescriptorsPair descriptorsPair,
            final List<AlignedDuplexesPair> currentAlignedDuplexPairs,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs,
            final int feasibleOtherElementsCount,
            final Map<Integer, Map<Integer, Integer>> alignedDuplexPairsAccessMap) {
        final int firstDescriptorOtherElementsCount = descriptorsPair.getFirstDescriptorElementsCount() - 1;
        final int secondDescriptorOtherElementsCount = descriptorsPair.getSecondDescriptorElementsCount() - 1;
        final double maxTotalRmsdThreshold = feasibleOtherElementsCount * maximalRmsdThresholdPerDuplexPair;
        final int assignmentSize = ArrayUtils.getLength(assignment);
        for (int assignmentElementIndex = 0; assignmentElementIndex < assignmentSize; assignmentElementIndex++) {
            final int firstDescriptorElementIndex = assignment[assignmentElementIndex][0];
            final int secondDescriptorElementIndex = assignment[assignmentElementIndex][1];
            if (shouldAssignmentElementBeSkipped(firstDescriptorElementIndex, secondDescriptorElementIndex,
                    firstDescriptorOtherElementsCount, secondDescriptorOtherElementsCount, costs)) {
                continue;
            }
            final int secondDescriptorElementAccessIndex = alignedDuplexPairsAccessMap.get(
                    firstDescriptorElementIndex).get(secondDescriptorElementIndex);
            final AlignedDuplexesPair alignedDuplexPair = allAlignedDuplexesPairs.get(
                    firstDescriptorElementIndex).get(secondDescriptorElementAccessIndex);
            currentAlignedDuplexPairs.add(alignedDuplexPair);
        }
        if ((type != AlgorithmType.THIRD)
                && (Double.compare(ExtendedAlignmentImpl.computeTotalRmsd(currentAlignedDuplexPairs),
                        maxTotalRmsdThreshold) > 0)) {
            return Collections.emptyList();
        }
        return currentAlignedDuplexPairs;
    }

    private void addNewAssignment(final List<AlignedDuplexesPair> newAssignment) {
        addNewAssignment(-1, newAssignment);
    }

    private void addNewAssignment(final int offset, final List<AlignedDuplexesPair> newAssignment) {
        if (shouldBeConsideredAsPotentialSolution(newAssignment)) {
            final int assignmentsCount = CollectionUtils.size(assignments);
            if ((offset >= 0) && (offset < assignmentsCount)) {
                assignments.add(offset, newAssignment);
            } else {
                assignments.add(newAssignment);
            }
        }
    }

    private boolean shouldBeConsideredAsPotentialSolution(final List<AlignedDuplexesPair> newAssignment) {
        boolean result = true;
        for (List<AlignedDuplexesPair> assignment : assignments) {
            final List<AlignedDuplexesPair> difference = edu.put.ma.utils.CollectionUtils
                    .identifyNewElements(assignment, newAssignment);
            final List<AlignedDuplexesPair> secondDifference = edu.put.ma.utils.CollectionUtils
                    .identifyNewElements(newAssignment, assignment);
            if ((org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(difference))
                    && (org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(secondDifference))) {
                result = false;
                break;
            }
        }
        return result;
    }

    private void updateMaximalRmsdThresholdPerDuplexPair() {
        if (Double.compare(maximalRmsdThresholdPerDuplexPair,
                descriptorsComparator.getMaximalRmsdThresholdPerDuplexPair()) != 0) {
            this.maximalRmsdThresholdPerDuplexPair = descriptorsComparator
                    .getMaximalRmsdThresholdPerDuplexPair();
        }
    }

    private static final int[][] solveMaximumSizeAssignmentProblem(final HungarianMethod hungarianMethod,
            final double[][] costs) {
        final int[][] assignment = hungarianMethod.execute(costs);
        final int assignmentSize = ArrayUtils.getLength(assignment);
        ensureNonDecreasingOrderOfAssignment(assignmentSize, assignment, costs);
        return assignment;
    }

    private static final boolean shouldAssignmentElementBeSkipped(final int firstDescriptorOtherElementIndex,
            final int secondDescriptorOtherElementIndex, final int firstDescriptorOtherElementsCount,
            final int secondDescriptorOtherElementsCount, final double[][] costs) {
        return (firstDescriptorOtherElementIndex >= firstDescriptorOtherElementsCount)
                || (secondDescriptorOtherElementIndex >= secondDescriptorOtherElementsCount)
                || (Double.compare(
                        costs[firstDescriptorOtherElementIndex][secondDescriptorOtherElementIndex],
                        MAXIMAL_COST) == 0);
    }

    private static final void ensureNonDecreasingOrderOfAssignment(final int assignmentSize,
            final int[][] assignment, final double[][] costs) {
        for (int assignmentElementIndex = 0; assignmentElementIndex < assignmentSize; assignmentElementIndex++) {
            double minAlignmentRmsd = costs[assignment[assignmentElementIndex][0]][assignment[assignmentElementIndex][1]];
            int assignmentElementIndexWithMinimalAlignmentRmsd = assignmentElementIndex;
            for (int indexOfAssignmentNextElement = assignmentElementIndex + 1; indexOfAssignmentNextElement < assignmentSize; indexOfAssignmentNextElement++) {
                if (costs[assignment[indexOfAssignmentNextElement][0]][assignment[indexOfAssignmentNextElement][1]] < minAlignmentRmsd) {
                    minAlignmentRmsd = costs[assignment[indexOfAssignmentNextElement][0]][assignment[indexOfAssignmentNextElement][1]];
                    assignmentElementIndexWithMinimalAlignmentRmsd = indexOfAssignmentNextElement;
                }
            }
            swap(assignment, assignmentElementIndex, assignmentElementIndexWithMinimalAlignmentRmsd);
        }
    }

    private static final void swap(final int[][] assignment, final int assignmentElementIndex,
            final int assignmentElementIndexWithMinimalAlignmentRmsd) {
        final int elementSize = ArrayUtils.getLength(assignment[0]);
        for (int componentIndex = 0; componentIndex < elementSize; componentIndex++) {
            int tmp = assignment[assignmentElementIndex][componentIndex];
            assignment[assignmentElementIndex][componentIndex] = assignment[assignmentElementIndexWithMinimalAlignmentRmsd][componentIndex];
            assignment[assignmentElementIndexWithMinimalAlignmentRmsd][componentIndex] = tmp;
        }
    }

    private static final double[][] constructCostMatrix(final DescriptorsPair descriptorsPair,
            final int size, final int minOtherElementsCount, final int maxOtherElementsCount,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs,
            final Map<Integer, Map<Integer, Integer>> alignedDuplexPairsAccessMap) {
        final int firstDescriptorOtherElementsCount = descriptorsPair.getFirstDescriptorElementsCount() - 1;
        final int secondDescriptorOtherElementsCount = descriptorsPair.getSecondDescriptorElementsCount() - 1;
        final double[][] costs = new double[size][size];
        fillDefaults(costs, 0, firstDescriptorOtherElementsCount, 0, secondDescriptorOtherElementsCount,
                MAXIMAL_COST);
        for (Map.Entry<Integer, List<AlignedDuplexesPair>> promisingFirstDescriptorOtherElement : allAlignedDuplexesPairs
                .entrySet()) {
            final int promisingFirstDescriptorOtherElementIndex = promisingFirstDescriptorOtherElement
                    .getKey();
            final Map<Integer, Integer> secondDescriptorOtherElementsAccessMap = Maps.newHashMap();
            int alignedDuplexPairIndex = 0;
            for (AlignedDuplexesPair pair : promisingFirstDescriptorOtherElement.getValue()) {
                costs[pair.getFirstDescriptorOtherElementIndex()][pair.getSecondDescriptorOtherElementIndex()] = pair
                        .getDuplexesPairAlignmentRmsd();
                secondDescriptorOtherElementsAccessMap.put(pair.getSecondDescriptorOtherElementIndex(),
                        alignedDuplexPairIndex++);
            }
            alignedDuplexPairsAccessMap.put(promisingFirstDescriptorOtherElementIndex,
                    secondDescriptorOtherElementsAccessMap);
        }
        fillDefaults(costs, minOtherElementsCount, size, maxOtherElementsCount, size, MAXIMAL_COST);
        return costs;
    }

    private static final void fillDefaults(final double[][] costs, final int fromRowIndex,
            final int toRowIndex, final int fromColIndex, final int toColIndex, final double defaultValue) {
        for (int rowIndex = fromRowIndex; rowIndex < toRowIndex; rowIndex++) {
            Arrays.fill(costs[rowIndex], fromColIndex, toColIndex, defaultValue);
        }
    }

}

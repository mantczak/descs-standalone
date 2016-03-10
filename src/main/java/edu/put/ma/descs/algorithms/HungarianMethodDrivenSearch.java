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
import edu.put.ma.model.Alignment;
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
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs) {
        this.assignments = edu.put.ma.utils.CollectionUtils.prepareList(assignments);
        updateMaximalRmsdThresholdPerDuplexPair();
        extend(descriptorsPair, currentAlignment, allAlignedDuplexesPairs);
    }

    private void extend(final DescriptorsPair descriptorsPair, final ExtendedAlignment currentAlignment,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs) {
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
                    currentAlignment.copy(), Lists.newArrayList(currentAlignedDuplexPairs),
                    allAlignedDuplexesPairs, feasibleOtherElementsCount, alignedDuplexPairsAccessMap);
            if (!CollectionUtils.sizeIsEmpty(newAssignment)) {
                if (type != AlgorithmType.THIRD) {
                    addNewAssignment(newAssignment);
                }
                if (firstAlignmentOnly) {
                    break;
                }
            }
        }
        analyseAlignments(descriptorsPair, currentAlignment);
    }

    private void analyseAlignments(final DescriptorsPair descriptorsPair,
            final ExtendedAlignment currentAlignment) {
        for (List<AlignedDuplexesPair> assignment : assignments) {
            final ExtendedAlignment currentAlignmentCopy = currentAlignment.copy();
            final Alignment extension = descriptorsComparator.constructExtension(descriptorsPair,
                    currentAlignmentCopy.getCurrentAlignment(), assignment, precision);
            if (extension != null) {
                currentAlignmentCopy.setAlignedDuplexPairs(assignment, extension,
                        descriptorsComparator.getAlignmentAtomsCount());
                updateLongestAlignment(descriptorsPair, currentAlignmentCopy, precision,
                        !CREATE_NEW_INSTANCE_OF_CURRENT_ALIGNMENT);
            }
        }
    }

    private List<AlignedDuplexesPair> extendBasedOnAssignment(final DescriptorsPair descriptorsPair,
            final ExtendedAlignment currentAlignment,
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
            extendWithAlignedDuplexPair(descriptorsPair, currentAlignment, currentAlignedDuplexPairs,
                    maxTotalRmsdThreshold, alignedDuplexPair);
        }
        if ((type != AlgorithmType.THIRD)
                && (Double.compare(ExtendedAlignmentImpl.computeTotalRmsd(currentAlignedDuplexPairs),
                        maxTotalRmsdThreshold) > 0)) {
            return Collections.emptyList();
        }
        return currentAlignedDuplexPairs;
    }

    private void extendWithAlignedDuplexPair(final DescriptorsPair descriptorsPair,
            final ExtendedAlignment currentAlignment,
            final List<AlignedDuplexesPair> currentAlignedDuplexPairs, final double maxTotalRmsdThreshold,
            final AlignedDuplexesPair alignedDuplexPair) {
        currentAlignedDuplexPairs.add(alignedDuplexPair);
        if ((type != AlgorithmType.FIRST)
                && ((Double.compare(ExtendedAlignmentImpl.computeTotalRmsd(currentAlignedDuplexPairs),
                        maxTotalRmsdThreshold) <= 0) && (type == AlgorithmType.THIRD))) {
            final Alignment extension = descriptorsComparator.constructExtension(descriptorsPair,
                    currentAlignment.getCurrentAlignment(), alignedDuplexPair, precision);
            if (extension != null) {
                currentAlignment.addAlignedDuplexesPair(alignedDuplexPair, extension,
                        descriptorsComparator.getAlignmentAtomsCount());
                final List<AlignedDuplexesPair> newAlignment = Lists.newArrayList(currentAlignedDuplexPairs);
                addNewAssignment(newAlignment);
            } else {
                removeLastAlignedDuplexesPair(currentAlignedDuplexPairs);
            }
        }
    }

    private void addNewAssignment(final List<AlignedDuplexesPair> newAssignment) {
        if (shouldBeConsideredAsPotentialSolution(newAssignment)) {
            assignments.add(newAssignment);
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

    private static final void removeLastAlignedDuplexesPair(
            final List<AlignedDuplexesPair> currentAlignedDuplexPairs) {
        final int currentAlignedDuplexPairsCount = CollectionUtils.size(currentAlignedDuplexPairs);
        currentAlignedDuplexPairs.remove(currentAlignedDuplexPairsCount - 1);
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

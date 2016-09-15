package edu.put.ma.descs.algorithms;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.put.ma.descs.ComparisonPrecision;
import edu.put.ma.model.AlignedDuplexesPair;
import edu.put.ma.model.Alignment;
import edu.put.ma.model.DescriptorsPair;
import edu.put.ma.model.ExtendedAlignment;

public class BacktrackingDrivenSearch extends CommonAlgorithm {

    private List<ExtendedAlignment> alignments;

    private ExtendedAlignment unacceptableAlignment;

    private int initNotVisitedFirstDescriptorOtherElementsCount;

    public BacktrackingDrivenSearch(final boolean firstAlignmentOnly, final ComparisonPrecision precision) {
        super(firstAlignmentOnly, precision);
        this.alignments = Lists.newArrayList();
    }

    @Override
    void extendAlignment(final DescriptorsPair descriptorsPair, final ExtendedAlignment currentAlignment,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs,
            final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        this.alignments = edu.put.ma.utils.CollectionUtils.prepareList(this.alignments);
        this.initNotVisitedFirstDescriptorOtherElementsCount = CollectionUtils.size(allAlignedDuplexesPairs);
        checkAndExtend(descriptorsPair, currentAlignment, allAlignedDuplexesPairs, alignmentAcceptanceMode);
    }

    private boolean checkAndExtend(final DescriptorsPair descriptorsPair,
            final ExtendedAlignment currentAlignment,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs,
            final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        final int notVisitedFirstDescriptorOtherElementsCount = CollectionUtils.size(allAlignedDuplexesPairs);
        if (notVisitedFirstDescriptorOtherElementsCount == initNotVisitedFirstDescriptorOtherElementsCount - 1) {
            unacceptableAlignment = null;
        }
        if ((notVisitedFirstDescriptorOtherElementsCount == 0) && (verifyPossibleAlignment(currentAlignment))) {
            return true;
        } else if (notVisitedFirstDescriptorOtherElementsCount > 0) {
            return extendPartialAlignment(descriptorsPair, currentAlignment, allAlignedDuplexesPairs,
                    notVisitedFirstDescriptorOtherElementsCount, alignmentAcceptanceMode);
        }
        return false;
    }

    private boolean extendPartialAlignment(final DescriptorsPair descriptorsPair,
            final ExtendedAlignment currentAlignment,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs,
            final int notVisitedFirstDescriptorOtherElementsCount,
            final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        if (verifyUnacceptabilityOfPossibleAlignment(currentAlignment, allAlignedDuplexesPairs)) {
            return false;
        }
        if ((currentAlignment.getAlignedElementsCount() + notVisitedFirstDescriptorOtherElementsCount < longestAlignment
                .getAlignedElementsCount())
                || (leadToIdentifiedAlignments(currentAlignment, allAlignedDuplexesPairs))) {
            return false;
        }
        return extend(descriptorsPair, currentAlignment, allAlignedDuplexesPairs, alignmentAcceptanceMode);
    }

    private boolean verifyUnacceptabilityOfPossibleAlignment(final ExtendedAlignment currentAlignment,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs) {
        if ((unacceptableAlignment != null) && (CollectionUtils.size(allAlignedDuplexesPairs) == 1)) {
            if (cover(currentAlignment, unacceptableAlignment, allAlignedDuplexesPairs)) {
                return true;
            } else {
                unacceptableAlignment = null;
            }
        }
        return false;
    }

    private boolean verifyPossibleAlignment(final ExtendedAlignment currentAlignment) {
        if (currentAlignment.isUpdated()) {
            addAlignment(currentAlignment.copy());
            unacceptableAlignment = null;
            if (firstAlignmentOnly) {
                return true;
            }
        } else {
            unacceptableAlignment = currentAlignment.copy();
        }
        return false;
    }

    private void addAlignment(final ExtendedAlignment newAlignment) {
        int alignmentsCount = CollectionUtils.size(alignments);
        for (int alignmentIndex = alignmentsCount - 1; alignmentIndex >= 0; alignmentIndex--) {
            final ExtendedAlignment currentAlignment = alignments.get(alignmentIndex);
            if (newAlignment.cover(currentAlignment)) {
                alignments.remove(alignmentIndex);
            }
        }
        alignments.add(0, newAlignment);
    }

    private boolean extend(final DescriptorsPair descriptorsPair, final ExtendedAlignment currentAlignment,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs,
            final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        for (Map.Entry<Integer, List<AlignedDuplexesPair>> promisingFirstDescriptorOtherElement : allAlignedDuplexesPairs
                .entrySet()) {
            final List<AlignedDuplexesPair> alignedDuplexPairsOfFirstDescriptorOtherElement = allAlignedDuplexesPairs
                    .get(promisingFirstDescriptorOtherElement.getKey());
            if (analyseAlignedDuplexPairsOfFirstDescriptorOtherElement(descriptorsPair, currentAlignment,
                    allAlignedDuplexesPairs, alignedDuplexPairsOfFirstDescriptorOtherElement,
                    alignmentAcceptanceMode)) {
                return true;
            }
        }
        return false;
    }

    private boolean analyseAlignedDuplexPairsOfFirstDescriptorOtherElement(
            final DescriptorsPair descriptorsPair, final ExtendedAlignment currentAlignment,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs,
            final List<AlignedDuplexesPair> alignedDuplexPairsOfFirstDescriptorOtherElement,
            final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        for (AlignedDuplexesPair alignedDuplexesPair : alignedDuplexPairsOfFirstDescriptorOtherElement) {
            final Alignment extension = descriptorsComparator.constructExtension(descriptorsPair,
                    currentAlignment.getCurrentAlignment(), alignedDuplexesPair, precision);
            if (extension.getAlignedResiduesCount() > 0) {
                currentAlignment.addAlignedDuplexesPair(alignedDuplexesPair, extension,
                        descriptorsComparator.getAlignmentAtomsCount(), descriptorsPair);
                updateLongestAlignment(descriptorsPair, currentAlignment, precision,
                        CREATE_NEW_INSTANCE_OF_CURRENT_ALIGNMENT, alignmentAcceptanceMode);
                final Map<Integer, List<AlignedDuplexesPair>> updatedAllAlignedDuplexesPairs = updateAndReturnNewInstanceOfAlignedDuplexesPairsContainer(
                        alignedDuplexesPair, allAlignedDuplexesPairs);
                if (checkAndExtend(descriptorsPair, currentAlignment, updatedAllAlignedDuplexesPairs,
                        alignmentAcceptanceMode)) {
                    return true;
                } else {
                    currentAlignment.removeLastAlignedDuplexesPair();
                }
            }
        }
        return false;
    }

    private boolean leadToIdentifiedAlignments(final ExtendedAlignment currentAlignment,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs) {
        boolean result = false;
        final int identifiedAlignmentsCount = CollectionUtils.size(alignments);
        for (int alignmentIndex = 0; alignmentIndex < identifiedAlignmentsCount; alignmentIndex++) {
            final ExtendedAlignment identifiedAlignment = alignments.get(alignmentIndex);
            result = !identifiedAlignment.isOverlapped()
                    && cover(currentAlignment, identifiedAlignment, allAlignedDuplexesPairs);
            if (result) {
                break;
            }
        }
        return result;
    }

    private static final Map<Integer, List<AlignedDuplexesPair>> updateAndReturnNewInstanceOfAlignedDuplexesPairsContainer(
            final AlignedDuplexesPair alignedDuplexesPair,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs) {
        final int visitedOtherElementIndexOfFirstDescriptor = alignedDuplexesPair
                .getFirstDescriptorOtherElementIndex();
        final int visitedOtherElementIndexOfSecondDescriptor = alignedDuplexesPair
                .getSecondDescriptorOtherElementIndex();
        final Map<Integer, List<AlignedDuplexesPair>> result = Maps.newHashMap();
        for (Map.Entry<Integer, List<AlignedDuplexesPair>> promisingFirstDescriptorOtherElement : allAlignedDuplexesPairs
                .entrySet()) {
            if (promisingFirstDescriptorOtherElement.getKey().intValue() != visitedOtherElementIndexOfFirstDescriptor) {
                final List<AlignedDuplexesPair> availableDuplexesPairs = filterAvailableDuplexPairs(
                        visitedOtherElementIndexOfSecondDescriptor, promisingFirstDescriptorOtherElement);
                int availableDuplexesPairsCount = CollectionUtils.size(availableDuplexesPairs);
                if (availableDuplexesPairsCount > 0) {
                    result.put(promisingFirstDescriptorOtherElement.getKey(), availableDuplexesPairs);
                }
            }
        }
        return result;
    }

    private static List<AlignedDuplexesPair> filterAvailableDuplexPairs(
            final int visitedOtherElementIndexOfSecondDescriptor,
            final Map.Entry<Integer, List<AlignedDuplexesPair>> promisingFirstDescriptorOtherElement) {
        final List<AlignedDuplexesPair> availableDuplexesPairs = Lists
                .newArrayList(promisingFirstDescriptorOtherElement.getValue());
        int availableDuplexesPairsCount = CollectionUtils.size(availableDuplexesPairs);
        for (int availableDuplexesPairIndex = availableDuplexesPairsCount - 1; availableDuplexesPairIndex >= 0; availableDuplexesPairIndex--) {
            final AlignedDuplexesPair availableDuplexesPair = availableDuplexesPairs
                    .get(availableDuplexesPairIndex);
            if (availableDuplexesPair.getSecondDescriptorOtherElementIndex() == visitedOtherElementIndexOfSecondDescriptor) {
                availableDuplexesPairs.remove(availableDuplexesPairIndex);
            }
        }
        return availableDuplexesPairs;
    }

    private static final boolean cover(final ExtendedAlignment currentAlignment,
            final ExtendedAlignment identifiedAlignment,
            final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs) {
        return identifiedAlignment.cover(currentAlignment)
                && areAllAvailableToAlignDuplexPairsAreConsideredByAlignment(allAlignedDuplexesPairs,
                        identifiedAlignment);
    }

    private static final boolean areAllAvailableToAlignDuplexPairsAreConsideredByAlignment(
            final Map<Integer, List<AlignedDuplexesPair>> availableToAlignDuplexPairs,
            final ExtendedAlignment identifiedAlignment) {
        for (Map.Entry<Integer, List<AlignedDuplexesPair>> firstDescriptorOtherElementAlignedDuplexPairs : availableToAlignDuplexPairs
                .entrySet()) {
            if (!identifiedAlignment.cover(firstDescriptorOtherElementAlignedDuplexPairs.getValue())) {
                return false;
            }
        }
        return true;
    }
}

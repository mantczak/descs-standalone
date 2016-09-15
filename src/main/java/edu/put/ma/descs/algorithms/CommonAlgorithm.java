package edu.put.ma.descs.algorithms;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import edu.put.ma.descs.ComparisonPrecision;
import edu.put.ma.descs.DescriptorsComparator;
import edu.put.ma.descs.SimilarDescriptorsVerifier;
import edu.put.ma.descs.algorithms.logger.ProcessingTimeLog;
import edu.put.ma.model.AlignedDuplexesPair;
import edu.put.ma.model.DescriptorsPair;
import edu.put.ma.model.ExtendedAlignment;
import edu.put.ma.model.RmsdModel;

@RequiredArgsConstructor
public abstract class CommonAlgorithm implements ComparisonAlgorithm {

    static final boolean COMPREHENSIVE_SEARCH = false;

    static final boolean FIRST_ALIGNMENT_ONLY = true;

    static final boolean CREATE_NEW_INSTANCE_OF_CURRENT_ALIGNMENT = true;

    final boolean firstAlignmentOnly;

    final ComparisonPrecision precision;

    ExtendedAlignment longestAlignment;

    DescriptorsComparator descriptorsComparator;

    @Override
    @ProcessingTimeLog
    public ExtendedAlignment extendAlignment(final DescriptorsComparator descriptorsComparator,
            final DescriptorsPair descriptorsPair, final ExtendedAlignment extendedOriginElementsAlignment,
            final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        init(descriptorsComparator, extendedOriginElementsAlignment);
        final Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs = descriptorsComparator
                .getDuplexPairsSimilarityContainer();
        extendAlignment(descriptorsPair, extendedOriginElementsAlignment.copy(), allAlignedDuplexesPairs,
                alignmentAcceptanceMode);
        return longestAlignment;
    }

    public static final double getRatio(final int currentElementsCount, final int firstTargetElementsCount,
            final int secondTargetElementsCount) {
        final double firstRatio = ((double) currentElementsCount) / firstTargetElementsCount;
        final double secondRatio = ((double) currentElementsCount) / secondTargetElementsCount;
        return Math.min(firstRatio, secondRatio);
    }

    abstract void extendAlignment(DescriptorsPair descriptorsPair, ExtendedAlignment currentAlignment,
            Map<Integer, List<AlignedDuplexesPair>> allAlignedDuplexesPairs,
            AlignmentAcceptanceMode alignmentAcceptanceMode);

    protected boolean updateLongestAlignment(final DescriptorsPair descriptorsPair,
            final ExtendedAlignment currentAlignment, final ComparisonPrecision precision,
            final boolean newInstance, final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        if ((currentAlignment.getAlignedResiduesCount() > longestAlignment.getAlignedResiduesCount())
                || isBetterAlignmentFoundEvenWhenAlignedResiduesCountIsNotChanged(longestAlignment,
                        currentAlignment, alignmentAcceptanceMode)) {
            return verifyFeasibilityAlignment(descriptorsPair, currentAlignment, precision, newInstance);
        }
        return false;
    }

    private boolean verifyFeasibilityAlignment(final DescriptorsPair descriptorsPair,
            final ExtendedAlignment currentAlignment, final ComparisonPrecision precision,
            final boolean newInstance) {
        final SimilarDescriptorsVerifier verifier = descriptorsComparator.getSimilarDescriptorsVerifier();
        final double alignedElementsRatio = getRatio(currentAlignment.getAlignedElementsCount(),
                descriptorsPair.getFirstDescriptorElementsCount(),
                descriptorsPair.getSecondDescriptorElementsCount());
        boolean shouldBeAnalyzed = ComparisonPrecision.ALL_RULES_EXCEPT_ALIGNMENT_RMSD.atLeast(precision);
        if ((!shouldBeAnalyzed)
                || ((shouldBeAnalyzed) && (verifier
                        .isMinimalAlignedElementsRatioAchieved(alignedElementsRatio)))) {
            final double alignedResiduesRatio = getRatio(currentAlignment.getAlignedResiduesCount(),
                    descriptorsPair.getFirstDescriptorResiduesCount(),
                    descriptorsPair.getSecondDescriptorResiduesCount());
            shouldBeAnalyzed = ComparisonPrecision.ALIGNED_RESIDUES_CONSIDERED_ONLY.atLeast(precision);
            if ((!shouldBeAnalyzed)
                    || ((shouldBeAnalyzed) && (verifier
                            .isMinimalAlignedResiduesRatioAchieved(alignedResiduesRatio)))) {
                return verifyGlobalRmsdOfAlignment(currentAlignment, precision, newInstance, verifier,
                        alignedElementsRatio, alignedResiduesRatio);
            }
        }
        return false;
    }

    private boolean verifyGlobalRmsdOfAlignment(final ExtendedAlignment currentAlignment,
            final ComparisonPrecision precision, final boolean newInstance,
            final SimilarDescriptorsVerifier verifier, final double alignedElementsRatio,
            final double alignedResiduesRatio) {
        final boolean shouldBeAnalyzed = ComparisonPrecision.ALL_RULES_CONSIDERED.atLeast(precision);
        RmsdModel currentAlignmentRmsdModel = null;
        if (shouldBeAnalyzed) {
            currentAlignmentRmsdModel = currentAlignment.computeAlignmentRmsd();
        } else {
            currentAlignmentRmsdModel = new RmsdModel(currentAlignment.getAvgRmsd());
        }
        final double alignmentRmsd = currentAlignmentRmsdModel.getAlignmentRmsd();
        if ((!shouldBeAnalyzed)
                || ((shouldBeAnalyzed) && (verifier.isAlignmentStructurallySimilar(alignmentRmsd)))) {
            currentAlignment.update(descriptorsComparator.getSimilarDescriptorsVerifier(),
                    currentAlignmentRmsdModel, alignedElementsRatio, alignedResiduesRatio);
            if (longestAlignment.isBetterAlignmentFound(alignedElementsRatio, alignedResiduesRatio,
                    alignmentRmsd)) {
                if (newInstance) {
                    longestAlignment = currentAlignment.copy();
                } else {
                    longestAlignment = currentAlignment;
                }
                return true;
            }
        }
        return false;
    }

    private void init(final DescriptorsComparator descriptorsComparator,
            final ExtendedAlignment extendedOriginElementsAlignment) {
        this.descriptorsComparator = descriptorsComparator;
        this.longestAlignment = extendedOriginElementsAlignment;
    }

    private static final boolean isBetterAlignmentFoundEvenWhenAlignedResiduesCountIsNotChanged(
            final ExtendedAlignment longestAlignment, final ExtendedAlignment currentAlignment,
            final AlignmentAcceptanceMode alignmentAcceptanceMode) {
        return (currentAlignment.getAlignedResiduesCount() == longestAlignment.getAlignedResiduesCount())
                && (alignmentAcceptanceMode == AlignmentAcceptanceMode.ALIGNED_RESIDUES_ONLY || (alignmentAcceptanceMode == AlignmentAcceptanceMode.ALIGNED_RESIDUES_AND_AVERAGE_RMSD_OF_ALIGNED_DUPLEXES && (Double
                        .compare(longestAlignment.getAvgRmsd(), currentAlignment.getAvgRmsd()) >= 0)));
    }
}

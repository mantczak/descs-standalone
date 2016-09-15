package edu.put.ma.model;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.put.ma.descs.SimilarDescriptorsVerifier;
import edu.put.ma.model.AlignmentImpl.Segment;

public class ExtendedAlignmentImpl implements ExtendedAlignment {

    private final List<AlignedDuplexesPair> alignedDuplexesPairs;

    @Getter
    private final Alignment currentAlignment;

    private final List<Alignment> extensions;

    private final List<List<Segment>> segmentStates;

    private final List<Boolean> overlappingStates;

    @Getter
    private final ComparisonResult comparisonResult;

    @Getter
    private boolean updated;

    @Getter
    private boolean overlapped;

    ExtendedAlignmentImpl(final Alignment alignment, final ComparisonResult comparisonResult) {
        this.currentAlignment = alignment;
        this.comparisonResult = comparisonResult;
        this.alignedDuplexesPairs = Lists.newArrayList();
        this.extensions = Lists.newArrayList();
        this.segmentStates = Lists.newArrayList();
        this.overlappingStates = Lists.newArrayList();
        this.updated = false;
        this.overlapped = false;
    }

    ExtendedAlignmentImpl(final ExtendedAlignmentImpl alignment) {
        this(Lists.newArrayList(alignment.alignedDuplexesPairs), alignment.currentAlignment.copy(), Lists
                .newArrayList(alignment.extensions), Lists.newArrayList(alignment.segmentStates), Lists
                .newArrayList(alignment.overlappingStates), ComparisonResultFactory
                .construct(alignment.comparisonResult));
        this.updated = alignment.updated;
        this.overlapped = alignment.overlapped;
    }

    private ExtendedAlignmentImpl(final List<AlignedDuplexesPair> alignedDuplexesPairs,
            final Alignment currentAlignment, final List<Alignment> extensions,
            final List<List<Segment>> segmentStates, final List<Boolean> overlappingStates,
            final ComparisonResult comparisonResult) {
        this.alignedDuplexesPairs = alignedDuplexesPairs;
        this.currentAlignment = currentAlignment;
        this.extensions = extensions;
        this.segmentStates = segmentStates;
        this.overlappingStates = overlappingStates;
        this.comparisonResult = comparisonResult;
        this.updated = false;
        this.overlapped = false;
    }

    @Override
    public ExtendedAlignment copy() {
        return new ExtendedAlignmentImpl(this);
    }

    @Override
    public List<AlignedDuplexesPair> getAlignedDuplexesPairs() {
        return Collections.unmodifiableList(alignedDuplexesPairs);
    }

    @Override
    public void update(final SimilarDescriptorsVerifier similarDescriptorsVerifier,
            final RmsdModel currentAlignmentRmsdModel, final double alignedElementsRatio,
            final double alignedResiduesRatio) {
        this.updated = true;
        this.comparisonResult.update(similarDescriptorsVerifier, currentAlignmentRmsdModel,
                alignedElementsRatio, alignedResiduesRatio);
    }

    @Override
    public void addAlignedDuplexesPair(final AlignedDuplexesPair alignedDuplexesPair,
            final Alignment extension, final int alignmentAtomsCount, final DescriptorsPair descriptorsPair) {
        updated = false;
        Preconditions.checkNotNull(alignedDuplexesPair, "Aligned duplexes pair should be initialized");
        Preconditions.checkNotNull(extension, "Alignment extension should be initialized");
        alignedDuplexesPairs.add(alignedDuplexesPair);
        extensions.add(extension);
        overlappingStates.add(overlapped);
        overlapped = overlapped
                || extension.getAlignedResiduesCount() != descriptorsPair.getFirstDescriptorElementSize();
        final List<Segment> currentSegmentStates = currentAlignment.extend(extension, alignmentAtomsCount,
                descriptorsPair.getMoleculeType());
        segmentStates.add(currentSegmentStates);
    }

    @Override
    public void setAlignedDuplexPairs(final List<AlignedDuplexesPair> alignedDuplexesPairs) {
        updated = false;
        Preconditions.checkNotNull(alignedDuplexesPairs, "Aligned duplex pairs should be initialized");
        CollectionUtils.addAll(this.alignedDuplexesPairs, alignedDuplexesPairs);
    }

    @Override
    public void removeLastAlignedDuplexesPair() {
        updated = false;
        final int lastAlignedDuplexesPairIndex = getAlignedDuplexesPairsCount() - 1;
        alignedDuplexesPairs.remove(lastAlignedDuplexesPairIndex);
        final List<Segment> currentSegmentStates = segmentStates.remove(lastAlignedDuplexesPairIndex);
        if (!CollectionUtils.sizeIsEmpty(currentSegmentStates)) {
            currentAlignment.revertSegmentStates(currentSegmentStates);
        }
        final Alignment extensionToRemove = extensions.remove(lastAlignedDuplexesPairIndex);
        currentAlignment.remove(extensionToRemove);
        overlapped = overlappingStates.remove(lastAlignedDuplexesPairIndex);
    }

    @Override
    public int getAlignedDuplexesPairsCount() {
        return CollectionUtils.size(alignedDuplexesPairs);
    }

    @Override
    public int getAlignedElementsCount() {
        return getAlignedDuplexesPairsCount() + 1;
    }

    @Override
    public int getAlignedResiduesCount() {
        return currentAlignment.getAlignedResiduesCount();
    }

    @Override
    public RmsdModel computeAlignmentRmsd() {
        return currentAlignment.computeAlignmentRmsd();
    }

    @Override
    public double getTotalRmsd() {
        return computeTotalRmsd(alignedDuplexesPairs);
    }

    @Override
    public double getAvgRmsd() {
        double result = Double.MAX_VALUE;
        final int alignedDuplexesPairsCount = getAlignedDuplexesPairsCount();
        if (alignedDuplexesPairsCount > 0) {
            result = getTotalRmsd() / alignedDuplexesPairsCount;
        }
        return result;
    }

    @Override
    public boolean isFirstAlignmentFound() {
        return comparisonResult.isStructurallySimilar();
    }

    @Override
    public boolean isBetterAlignmentFound(final double alignedElementsRatio,
            final double alignedResiduesRatio, final double alignmentRmsd) {
        return Double.compare(alignedResiduesRatio, comparisonResult.getAlignedResiduesRatio()) > 0
                || ((Double.compare(alignedResiduesRatio, comparisonResult.getAlignedResiduesRatio()) == 0) && (Double
                        .compare(alignmentRmsd, comparisonResult.getAlignmentGlobalRmsd()) < 0));
    }

    @Override
    public String getAlignedDuplexesPairsString() {
        if (CollectionUtils.sizeIsEmpty(alignedDuplexesPairs)) {
            return "Alignment has not been found";
        } else {
            return new StringBuilder("Found alignment consists of the following duplex pairs:\n").append(
                    alignedDuplexesPairs.toString()).toString();
        }
    }

    @Override
    public boolean contains(final AlignedDuplexesPair alignedDuplexesPair) {
        return this.alignedDuplexesPairs.contains(alignedDuplexesPair);
    }

    @Override
    public boolean cover(final ExtendedAlignment currentAlignment) {
        boolean result = true;
        if (currentAlignment instanceof ExtendedAlignmentImpl) {
            ExtendedAlignmentImpl currentAlignmentImpl = (ExtendedAlignmentImpl) currentAlignment;
            return cover(currentAlignmentImpl.alignedDuplexesPairs);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public boolean cover(final List<AlignedDuplexesPair> currentAlignment) {
        return CollectionUtils.containsAll(alignedDuplexesPairs, currentAlignment);
    }

    public static final double computeTotalRmsd(final List<AlignedDuplexesPair> alignedDuplexPairs) {
        double result = Double.MAX_VALUE;
        final int alignedDuplexesPairsCount = CollectionUtils.size(alignedDuplexPairs);
        if (alignedDuplexesPairsCount > 0) {
            double totalRmsd = 0.0;
            for (AlignedDuplexesPair alignedDuplexPair : alignedDuplexPairs) {
                totalRmsd += alignedDuplexPair.getDuplexesPairAlignmentRmsd();
            }
            result = totalRmsd;
        }
        return result;
    }
}

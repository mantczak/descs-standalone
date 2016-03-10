package edu.put.ma.descs;

import lombok.Setter;
import edu.put.ma.model.Descriptor;

@Setter
public class DescriptorsFilterImpl implements DescriptorsFilter {

    private static final int DEFAULT_MAXIMAL_RESIDUES_COUNT = 1000;

    private static final int DEFAULT_MINIMAL_RESIDUES_COUNT = 1;

    private static final int DEFAULT_MAXIMAL_ELEMENTS_COUNT = 200;

    private static final int DEFAULT_MINIMAL_ELEMENTS_COUNT = 1;

    private static final int DEFAULT_MAXIMAL_SEGMENTS_COUNT = 50;

    private static final int DEFAULT_MINIMAL_SEGMENTS_COUNT = 1;

    private int minimalSegmentsCount;

    private int maximalSegmentsCount;

    private int minimalElementsCount;

    private int maximalElementsCount;

    private int minimalResiduesCount;

    private int maximalResiduesCount;

    public DescriptorsFilterImpl() {
        this.minimalSegmentsCount = DEFAULT_MINIMAL_SEGMENTS_COUNT;
        this.maximalSegmentsCount = DEFAULT_MAXIMAL_SEGMENTS_COUNT;
        this.minimalElementsCount = DEFAULT_MINIMAL_ELEMENTS_COUNT;
        this.maximalElementsCount = DEFAULT_MAXIMAL_ELEMENTS_COUNT;
        this.minimalResiduesCount = DEFAULT_MINIMAL_RESIDUES_COUNT;
        this.maximalResiduesCount = DEFAULT_MAXIMAL_RESIDUES_COUNT;
    }

    public DescriptorsFilterImpl(final Builder descriptorsFilterBuilder) {
        this.minimalSegmentsCount = descriptorsFilterBuilder.minimalSegmentsCount;
        this.maximalSegmentsCount = descriptorsFilterBuilder.maximalSegmentsCount;
        this.minimalElementsCount = descriptorsFilterBuilder.minimalElementsCount;
        this.maximalElementsCount = descriptorsFilterBuilder.maximalElementsCount;
        this.minimalResiduesCount = descriptorsFilterBuilder.minimalResiduesCount;
        this.maximalResiduesCount = descriptorsFilterBuilder.maximalResiduesCount;
    }

    @Override
    public boolean isAppropriate(final Descriptor descriptor) {
        final int segmentsCount = descriptor.getSegmentsCount();
        final int elementsCount = descriptor.getElementsCount();
        final int residuesCount = descriptor.getSegmentsCount();
        return isInRange(segmentsCount, minimalSegmentsCount, maximalSegmentsCount)
                && isInRange(elementsCount, minimalElementsCount, maximalElementsCount)
                && isInRange(residuesCount, minimalResiduesCount, maximalResiduesCount);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Minimal segments count: ").append(minimalSegmentsCount)
                .append("\n").append("Maximal segments count: ").append(maximalSegmentsCount).append("\n")
                .append("Minimal elements count: ").append(minimalElementsCount).append("\n")
                .append("Maximal elements count: ").append(maximalElementsCount).append("\n")
                .append("Minimal residues count: ").append(minimalResiduesCount).append("\n")
                .append("Maximal residues count: ").append(maximalResiduesCount).toString();
    }

    public static class Builder {

        private int minimalSegmentsCount;

        private int maximalSegmentsCount;

        private int minimalElementsCount;

        private int maximalElementsCount;

        private int minimalResiduesCount;

        private int maximalResiduesCount;

        public Builder() {
            this.minimalSegmentsCount = DEFAULT_MINIMAL_SEGMENTS_COUNT;
            this.maximalSegmentsCount = DEFAULT_MAXIMAL_SEGMENTS_COUNT;
            this.minimalElementsCount = DEFAULT_MINIMAL_ELEMENTS_COUNT;
            this.maximalElementsCount = DEFAULT_MAXIMAL_ELEMENTS_COUNT;
            this.minimalResiduesCount = DEFAULT_MINIMAL_RESIDUES_COUNT;
            this.maximalResiduesCount = DEFAULT_MAXIMAL_RESIDUES_COUNT;
        }

        public Builder minimalSegmentsCount(final int minimalSegmentsCount) {
            this.minimalSegmentsCount = minimalSegmentsCount;
            return this;
        }

        public Builder maximalSegmentsCount(final int maximalSegmentsCount) {
            this.maximalSegmentsCount = maximalSegmentsCount;
            return this;
        }

        public Builder minimalElementsCount(final int minimalElementsCount) {
            this.minimalElementsCount = minimalElementsCount;
            return this;
        }

        public Builder maximalElementsCount(final int maximalElementsCount) {
            this.maximalElementsCount = maximalElementsCount;
            return this;
        }

        public Builder minimalResiduesCount(final int minimalResiduesCount) {
            this.minimalResiduesCount = minimalResiduesCount;
            return this;
        }

        public Builder maximalResiduesCount(final int maximalResiduesCount) {
            this.maximalResiduesCount = maximalResiduesCount;
            return this;
        }

        public DescriptorsFilter build() {
            return new DescriptorsFilterImpl(this);
        }
    }

    private static final boolean isInRange(final int val, final int leftBoundary, final int rightBoundary) {
        return (val >= leftBoundary) && (val <= rightBoundary);
    }
}

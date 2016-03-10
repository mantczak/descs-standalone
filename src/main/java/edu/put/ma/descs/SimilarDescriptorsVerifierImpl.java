package edu.put.ma.descs;

import edu.put.ma.model.ComparisonResult;
import lombok.Setter;
import static edu.put.ma.utils.PreconditionUtils.MAXIMAL_PERCENTAGE_VALUE;

@Setter
public class SimilarDescriptorsVerifierImpl implements SimilarDescriptorsVerifier {

    private static final double DEFAULT_MINIMAL_ALIGNED_RESIDUES_PERCENTAGE = 66.66;

    private static final double DEFAULT_MINIMAL_ALIGNED_ELEMENTS_PERCENTAGE = 80.00;

    private static final double DEFAULT_MAXIMAL_DUPLEXES_PAIR_ALIGNMENT_RMSD = 3.50;

    private static final double DEFAULT_MAXIMAL_ALIGNMENT_GLOBAL_RMSD = 3.50;

    private static final double DEFAULT_MAXIMAL_ORIGIN_ELEMENTS_PAIR_ALIGNMENT_RMSD = 1.20;

    private double maximalOriginElementsPairAlignmentRmsd;

    private double maximalDuplexesPairAlignmentRmsd;

    private double minimalAlignedElementsPercentage;

    private double minimalAlignedResiduesPercentage;

    private double maximalAlignmentGlobalRmsd;

    public SimilarDescriptorsVerifierImpl() {
        this.maximalOriginElementsPairAlignmentRmsd = DEFAULT_MAXIMAL_ORIGIN_ELEMENTS_PAIR_ALIGNMENT_RMSD;
        this.maximalDuplexesPairAlignmentRmsd = DEFAULT_MAXIMAL_DUPLEXES_PAIR_ALIGNMENT_RMSD;
        this.minimalAlignedElementsPercentage = DEFAULT_MINIMAL_ALIGNED_ELEMENTS_PERCENTAGE;
        this.minimalAlignedResiduesPercentage = DEFAULT_MINIMAL_ALIGNED_RESIDUES_PERCENTAGE;
        this.maximalAlignmentGlobalRmsd = DEFAULT_MAXIMAL_ALIGNMENT_GLOBAL_RMSD;
    }

    public SimilarDescriptorsVerifierImpl(final Builder similarDescriptorsVerifierBuilder) {
        this.maximalOriginElementsPairAlignmentRmsd = similarDescriptorsVerifierBuilder.maximalOriginElementsPairAlignmentRmsd;
        this.maximalDuplexesPairAlignmentRmsd = similarDescriptorsVerifierBuilder.maximalDuplexesPairAlignmentRmsd;
        this.minimalAlignedElementsPercentage = similarDescriptorsVerifierBuilder.minimalAlignedElementsPercentage;
        this.minimalAlignedResiduesPercentage = similarDescriptorsVerifierBuilder.minimalAlignedResiduesPercentage;
        this.maximalAlignmentGlobalRmsd = similarDescriptorsVerifierBuilder.maximalAlignmentGlobalRmsd;
    }

    @Override
    public boolean areStructurallySimilar(final ComparisonResult comparisonResult) {
        return arePartialStructurallySimilar(comparisonResult)
                && (Double.compare(comparisonResult.getAlignmentGlobalRmsd(), Double.MAX_VALUE) == 0 || (Double
                        .compare(comparisonResult.getAlignmentGlobalRmsd(), maximalAlignmentGlobalRmsd) <= 0));
    }

    @Override
    public boolean areOriginElementsPairStructurallySimilar(final double originElementsPairAlignmentRmsd) {
        return Double.compare(originElementsPairAlignmentRmsd, maximalOriginElementsPairAlignmentRmsd) <= 0;
    }

    @Override
    public boolean isMinimalAlignedElementsRatioAchieved(final double alignedElementsRatio) {
        return Double.compare(alignedElementsRatio * MAXIMAL_PERCENTAGE_VALUE,
                minimalAlignedElementsPercentage) >= 0;
    }

    @Override
    public boolean isMinimalAlignedResiduesRatioAchieved(final double alignedResiduesRatio) {
        return Double.compare(alignedResiduesRatio * MAXIMAL_PERCENTAGE_VALUE,
                minimalAlignedResiduesPercentage) >= 0;
    }

    @Override
    public boolean areDuplexesPairStructurallySimilar(final double duplexesPairAlignmentRmsd) {
        return Double.compare(duplexesPairAlignmentRmsd, maximalDuplexesPairAlignmentRmsd) <= 0;
    }

    @Override
    public boolean isAlignmentStructurallySimilar(final double alignmentRmsd) {
        return Double.compare(alignmentRmsd, Double.MAX_VALUE) == 0
                || Double.compare(alignmentRmsd, maximalAlignmentGlobalRmsd) <= 0;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Maximal RMSD of the central elements alignment: ")
                .append(String.format("%.2f", maximalOriginElementsPairAlignmentRmsd)).append("\n")
                .append("Maximal RMSD of a pair of aligned duplexes: ")
                .append(String.format("%.2f", maximalDuplexesPairAlignmentRmsd)).append("\n")
                .append("Minimal fraction of aligned elements: ")
                .append(String.format("%.2f", minimalAlignedElementsPercentage / MAXIMAL_PERCENTAGE_VALUE))
                .append("\n").append("Minimal fraction of aligned residues: ")
                .append(String.format("%.2f", minimalAlignedResiduesPercentage / MAXIMAL_PERCENTAGE_VALUE))
                .append("\n").append("Maximal RMSD of the total alignment: ")
                .append(String.format("%.2f", maximalAlignmentGlobalRmsd)).toString();
    }

    public static class Builder {

        private double maximalOriginElementsPairAlignmentRmsd;

        private double maximalDuplexesPairAlignmentRmsd;

        private double minimalAlignedElementsPercentage;

        private double minimalAlignedResiduesPercentage;

        private double maximalAlignmentGlobalRmsd;

        public Builder() {
            this.maximalOriginElementsPairAlignmentRmsd = DEFAULT_MAXIMAL_ORIGIN_ELEMENTS_PAIR_ALIGNMENT_RMSD;
            this.maximalDuplexesPairAlignmentRmsd = DEFAULT_MAXIMAL_DUPLEXES_PAIR_ALIGNMENT_RMSD;
            this.minimalAlignedElementsPercentage = DEFAULT_MINIMAL_ALIGNED_ELEMENTS_PERCENTAGE;
            this.minimalAlignedResiduesPercentage = DEFAULT_MINIMAL_ALIGNED_RESIDUES_PERCENTAGE;
            this.maximalAlignmentGlobalRmsd = DEFAULT_MAXIMAL_ALIGNMENT_GLOBAL_RMSD;
        }

        public Builder maximalOriginElementsPairAlignmentRmsd(
                final double maximalOriginElementsPairAlignmentRmsd) {
            this.maximalOriginElementsPairAlignmentRmsd = maximalOriginElementsPairAlignmentRmsd;
            return this;
        }

        public Builder maximalDuplexesPairAlignmentRmsd(final double maximalDuplexesPairAlignmentRmsd) {
            this.maximalDuplexesPairAlignmentRmsd = maximalDuplexesPairAlignmentRmsd;
            return this;
        }

        public Builder minimalAlignedElementsPercentage(final double minimalAlignedElementsPercentage) {
            this.minimalAlignedElementsPercentage = minimalAlignedElementsPercentage;
            return this;
        }

        public Builder minimalAlignedResiduesPercentage(final double minimalAlignedResiduesPercentage) {
            this.minimalAlignedResiduesPercentage = minimalAlignedResiduesPercentage;
            return this;
        }

        public Builder maximalAlignmentGlobalRmsd(final double maximalAlignmentGlobalRmsd) {
            this.maximalAlignmentGlobalRmsd = maximalAlignmentGlobalRmsd;
            return this;
        }

        public SimilarDescriptorsVerifier build() {
            return new SimilarDescriptorsVerifierImpl(this);
        }
    }

    private boolean arePartialStructurallySimilar(final ComparisonResult comparisonResult) {
        return (Double.compare(comparisonResult.getOriginElementsAlignmentRmsd(),
                maximalOriginElementsPairAlignmentRmsd) <= 0)
                && (Double.compare(comparisonResult.getAlignedElementsRatio() * MAXIMAL_PERCENTAGE_VALUE,
                        minimalAlignedElementsPercentage) >= 0)
                && (Double.compare(comparisonResult.getAlignedResiduesRatio() * MAXIMAL_PERCENTAGE_VALUE,
                        minimalAlignedResiduesPercentage) >= 0);
    }
}

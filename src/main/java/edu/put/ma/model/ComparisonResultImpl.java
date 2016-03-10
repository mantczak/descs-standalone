package edu.put.ma.model;

import org.biojava.nbio.structure.SVDSuperimposer;

import lombok.Getter;
import edu.put.ma.descs.SimilarDescriptorsVerifier;
import edu.put.ma.utils.ResidueUtils;

@Getter
public class ComparisonResultImpl implements ComparisonResult {

    private final double originElementsAlignmentRmsd;

    private double alignedElementsRatio;

    private double alignedResiduesRatio;

    private double alignmentGlobalRmsd;

    private boolean structurallySimilar;

    private SVDSuperimposer superimposer;

    ComparisonResultImpl(final SimilarDescriptorsVerifier similarDescriptorsVerifier,
            final double originElementsAlignmentRmsd, final SVDSuperimposer superimposer,
            final double alignedElementsRatio, final double alignedResiduesRatio) {
        this(originElementsAlignmentRmsd, alignedElementsRatio, alignedResiduesRatio,
                originElementsAlignmentRmsd, superimposer);
        this.structurallySimilar = similarDescriptorsVerifier.areStructurallySimilar(this);
    }

    ComparisonResultImpl(final SimilarDescriptorsVerifier similarDescriptorsVerifier,
            final double originElementsAlignmentRmsd, final SVDSuperimposer superimposer) {
        this(similarDescriptorsVerifier, originElementsAlignmentRmsd, superimposer, 1.0, 1.0);
    }

    ComparisonResultImpl(final ComparisonResultImpl comparisonResult) {
        this(comparisonResult.originElementsAlignmentRmsd, comparisonResult.alignedElementsRatio,
                comparisonResult.alignedResiduesRatio, comparisonResult.alignmentGlobalRmsd,
                comparisonResult.structurallySimilar, comparisonResult.superimposer);
    }

    private ComparisonResultImpl(final double originElementsAlignmentRmsd, final double alignedElementsRatio,
            final double alignedResiduesRatio, final double alignmentGlobalRmsd,
            final SVDSuperimposer superimposer) {
        this.originElementsAlignmentRmsd = originElementsAlignmentRmsd;
        this.alignedElementsRatio = alignedElementsRatio;
        this.alignedResiduesRatio = alignedResiduesRatio;
        this.alignmentGlobalRmsd = alignmentGlobalRmsd;
        this.superimposer = superimposer;
    }

    private ComparisonResultImpl(final double originElementsAlignmentRmsd, final double alignedElementsRatio,
            final double alignedResiduesRatio, final double alignmentGlobalRmsd,
            final boolean structurallySimilar, final SVDSuperimposer superimposer) {
        this.originElementsAlignmentRmsd = originElementsAlignmentRmsd;
        this.alignedElementsRatio = alignedElementsRatio;
        this.alignedResiduesRatio = alignedResiduesRatio;
        this.alignmentGlobalRmsd = alignmentGlobalRmsd;
        this.structurallySimilar = structurallySimilar;
        this.superimposer = superimposer;
    }

    @Override
    public void update(final SimilarDescriptorsVerifier similarDescriptorsVerifier,
            final RmsdModel currentAlignmentRmsdModel, final double alignedElementsRatio,
            final double alignedResiduesRatio) {
        this.superimposer = currentAlignmentRmsdModel.getSuperimposer();
        this.alignedElementsRatio = alignedElementsRatio;
        this.alignedResiduesRatio = alignedResiduesRatio;
        this.alignmentGlobalRmsd = currentAlignmentRmsdModel.getAlignmentRmsd();
        this.structurallySimilar = similarDescriptorsVerifier.areStructurallySimilar(this);
    }

    @Override
    public String toString() {
        return new StringBuilder((structurallySimilar) ? "are" : "are not")
                .append(" structurally similar\n")
                .append("RMSD of the central elements alignment: ")
                .append(String.format("%.3f",
                        ResidueUtils.ensureCommonDoubleFormat(originElementsAlignmentRmsd))).append("\n")
                .append("Fraction of aligned elements: ")
                .append(String.format("%.2f", ResidueUtils.ensureCommonDoubleFormat(alignedElementsRatio)))
                .append("\n").append("Fraction of aligned residues: ")
                .append(String.format("%.2f", ResidueUtils.ensureCommonDoubleFormat(alignedResiduesRatio)))
                .append("\n").append("RMSD of the total alignment: ")
                .append(String.format("%.3f", ResidueUtils.ensureCommonDoubleFormat(alignmentGlobalRmsd)))
                .toString();
    }

}

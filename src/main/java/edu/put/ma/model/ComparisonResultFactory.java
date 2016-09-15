package edu.put.ma.model;

import org.biojava.nbio.structure.SVDSuperimposer;

import edu.put.ma.descs.SimilarDescriptorsVerifier;

public class ComparisonResultFactory {

    private ComparisonResultFactory() {
        // hidden constructor
    }

    public static final ComparisonResult construct(
            final SimilarDescriptorsVerifier similarDescriptorsVerifier,
            final double originElementsAlignmentRmsd, final SVDSuperimposer superimposer,
            final double alignedElementsRatio, final double alignedResiduesRatio) {
        return new ComparisonResultImpl(similarDescriptorsVerifier, originElementsAlignmentRmsd,
                superimposer, alignedElementsRatio, alignedResiduesRatio);
    }

    public static final ComparisonResult construct(
            final SimilarDescriptorsVerifier similarDescriptorsVerifier,
            final double originElementsAlignmentRmsd, final SVDSuperimposer superimposer,
            final String sequenceAlignment) {
        return new ComparisonResultImpl(similarDescriptorsVerifier, originElementsAlignmentRmsd,
                superimposer, sequenceAlignment);
    }

    public static final ComparisonResult construct(final ComparisonResult comparisonResult) {
        if (comparisonResult instanceof ComparisonResultImpl) {
            return new ComparisonResultImpl((ComparisonResultImpl) comparisonResult);
        }
        throw new IllegalArgumentException("Unknown implementation of ComparisonResult interface");
    }
}

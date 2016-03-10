package edu.put.ma.model;

import org.biojava.nbio.structure.SVDSuperimposer;

import edu.put.ma.descs.SimilarDescriptorsVerifier;

public interface ComparisonResult {

    boolean isStructurallySimilar();
    
    double getOriginElementsAlignmentRmsd();

    double getAlignedElementsRatio();

    double getAlignedResiduesRatio();

    double getAlignmentGlobalRmsd();

    SVDSuperimposer getSuperimposer();

    void update(final SimilarDescriptorsVerifier similarDescriptorsVerifier,
            final RmsdModel currentAlignmentRmsdModel, final double alignedElementsRatio,
            final double alignedResiduesRatio);

    String toString();
}

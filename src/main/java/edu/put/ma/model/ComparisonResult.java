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

    void update(SimilarDescriptorsVerifier similarDescriptorsVerifier, RmsdModel currentAlignmentRmsdModel,
            double alignedElementsRatio, double alignedResiduesRatio);

    String toString();

    String getSequenceAlignment();
    
    void setSequenceAlignment(String sequenceAlignment);
}

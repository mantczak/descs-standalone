package edu.put.ma.descs;

import edu.put.ma.model.ComparisonResult;

public interface SimilarDescriptorsVerifier {

    void setMaximalOriginElementsPairAlignmentRmsd(double maximalOriginElementsPairAlignmentRmsd);

    void setMaximalDuplexesPairAlignmentRmsd(double maximalDuplexesPairAlignmentRmsd);

    void setMinimalAlignedElementsPercentage(double minimalAlignedElementsPercentage);

    void setMinimalAlignedResiduesPercentage(double minimalAlignedResiduesPercentage);

    void setMaximalAlignmentGlobalRmsd(double maximalAlignmentGlobalRmsd);

    boolean areStructurallySimilar(ComparisonResult comparisonResult);

    boolean areOriginElementsPairStructurallySimilar(double originElementsPairAlignmentRmsd);

    boolean areDuplexesPairStructurallySimilar(double duplexesPairAlignmentRmsd);

    boolean isMinimalAlignedElementsRatioAchieved(double alignedElementsRatio);

    boolean isMinimalAlignedResiduesRatioAchieved(double alignedResiduesRatio);

    boolean isAlignmentStructurallySimilar(double alignmentRmsd);

    String toString();
}

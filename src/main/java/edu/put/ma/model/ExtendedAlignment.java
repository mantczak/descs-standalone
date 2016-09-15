package edu.put.ma.model;

import java.util.List;

import edu.put.ma.descs.SimilarDescriptorsVerifier;

public interface ExtendedAlignment {

    void update(SimilarDescriptorsVerifier similarDescriptorsVerifier, RmsdModel currentAlignmentRmsdModel,
            double alignedElementsRatio, double alignedResiduesRatio);

    void addAlignedDuplexesPair(AlignedDuplexesPair alignedDuplexesPair, Alignment extension,
            int alignmentAtomsCount, DescriptorsPair descriptorsPair);

    void setAlignedDuplexPairs(List<AlignedDuplexesPair> alignedDuplexesPairs);

    void removeLastAlignedDuplexesPair();

    int getAlignedDuplexesPairsCount();

    int getAlignedElementsCount();

    int getAlignedResiduesCount();

    RmsdModel computeAlignmentRmsd();

    double getTotalRmsd();

    double getAvgRmsd();

    boolean isFirstAlignmentFound();

    boolean isBetterAlignmentFound(double alignedElementsRatio, double alignedResiduesRatio,
            double alignmentRmsd);

    String getAlignedDuplexesPairsString();

    boolean contains(AlignedDuplexesPair alignedDuplexesPair);

    Alignment getCurrentAlignment();

    ComparisonResult getComparisonResult();

    ExtendedAlignment copy();

    boolean cover(ExtendedAlignment currentAlignment);

    boolean isUpdated();

    boolean cover(List<AlignedDuplexesPair> currentAlignment);

    List<AlignedDuplexesPair> getAlignedDuplexesPairs();

    boolean isOverlapped();
}

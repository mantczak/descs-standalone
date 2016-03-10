package edu.put.ma.descs;

import java.util.List;
import java.util.Map;

import edu.put.ma.model.AlignedDuplexesPair;
import edu.put.ma.model.Alignment;
import edu.put.ma.model.ComparisonResult;
import edu.put.ma.model.DescriptorsPair;

public interface DescriptorsComparator {

    ComparisonResult compare(DescriptorsPair descriptorsPair);

    String getDuplexPairsString();

    int getAlignmentAtomsCount();

    Alignment constructExtension(DescriptorsPair descriptorsPair, Alignment currentAlignment,
            AlignedDuplexesPair alignedDuplexesPair, ComparisonPrecision precision);

    Alignment constructExtension(DescriptorsPair descriptorsPair, Alignment currentAlignment,
            List<AlignedDuplexesPair> alignedDuplexPair, ComparisonPrecision precision);

    SimilarDescriptorsVerifier getSimilarDescriptorsVerifier();

    Map<Integer, List<AlignedDuplexesPair>> getDuplexPairsSimilarityContainer();

    double getMaximalRmsdThresholdPerDuplexPair();
}

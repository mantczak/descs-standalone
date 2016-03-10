package edu.put.ma.descs.algorithms;

import edu.put.ma.descs.DescriptorsComparator;
import edu.put.ma.model.DescriptorsPair;
import edu.put.ma.model.ExtendedAlignment;

public interface ComparisonAlgorithm {

    ExtendedAlignment extendAlignment(DescriptorsComparator descriptorsComparator,
            DescriptorsPair descriptorsPair, ExtendedAlignment extendedOriginElementsAlignment);
}

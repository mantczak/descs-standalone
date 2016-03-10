package edu.put.ma.descs;

import edu.put.ma.model.Descriptor;

public interface DescriptorsFilter {

    void setMinimalSegmentsCount(int minimalSegmentsCount);

    void setMaximalSegmentsCount(int maximalSegmentsCount);

    void setMinimalElementsCount(int minimalElementsCount);

    void setMaximalElementsCount(int maximalElementsCount);

    void setMinimalResiduesCount(int minimalResiduesCount);

    void setMaximalResiduesCount(int maximalResiduesCount);

    boolean isAppropriate(Descriptor descriptor);

    String toString();

}

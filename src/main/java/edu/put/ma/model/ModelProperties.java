package edu.put.ma.model;

import edu.put.ma.access.ResiduesAccess;
import edu.put.ma.gaps.GapsDistribution;

public interface ModelProperties {
    
    ResiduesAccess getResiduesAccess();
    
    GapsDistribution getGapsDistribution();

    boolean isValid();
}

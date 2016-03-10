package edu.put.ma.gaps;

public interface GapsDistribution {

    int getGapsDistributionSize();

    int getResidueGapFlag(int index);

    boolean isValid();

    int getNeibhourhoodSize();
    
    int getElementSize();
}

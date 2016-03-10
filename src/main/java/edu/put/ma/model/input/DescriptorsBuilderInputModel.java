package edu.put.ma.model.input;

import edu.put.ma.descs.DescriptorsFilter;
import edu.put.ma.model.MoleculeType;

public interface DescriptorsBuilderInputModel extends CommonInputModel {

    String getInputFilePath();
    
    MoleculeType getMoleculeType();
    
    String getInContactResiduesExpressionString();
    
    int getElementSize();
    
    int getThreadsCount();
    
    DescriptorsFilter getDescriptorsFilter();
    
    String getOutputDirPath();

}

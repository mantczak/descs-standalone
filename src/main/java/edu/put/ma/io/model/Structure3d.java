package edu.put.ma.io.model;

import java.util.List;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;

public interface Structure3d {

    Structure cloneStructure();

    int nrModels();

    List<Chain> getModel(int index);

    ModelInfo getModelInfo(int index);

    boolean shouldEntityIdsBeConsidered();
    
    Structure getRawStructure();
    
    List<ModelInfo> getModelInfos();

}

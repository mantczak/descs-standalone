package edu.put.ma.io.model;

import java.util.List;
import java.util.Map;

import org.biojava.nbio.structure.Structure;

import edu.put.ma.io.model.ModelInfo;

public interface PdbBundle {

    Structure getStructure();

    List<Integer> getModelNos();

    Map<Integer, List<Integer>> getModelEntityIds();

    List<ModelInfo> getModelInfos();
}

package edu.put.ma.io.model;

import java.util.List;

import org.biojava.nbio.structure.Structure;

public final class Structure3dFactory {

    private Structure3dFactory() {
        // hidden constructor
    }

    public static final Structure3d construct(final Structure rawStructure, final List<ModelInfo> modelInfos) {
        return new Structure3dImpl(rawStructure, modelInfos);
    }
}

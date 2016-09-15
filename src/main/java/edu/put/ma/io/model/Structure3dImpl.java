package edu.put.ma.io.model;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.commons.collections4.CollectionUtils;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;

import edu.put.ma.utils.PreconditionUtils;

@RequiredArgsConstructor
public class Structure3dImpl implements Structure3d {

    @Getter
    private final Structure rawStructure;

    private final List<ModelInfo> modelInfos;

    @Override
    public Structure cloneStructure() {
        return rawStructure.clone();
    }

    @Override
    public int nrModels() {
        return rawStructure.nrModels();
    }

    @Override
    public List<Chain> getModel(final int index) {
        PreconditionUtils.checkIfIndexInRange(index, 0, rawStructure.nrModels(), "Model index");
        return rawStructure.getModel(index);
    }

    @Override
    public ModelInfo getModelInfo(final int index) {
        PreconditionUtils.checkIfIndexInRange(index, 0, CollectionUtils.size(modelInfos), "Model info index");
        return modelInfos.get(index);
    }

    @Override
    public boolean shouldEntityIdsBeConsidered() {
        boolean result = false;
        for (ModelInfo modelInfo : modelInfos) {
            if (modelInfo.isEntityIdsConsidered()) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public List<ModelInfo> getModelInfos() {
        return Collections.unmodifiableList(modelInfos);
    }
}

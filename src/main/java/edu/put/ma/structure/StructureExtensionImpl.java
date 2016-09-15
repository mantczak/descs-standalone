package edu.put.ma.structure;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

import org.apache.commons.collections4.CollectionUtils;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;

import edu.put.ma.io.model.ModelInfo;
import edu.put.ma.io.model.Structure3d;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.model.StructureType;
import edu.put.ma.utils.PreconditionUtils;
import edu.put.ma.utils.ResidueUtils;

public class StructureExtensionImpl implements StructureExtension {

    private static final boolean CONSIDER_APPROPRIATE_RESIDUES_ONLY = true;

    private static final String MODEL = "Model";

    private final Structure3d structure3d;

    @Getter
    private final Structure structureWithoutResidues;

    @Getter
    private final boolean valid;

    @Getter
    private final MoleculeType moleculeType;

    @Getter
    private final String inputFileBasename;

    public StructureExtensionImpl(final String inputFileBasename, final Structure3d structure3d,
            final MoleculeType moleculeType, final StructureType structureType) {
        this.inputFileBasename = inputFileBasename;
        this.moleculeType = moleculeType;
        filterResidues(structure3d.getRawStructure(), moleculeType, structureType);
        this.structure3d = structure3d;
        this.structureWithoutResidues = getStructureWithoutResidues(structure3d.getRawStructure());
        valid = this.structure3d.getRawStructure() != null && this.structureWithoutResidues != null;
    }

    @Override
    public List<ModelInfo> getModelInfos() {
        return Collections.unmodifiableList(structure3d.getModelInfos());
    }

    @Override
    public Structure cloneStructure() {
        return structure3d.cloneStructure();
    }

    @Override
    public Structure cloneStructureWithoutResidues() {
        return structureWithoutResidues.clone();
    }

    @Override
    public int getModelsNo() {
        return structure3d.nrModels();
    }

    @Override
    public List<Chain> getModelByIndex(final int index) {
        PreconditionUtils.checkIfIndexInRange(index, 0, structure3d.nrModels(), MODEL);
        return structure3d.getModel(index);
    }

    @Override
    public List<Chain> getModelWithoutResiduesByIndex(final int index) {
        PreconditionUtils.checkIfIndexInRange(index, 0, structureWithoutResidues.nrModels(), MODEL);
        return structureWithoutResidues.getModel(index);
    }

    @Override
    public Structure cloneModelByIndex(final int index) {
        PreconditionUtils.checkIfIndexInRange(index, 0, structure3d.nrModels(), MODEL);
        return prepareModelOfStructureByIndex(cloneStructure(), index);
    }

    @Override
    public Structure cloneModelWithoutResiduesByIndex(final int index) {
        PreconditionUtils.checkIfIndexInRange(index, 0, structureWithoutResidues.nrModels(), MODEL);
        return prepareModelOfStructureByIndex(cloneStructureWithoutResidues(), index);
    }

    @Override
    public Group getResidue(int modelIndex, int chainIndex, int residueIndex) {
        PreconditionUtils.checkIfIndexInRange(modelIndex, 0, structure3d.nrModels(), MODEL);
        final List<Chain> model = structure3d.getModel(modelIndex);
        PreconditionUtils.checkIfIndexInRange(chainIndex, 0, CollectionUtils.size(model), "Chain");
        final Chain chain = model.get(chainIndex);
        PreconditionUtils.checkIfIndexInRange(residueIndex, 0, CollectionUtils.size(chain.getAtomGroups()),
                "Residue");
        return chain.getAtomGroup(residueIndex);
    }

    @Override
    public Group cloneResidue(int modelIndex, int chainIndex, int residueIndex) {
        final Group residue = getResidue(modelIndex, chainIndex, residueIndex);
        return (Group) residue.clone();
    }

    @Override
    public Structure getStructure() {
        return structure3d.getRawStructure();
    }

    public static final Structure prepareModelOfStructureByIndex(final Structure structure, final int index) {
        final List<Chain> model = structure.getModel(index);
        structure.resetModels();
        structure.addModel(model);
        return structure;
    }

    private static final Structure getStructureWithoutResidues(final Structure structure) {
        final Structure structureWithoutResidues = structure.clone();
        for (int modelIndex = 0; modelIndex < structureWithoutResidues.nrModels(); modelIndex++) {
            final List<Chain> model = structureWithoutResidues.getModel(modelIndex);
            for (Chain chain : model) {
                final List<Group> residues = chain.getAtomGroups();
                residues.clear();
            }
        }
        return structureWithoutResidues;
    }

    private static final void filterResidues(final Structure structure, final MoleculeType moleculeType,
            final StructureType structureType) {
        for (int modelIndex = 0; modelIndex < structure.nrModels(); modelIndex++) {
            final List<Chain> model = structure.getModel(modelIndex);
            final List<Integer> chainsThatShouldBeDeleted = ResidueUtils.filterResidues(model, moleculeType,
                    structureType, CONSIDER_APPROPRIATE_RESIDUES_ONLY);
            deleteEmptyChains(model, chainsThatShouldBeDeleted);
        }
    }

    private static final void deleteEmptyChains(final List<Chain> model,
            final List<Integer> chainsThatShouldBeDeleted) {
        final int chainsThatShouldBeDeletedCount = CollectionUtils.size(chainsThatShouldBeDeleted);
        if (chainsThatShouldBeDeletedCount > 0) {
            for (int index = chainsThatShouldBeDeletedCount - 1; index >= 0; index--) {
                final int chainIndex = chainsThatShouldBeDeleted.get(index);
                model.remove(chainIndex);
            }
        }
    }

}

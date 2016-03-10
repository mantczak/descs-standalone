package edu.put.ma.structure;

import java.util.List;

import lombok.Getter;

import org.apache.commons.collections4.CollectionUtils;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;

import edu.put.ma.model.MoleculeType;
import edu.put.ma.utils.PreconditionUtils;
import edu.put.ma.utils.ResidueUtils;

@Getter
public class StructureExtensionImpl implements StructureExtension {

    private static final String MODEL = "Model";

    private final Structure structure;

    private final Structure structureWithoutResidues;

    private final boolean valid;

    private final MoleculeType moleculeType;

    private final String inputFileBasename;

    public StructureExtensionImpl(final String inputFileBasename, final Structure structure,
            final MoleculeType moleculeType) {
        this.inputFileBasename = inputFileBasename;
        this.moleculeType = moleculeType;
        filterResidues(structure, moleculeType);
        this.structure = structure;
        this.structureWithoutResidues = getStructureWithoutResidues(structure);
        valid = this.structure != null && this.structureWithoutResidues != null;
    }

    @Override
    public Structure cloneStructure() {
        return structure.clone();
    }

    @Override
    public Structure cloneStructureWithoutResidues() {
        return structureWithoutResidues.clone();
    }

    @Override
    public int getModelsNo() {
        return structure.nrModels();
    }

    @Override
    public List<Chain> getModelByIndex(final int index) {
        PreconditionUtils.checkIfIndexInRange(index, 0, structure.nrModels(), MODEL);
        return structure.getModel(index);
    }

    @Override
    public List<Chain> getModelWithoutResiduesByIndex(final int index) {
        PreconditionUtils.checkIfIndexInRange(index, 0, structureWithoutResidues.nrModels(), MODEL);
        return structureWithoutResidues.getModel(index);
    }

    @Override
    public Structure cloneModelByIndex(final int index) {
        PreconditionUtils.checkIfIndexInRange(index, 0, structure.nrModels(), MODEL);
        return prepareModelOfStructureByIndex(cloneStructure(), index);
    }

    @Override
    public Structure cloneModelWithoutResiduesByIndex(final int index) {
        PreconditionUtils.checkIfIndexInRange(index, 0, structureWithoutResidues.nrModels(), MODEL);
        return prepareModelOfStructureByIndex(cloneStructureWithoutResidues(), index);
    }

    @Override
    public Group getResidue(int modelIndex, int chainIndex, int residueIndex) {
        PreconditionUtils.checkIfIndexInRange(modelIndex, 0, structure.nrModels(), MODEL);
        final List<Chain> model = structure.getModel(modelIndex);
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

    private static final Structure prepareModelOfStructureByIndex(final Structure structure, final int index) {
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

    private static final void filterResidues(final Structure structure, final MoleculeType moleculeType) {
        for (int modelIndex = 0; modelIndex < structure.nrModels(); modelIndex++) {
            final List<Chain> model = structure.getModel(modelIndex);
            ResidueUtils.filterResidues(model, moleculeType);
        }
    }

}

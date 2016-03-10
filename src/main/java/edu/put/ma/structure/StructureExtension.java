package edu.put.ma.structure;

import java.util.List;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;

import edu.put.ma.model.MoleculeType;

public interface StructureExtension {

    Structure getStructure();

    Structure cloneStructure();

    Structure getStructureWithoutResidues();

    Structure cloneStructureWithoutResidues();
    
    int getModelsNo();

    List<Chain> getModelByIndex(int index);

    List<Chain> getModelWithoutResiduesByIndex(int index);

    Structure cloneModelByIndex(int index);

    Structure cloneModelWithoutResiduesByIndex(int index);

    Group getResidue(int modelIndex, int chainIndex, int residueIndex);

    Group cloneResidue(int modelIndex, int chainIndex, int residueIndex);

    boolean isValid();

    MoleculeType getMoleculeType();
    
    String getInputFileBasename();
}

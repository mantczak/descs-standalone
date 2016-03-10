package edu.put.ma.io.writer;

import org.biojava.nbio.structure.Structure;

import edu.put.ma.io.FormatType;

public class PdbWriter extends CommonWriter {

    @Override
    String getStructureString(final Structure structure) {
        return structure.toPDB();
    }

    @Override
    String transformOutputFilePath(final String outputFilePath) {
        return transformOutputFilePath(outputFilePath, "pdb");
    }

    @Override
    String getAtomsStructureString(Structure structure) {
        return getAtomsStructureString(structure, FormatType.PDB);
    }

}

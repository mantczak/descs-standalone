package edu.put.ma.io.writer;

import org.biojava.nbio.structure.Structure;

import edu.put.ma.io.FormatType;

public class MmcifWriter extends CommonWriter {

    @Override
    String getStructureString(final Structure structure) {
        return structure.toMMCIF();
    }

    @Override
    String transformOutputFilePath(final String outputFilePath) {
        return transformOutputFilePath(outputFilePath, "cif");
    }

    @Override
    String getAtomsStructureString(Structure structure) {
        return getAtomsStructureString(structure, FormatType.CIF);
    }

}

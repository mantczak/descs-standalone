package edu.put.ma.io.writer;

import java.io.File;

import org.biojava.nbio.structure.Structure;

public interface Writer {

    void write(Structure structure, String outputFilePath);

    void write(Structure structure, File outputFile);
    
    void writeAtomsOnly(Structure structure, String outputFilePath);

    void writeAtomsOnly(Structure structure, File outputFile);
    
    void write(String text, File outputFile, String prefix);
}

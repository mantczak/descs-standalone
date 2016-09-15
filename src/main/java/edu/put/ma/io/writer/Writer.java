package edu.put.ma.io.writer;

import java.io.File;

import org.biojava.nbio.structure.Structure;

import edu.put.ma.io.model.Structure3d;

public interface Writer {

    void write(Structure structure, String outputFilePath);

    void write(Structure structure, File outputFile);

    void write(Structure3d structure3d, String outputFilePath);

    void write(Structure3d structure3d, File outputFile);

    void write(String text, File outputFile, String prefix);
}

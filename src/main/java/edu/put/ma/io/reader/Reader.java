package edu.put.ma.io.reader;

import java.io.InputStream;

import org.biojava.nbio.structure.Structure;

public interface Reader {

    Structure read(String inputFilePath);
    
    Structure read(InputStream inStream);
}

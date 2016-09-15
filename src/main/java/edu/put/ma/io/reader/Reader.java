package edu.put.ma.io.reader;

import edu.put.ma.io.model.Structure3d;

public interface Reader {

    Structure3d read(String inputFilePath);
}

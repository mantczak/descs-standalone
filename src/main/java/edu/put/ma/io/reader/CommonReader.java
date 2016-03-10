package edu.put.ma.io.reader;

import java.io.File;

import org.apache.commons.io.FileUtils;

import edu.put.ma.utils.PreconditionUtils;

abstract class CommonReader {
    
    File getFile(final String inputFilePath) {
        PreconditionUtils.checkIfStringIsBlank(inputFilePath, "Input file path");
        final File file = FileUtils.getFile(inputFilePath);
        PreconditionUtils.checkIfFileExistsAndIsNotADirectory(file, "Input");
        return file;
    }
}

package edu.put.ma.io.reader;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdbReader extends CommonReader implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdbReader.class);
    
    private final PDBFileReader reader;

    PdbReader() {
        this.reader = new PDBFileReader(FileUtils.getTempDirectoryPath());
    }

    @Override
    public Structure read(final String inputFilePath) {
        Structure structure = null;
        File file = getFile(inputFilePath);
        try {
            structure = reader.getStructure(file);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return structure;
    }

    @Override
    public Structure read(final InputStream inStream) {
        Structure structure = null;
        try {
            structure = reader.getStructure(inStream);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inStream);
        }
        return structure;
    }

}

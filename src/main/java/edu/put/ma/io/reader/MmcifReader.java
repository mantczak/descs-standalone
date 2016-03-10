package edu.put.ma.io.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.mmcif.MMcifParser;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MmcifReader extends CommonReader implements Reader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MmcifReader.class);

    @Override
    public Structure read(final String inputFilePath) {
        Structure structure = null;
        File file = getFile(inputFilePath);
        try {
            final InputStream inStream = new FileInputStream(file);
            return read(inStream);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return structure;
    }

    @Override
    public Structure read(final InputStream inStream) {
        Structure structure = null;
        try {
            MMcifParser parser = new SimpleMMcifParser();
            SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();
            parser.addMMcifConsumer(consumer);
            parser.parse(inStream);
            structure = consumer.getStructure();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inStream);
        }
        return structure;
    }

}

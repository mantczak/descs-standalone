package edu.put.ma.io.reader;

import static edu.put.ma.io.writer.PdbWriter.MODEL_PREFIX;
import static edu.put.ma.io.writer.PdbWriter.MODEL_RECORD_PATTERN;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.put.ma.io.model.ModelInfo;
import edu.put.ma.io.model.ModelInfoImpl;

public class PdbReader extends CommonReader {

    public static final int MODEL_NO_GROUP_INDEX = 3;

    private static final Logger LOGGER = LoggerFactory.getLogger(PdbReader.class);

    private final PDBFileReader reader;

    PdbReader() {
        this.reader = new PDBFileReader(FileUtils.getTempDirectoryPath());
    }

    @Override
    protected Structure read(final InputStream inStream) {
        Structure structure = null;
        try {
            structure = reader.getStructure(inStream);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return structure;
    }

    @Override
    protected List<ModelInfo> readModelInfo(final List<String> records) {
        final List<ModelInfo> result = Lists.newArrayList();
        for (String record : records) {
            if (StringUtils.startsWith(record, MODEL_PREFIX)) {
                final Matcher modelRecordMatcher = MODEL_RECORD_PATTERN.matcher(record);
                if (modelRecordMatcher.matches()) {
                    result.add(new ModelInfoImpl(Integer.parseInt(modelRecordMatcher
                            .group(MODEL_NO_GROUP_INDEX))));
                }
            }
        }
        if (CollectionUtils.sizeIsEmpty(result)) {
            result.add(new ModelInfoImpl(1));
        }
        return result;
    }

}

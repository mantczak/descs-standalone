package edu.put.ma.io.writer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.put.ma.archiver.ArchiverFactory;
import edu.put.ma.io.model.ModelInfo;
import edu.put.ma.io.model.ModelInfoImpl;
import edu.put.ma.io.model.Structure3d;
import edu.put.ma.io.model.Structure3dFactory;
import edu.put.ma.utils.PreconditionUtils;

public abstract class CommonWriter implements Writer {

    private static final List<ModelInfo> SINGLE_MODEL_INFO = ImmutableList
            .of((ModelInfo) new ModelInfoImpl(1));

    private static final int SUPPORTED_EXTENSION_SIZE = 3;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonWriter.class);

    protected static final boolean IS_ATOMS_ONLY = true;

    @Override
    public void write(final Structure structure, final File outputFile) {
        if (isValid(structure)) {
            write(getStructureString(Structure3dFactory.construct(structure, SINGLE_MODEL_INFO)),
                    outputFile.getAbsolutePath());
        }
    }

    @Override
    public void write(final Structure structure, String outputFilePath) {
        write(structure, new File(outputFilePath));
    }

    @Override
    public void write(final Structure3d structure3d, final File outputFile) {
        write(structure3d, outputFile.getAbsolutePath());
    }

    @Override
    public void write(final String text, final File outputFile, final String prefix) {
        writeText(text, outputFile, prefix);
    }

    public static final String transformOutputFilePath(final String outputFilePath,
            final String expectedExtension) {
        if (!ArchiverFactory.isArchive(outputFilePath)) {
            final String extension = FilenameUtils.getExtension(outputFilePath);
            if ((StringUtils.isBlank(extension))
                    || (StringUtils.length(extension) > SUPPORTED_EXTENSION_SIZE)) {
                return outputFilePath + "." + expectedExtension;
            }
            if (!StringUtils.equalsIgnoreCase(expectedExtension, extension)) {
                final int indexOfExtension = FilenameUtils.indexOfExtension(outputFilePath) + 1;
                final String outputFilePathWithoutExtension = StringUtils.substring(outputFilePath, 0,
                        indexOfExtension);
                return new StringBuilder(outputFilePathWithoutExtension).append(expectedExtension).toString();
            }
        }
        return outputFilePath;
    }

    protected abstract String getStructureString(Structure3d structure3d);

    protected abstract String transformOutputFilePath(String outputFilePath);

    protected boolean isValid(final Structure structure) {
        Preconditions.checkNotNull(structure, "Structure is not initialized properly");
        return true;
    }

    protected void write(final String structureString, final String outputFilePath) {
        PreconditionUtils.checkIfStringIsBlank(outputFilePath, "Output file path");
        write(structureString, new File(transformOutputFilePath(outputFilePath)));
    }

    private void write(final String structureString, final File outputFile) {
        writeText(structureString, outputFile, "Structure");
    }

    private static final void writeText(final String text, final File outputFile, final String prefix) {
        PreconditionUtils.checkIfStringIsBlank(text, prefix);
        PreconditionUtils.checkIfFileNotExistOrExistsAndIsNotADirectory(outputFile, "Output");
        try {
            FileUtils.writeStringToFile(outputFile, text);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}

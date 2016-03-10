package edu.put.ma.io.writer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.put.ma.io.FormatType;
import edu.put.ma.utils.PreconditionUtils;

public abstract class CommonWriter implements Writer {

    private static final int SUPPORTED_EXTENSION_SIZE = 3;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonWriter.class);

    @Override
    public void write(final Structure structure, final String outputFilePath) {
        if (isValid(structure)) {
            write(getStructureString(structure), outputFilePath);
        }
    }

    @Override
    public void write(final Structure structure, final File outputFile) {
        write(structure, outputFile.getAbsolutePath());
    }

    @Override
    public void writeAtomsOnly(final Structure structure, final String outputFilePath) {
        if (isValid(structure)) {
            write(getAtomsStructureString(structure), outputFilePath);
        }
    }

    @Override
    public void writeAtomsOnly(final Structure structure, final File outputFile) {
        writeAtomsOnly(structure, outputFile.getAbsolutePath());
    }

    @Override
    public void write(final String text, final File outputFile, final String prefix) {
        writeText(text, outputFile, prefix);
    }

    public static final String transformOutputFilePath(final String outputFilePath,
            final String expectedExtension) {
        final String extension = FilenameUtils.getExtension(outputFilePath);
        if ((StringUtils.isBlank(extension)) || (StringUtils.length(extension) > SUPPORTED_EXTENSION_SIZE)) {
            return outputFilePath + "." + expectedExtension;
        }
        if (!StringUtils.equalsIgnoreCase(expectedExtension, extension)) {
            final int indexOfExtension = FilenameUtils.indexOfExtension(outputFilePath) + 1;
            final String outputFilePathWithoutExtension = StringUtils.substring(outputFilePath, 0,
                    indexOfExtension);
            return new StringBuilder(outputFilePathWithoutExtension).append(expectedExtension).toString();
        }
        return outputFilePath;
    }

    abstract String getStructureString(Structure structure);

    abstract String getAtomsStructureString(Structure structure);

    abstract String transformOutputFilePath(String outputFilePath);

    static final String getAtomsStructureString(final Structure structure, final FormatType type) {
        final StringBuilder atomsStringBuilder = new StringBuilder();
        final int modelsNo = structure.nrModels();
        for (int modelIndex = 0; modelIndex < modelsNo; modelIndex++) {
            if ((type == FormatType.PDB) && (modelsNo > 1)) {
                atomsStringBuilder.append("MODEL ").append(String.valueOf(modelIndex + 1)).append("\n");
            }
            final List<Chain> model = structure.getModel(modelIndex);
            for (Chain chain : model) {
                if (type == FormatType.PDB) {
                    atomsStringBuilder.append(chain.toPDB());
                    atomsStringBuilder.append("TER").append("\n");
                } else {
                    atomsStringBuilder.append(chain.toMMCIF());
                }
            }
            if ((type == FormatType.PDB) && (modelsNo > 1)) {
                atomsStringBuilder.append("ENDMDL").append("\n");
            }
        }
        return atomsStringBuilder.toString();
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

    private boolean isValid(final Structure structure) {
        Preconditions.checkNotNull(structure, "Structure is not initialized properly");
        return true;
    }

    private void write(final String structureString, final String outputFilePath) {
        PreconditionUtils.checkIfStringIsBlank(outputFilePath, "Output file path");
        write(structureString, new File(transformOutputFilePath(outputFilePath)));
    }

    private void write(final String structureString, final File outputFile) {
        writeText(structureString, outputFile, "Structure");
    }

}

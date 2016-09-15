package edu.put.ma;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junitx.framework.FileAssert;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;

import edu.put.ma.archiver.Archiver;
import edu.put.ma.archiver.ArchiverFactory;
import edu.put.ma.io.FormatType;
import edu.put.ma.model.input.FormatConverterInputModel;
import edu.put.ma.model.input.FormatConverterInputModelImpl;

public class StructuresConversionTest extends CommonTest {

    @Test
    public void testCifToPdbConversionOfLargeProteinStructureConsideringEntityIds() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.CIF, "1vvj.cif", FormatType.PDB, "1vvj.tar.gz");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testPdbToCifConversionOfLargeProteinStructureConsideringEntityIds() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.PDB, "1vvj.tar.gz", FormatType.CIF, "1vvj.cif");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testPdbToCifConversionOfLargeProteinStructure() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.PDB, "1vvj2.tar.gz", FormatType.CIF, "1vvj2.cif");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testCifToPdbConversionOfLargeMultipleModelProteinStructure() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.CIF, "1vvj3.cif", FormatType.PDB, "1vvj3.tar.gz");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testPdbToCifConversionOfLargeMultipleModelProteinStructure() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.PDB, "1vvj3.tar.gz", FormatType.CIF, "1vvj3.cif");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testPdbToCifConversionOfSingleModelAndSingleChainStructure() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.PDB, "d2b97a1.pdb", FormatType.CIF, "d2b97a1.cif");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testCifToPdbConversionOfSingleModelAndSingleChainStructure() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.CIF, "d2b97a1.cif", FormatType.PDB, "d2b97a1.pdb");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testPdbToCifConversionOfSingleModelAndMultipleChainStructure() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.PDB, "1guc-1.pdb", FormatType.CIF, "1guc-1.cif");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testCifToPdbConversionOfSingleModelAndMultipleChainStructure() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.CIF, "1guc-1.cif", FormatType.PDB, "1guc-1.pdb");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testPdbToCifConversionOfSingleModelAndMultipleChainStructureConsideringEntityIds()
            throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.PDB, "1guc.tar.gz", FormatType.CIF, "1guc.cif");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testCifToPdbConversionOfSingleModelAndMultipleChainStructureConsideringEntityIds()
            throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.CIF, "1guc.cif", FormatType.PDB, "1guc.tar.gz");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testPdbToCifConversionOfMultipleModelAndSingleChainStructure() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.PDB, "2lbk-3.pdb", FormatType.CIF, "2lbk-3.cif");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testCifToPdbConversionOfMultipleModelAndSingleChainStructure() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.CIF, "2lbk-3.cif", FormatType.PDB, "2lbk-3.pdb");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testPdbToCifConversionOfMultipleModelAndMultipleChainStructure() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.PDB, "1guc-3.pdb", FormatType.CIF, "1guc-3.cif");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testCifToPdbConversionOfMultipleModelAndMultipleChainStructure() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.CIF, "1guc-3.cif", FormatType.PDB, "1guc-3.pdb");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testPdbToCifConversionOfMultipleModelAndMultipleChainStructureAssumingNotAdjacentModelNumbers()
            throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.PDB, "1guc-2.pdb", FormatType.CIF, "1guc-2.cif");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testCifToPdbConversionOfMultipleModelAndMultipleChainStructureAssumingNotAdjacentModelNumbers()
            throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.CIF, "1guc-2.cif", FormatType.PDB, "1guc-2.pdb");
        convert(formatConverterInputModelBuilder);
    }

    private void convert(final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder)
            throws Exception {
        final Class<?> clazz = this.getClass();
        final String executionModeString = ExecutionMode.FORMAT_CONVERSION.toString();
        final File inputFile = getFile(clazz, "./", executionModeString, "/",
                formatConverterInputModelBuilder.getInputFilePath());
        formatConverterInputModelBuilder.inputFilePath(inputFile.getCanonicalPath());
        final String outputFileName = formatConverterInputModelBuilder.getOutputFilePath();
        final File outputFile = temporaryFolder.newFile(outputFileName);
        formatConverterInputModelBuilder.outputFilePath(outputFile.getCanonicalPath());
        final File expectedFile = getFile(clazz, "./", executionModeString, "/", outputFileName);
        final FormatConverterInputModel formatConverterInputModel = formatConverterInputModelBuilder.build();
        final String[] args = formatConverterInputModel.getArgs();
        app.execute(args);
        if (ArchiverFactory.isArchive(expectedFile)) {
            assertArchiveEquals(outputFile, expectedFile);
        } else {
            FileAssert.assertEquals(expectedFile, outputFile);
        }
    }

    private void assertArchiveEquals(final File outputFile, final File expectedFile) throws IOException {
        final Archiver archiver = ArchiverFactory.getArchiver(expectedFile);
        final File expectedFolder = temporaryFolder.newFolder();
        archiver.extract(expectedFile, expectedFolder);
        final File outputFolder = temporaryFolder.newFolder();
        archiver.extract(outputFile, outputFolder);
        final File[] expectedFolderFiles = expectedFolder.listFiles();
        final File[] outputFolderFiles = outputFolder.listFiles();
        final int expectedFolderFilesNo = ArrayUtils.getLength(expectedFolderFiles);
        final int outputFolderFilesNo = ArrayUtils.getLength(outputFolderFiles);
        Assert.assertEquals(expectedFolderFilesNo, outputFolderFilesNo);
        final Map<String, File> conversionResultFiles = Maps.newHashMap();
        for (File conversionResultFile : outputFolderFiles) {
            conversionResultFiles.put(conversionResultFile.getName(), conversionResultFile);
        }
        for (File file : expectedFolder.listFiles()) {
            FileAssert.assertEquals(file, conversionResultFiles.get(file.getName()));
        }
    }
}

package edu.put.ma;

import java.io.File;

import junitx.framework.FileAssert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import edu.put.ma.io.FormatType;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.model.input.DescriptorsBuilderInputModel;
import edu.put.ma.model.input.DescriptorsBuilderInputModelImpl;

public class DescriptorBuildingTest extends CommonTest {

    @Test
    public void testProteinDescriptorsBuilding() throws Exception {
        final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder = new DescriptorsBuilderInputModelImpl.Builder()
                .inputFormat(FormatType.PDB).inputFilePath("d2b97a1.pdb").moleculeType(MoleculeType.PROTEIN)
                .outputFormat(FormatType.PDB).minimalElementsCount(10);
        build("1", "in-contact-residues-proteins-scgc.exp", descriptorsBuilderInputModelBuilder,
                new String[] { "d2b97a1_A_39_PHE.pdb",
                        "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    @Test
    public void testProteinDescriptorsBuilding2() throws Exception {
        final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder = new DescriptorsBuilderInputModelImpl.Builder()
                .inputFormat(FormatType.PDB).inputFilePath("d2b97a1.pdb").moleculeType(MoleculeType.PROTEIN)
                .outputFormat(FormatType.CIF).minimalElementsCount(10);
        build("2", "in-contact-residues-proteins-scgc.exp", descriptorsBuilderInputModelBuilder,
                new String[] { "d2b97a1_A_39_PHE.cif",
                        "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    @Test
    public void testProteinDescriptorsBuilding3() throws Exception {
        final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder = new DescriptorsBuilderInputModelImpl.Builder()
                .inputFormat(FormatType.CIF).inputFilePath("d2b97a1.cif").moleculeType(MoleculeType.PROTEIN)
                .outputFormat(FormatType.CIF).minimalElementsCount(10);
        build("3", "in-contact-residues-proteins-scgc.exp", descriptorsBuilderInputModelBuilder,
                new String[] { "d2b97a1_A_39_PHE.cif",
                        "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    @Test
    public void testProteinDescriptorsBuilding4() throws Exception {
        final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder = new DescriptorsBuilderInputModelImpl.Builder()
                .inputFormat(FormatType.CIF).inputFilePath("d2b97a1.cif").moleculeType(MoleculeType.PROTEIN)
                .outputFormat(FormatType.PDB).minimalElementsCount(10);
        build("4", "in-contact-residues-proteins-scgc.exp", descriptorsBuilderInputModelBuilder,
                new String[] { "d2b97a1_A_39_PHE.pdb",
                        "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    @Test
    public void testProteinDescriptorsBuilding5() throws Exception {
        final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder = new DescriptorsBuilderInputModelImpl.Builder()
                .inputFormat(FormatType.PDB).inputFilePath("d2b97a1.pdb").moleculeType(MoleculeType.PROTEIN)
                .outputFormat(FormatType.PDB).minimalElementsCount(10);
        build("5", "in-contact-residues-proteins-cbx.exp", descriptorsBuilderInputModelBuilder, new String[] {
                "d2b97a1_A_51_LEU.pdb",
                "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    @Test
    public void testProteinDescriptorsBuilding6() throws Exception {
        final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder = new DescriptorsBuilderInputModelImpl.Builder()
                .inputFormat(FormatType.PDB).inputFilePath("d2b97a1.pdb").moleculeType(MoleculeType.PROTEIN)
                .outputFormat(FormatType.PDB).elementSize(7).minimalElementsCount(9);
        build("7", "in-contact-residues-proteins-scgc.exp", descriptorsBuilderInputModelBuilder,
                new String[] { "d2b97a1_A_39_PHE.pdb", "d2b97a1_A_51_LEU.pdb",
                        "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    @Test
    public void testRNADescriptorsBuilding() throws Exception {
        final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder = new DescriptorsBuilderInputModelImpl.Builder()
                .inputFormat(FormatType.PDB).inputFilePath("430d.pdb").moleculeType(MoleculeType.RNA)
                .outputFormat(FormatType.PDB).minimalElementsCount(5);
        build("6", "in-contact-residues-rnas.exp", descriptorsBuilderInputModelBuilder, new String[] {
                "430d_A_4_U.pdb", "430d_A_5_G.pdb", "430d_A_6_C.pdb", "430d_A_7_U.pdb", "430d_A_8_C.pdb",
                "430d_A_9_A.pdb", "430d_A_10_G.pdb", "430d_A_11_U.pdb", "430d_A_12_A.pdb", "430d_A_20_A.pdb",
                "430d_A_21_A.pdb", "430d_A_22_C.pdb", "430d_A_23_C.pdb", "430d_A_24_G.pdb",
                "430d_A_25_C.pdb", "430d_A_26_A.pdb",
                "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    @Test
    public void testRNAMultiChainDescriptorsBuilding() throws Exception {
        final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder = new DescriptorsBuilderInputModelImpl.Builder()
                .inputFormat(FormatType.PDB).inputFilePath("4u4o-extracted.pdb")
                .moleculeType(MoleculeType.RNA).outputFormat(FormatType.PDB).minimalElementsCount(9)
                .minimalSegmentsCount(5);
        build("8", "in-contact-residues-rnas.exp", descriptorsBuilderInputModelBuilder, new String[] {
                "4u4o-extracted_A_1156_C.pdb", "4u4o-extracted_B_86_U.pdb",
                "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    @Test
    public void testRNAMultiChainDescriptorsBuildingInCIF() throws Exception {
        final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder = new DescriptorsBuilderInputModelImpl.Builder()
                .inputFormat(FormatType.PDB).inputFilePath("4u4o-extracted.pdb")
                .moleculeType(MoleculeType.RNA).outputFormat(FormatType.CIF).minimalElementsCount(9)
                .minimalSegmentsCount(5);
        build("9", "in-contact-residues-rnas.exp", descriptorsBuilderInputModelBuilder, new String[] {
                "4u4o-extracted_A_1156_C.cif", "4u4o-extracted_B_86_U.cif",
                "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    @Test
    public void testRNAMultiChainDescriptorsBuildingWithinMultiModelStructureFromPdbToCif() throws Exception {
        final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder = new DescriptorsBuilderInputModelImpl.Builder()
                .inputFormat(FormatType.PDB).inputFilePath("1guc-3.pdb").moleculeType(MoleculeType.RNA)
                .outputFormat(FormatType.CIF).minimalResiduesCount(16);
        build("10", "in-contact-residues-rnas.exp", descriptorsBuilderInputModelBuilder, new String[] {
                "1guc-3_1_A_5_U.cif", "1guc-3_1_B_13_U.cif", "1guc-3_2_A_5_U.cif", "1guc-3_2_B_13_U.cif",
                "1guc-3_3_A_5_U.cif", "1guc-3_3_B_13_U.cif",
                "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    @Test
    public void testRNAMultiChainDescriptorsBuildingWithinMultiModelStructureFromCifToPdb() throws Exception {
        final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder = new DescriptorsBuilderInputModelImpl.Builder()
                .inputFormat(FormatType.CIF).inputFilePath("1guc-3.cif").moleculeType(MoleculeType.RNA)
                .outputFormat(FormatType.PDB).minimalResiduesCount(16);
        build("11", "in-contact-residues-rnas.exp", descriptorsBuilderInputModelBuilder, new String[] {
                "1guc-3_1_A_5_U.pdb", "1guc-3_1_B_13_U.pdb", "1guc-3_2_A_5_U.pdb", "1guc-3_2_B_13_U.pdb",
                "1guc-3_3_A_5_U.pdb", "1guc-3_3_B_13_U.pdb",
                "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    private void build(final String dataPackageNo, final String inContactResiduesExpressionFileName,
            final DescriptorsBuilderInputModelImpl.Builder descriptorsBuilderInputModelBuilder,
            final String[] outputFileNames) throws Exception {
        final Class<?> clazz = this.getClass();
        final String executionModeString = ExecutionMode.DESCRIPTORS_BUILDING.toString();
        final File inputFile = getFile(clazz, "./", executionModeString, "/", dataPackageNo, "/",
                descriptorsBuilderInputModelBuilder.getInputFilePath());
        descriptorsBuilderInputModelBuilder.inputFilePath(inputFile.getCanonicalPath());
        final File inContactResiduesExpressionFile = getFile(clazz, "./", executionModeString, "/",
                dataPackageNo, "/", inContactResiduesExpressionFileName);
        final File outputFolder = temporaryFolder.newFolder();
        descriptorsBuilderInputModelBuilder.outputDirPath(outputFolder.getCanonicalPath());
        final DescriptorsBuilderInputModel descriptorsBuilderInputModel = descriptorsBuilderInputModelBuilder
                .build();
        final String[] args = descriptorsBuilderInputModel.getArgs();
        int argsCount = ArrayUtils.getLength(args);
        final String[] extendedArgs = new String[argsCount + 2];
        System.arraycopy(args, 0, extendedArgs, 0, argsCount);
        argsCount = introduceArg(extendedArgs, true, "-ice",
                inContactResiduesExpressionFile.getCanonicalPath(), argsCount);
        final int outputFileNamesCount = ArrayUtils.getLength(outputFileNames);
        final File[] expectedFiles = new File[outputFileNamesCount];
        for (int outputFileNameIndex = 0; outputFileNameIndex < outputFileNamesCount; outputFileNameIndex++) {
            expectedFiles[outputFileNameIndex] = getFile(clazz, "./", executionModeString, "/",
                    dataPackageNo, "/expected/", outputFileNames[outputFileNameIndex]);
        }
        app.execute(extendedArgs);
        for (int outputFileNameIndex = 0; outputFileNameIndex < outputFileNamesCount; outputFileNameIndex++) {
            FileAssert.assertEquals(expectedFiles[outputFileNameIndex],
                    FileUtils.getFile(outputFolder, outputFileNames[outputFileNameIndex]));
        }
    }

    private static final int introduceArg(final String[] args, final boolean isArgConsidered,
            final String option, final String value, final int argsCount) {
        if (isArgConsidered) {
            System.arraycopy(new String[] { option, value }, 0, args, argsCount, 2);
            return argsCount + 2;
        }
        return argsCount;
    }

}

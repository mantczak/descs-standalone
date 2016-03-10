package edu.put.ma;

import java.io.File;

import junitx.framework.FileAssert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.put.ma.descs.AlignmentMode;
import edu.put.ma.descs.UncomparableDescriptorsException;
import edu.put.ma.descs.algorithms.ComparisonAlgorithms;
import edu.put.ma.io.FormatType;
import edu.put.ma.io.writer.CommonWriter;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.model.input.DescriptorsBuilderInputModel;
import edu.put.ma.model.input.DescriptorsBuilderInputModelImpl;
import edu.put.ma.model.input.DescriptorsComparatorInputModel;
import edu.put.ma.model.input.DescriptorsComparatorInputModelImpl;
import edu.put.ma.model.input.FormatConverterInputModel;
import edu.put.ma.model.input.FormatConverterInputModelImpl;

public class AppTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private App app;

    @Before
    public void setUp() {
        app = new App();
    }

    @Test
    public void testPdbToCifConversion() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.PDB, "d2b97a1.pdb", FormatType.CIF, "d2b97a1.cif");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testCifToPdbConversion() throws Exception {
        final FormatConverterInputModelImpl.Builder formatConverterInputModelBuilder = new FormatConverterInputModelImpl.Builder(
                FormatType.CIF, "d2b97a1.cif", FormatType.PDB, "d2b97a1.pdb");
        convert(formatConverterInputModelBuilder);
    }

    @Test
    public void testComparisonOfSingleElementProteinDescriptors() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.pdb")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.pdb")
                .comparisonAlgorithmType(ComparisonAlgorithms.BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER);
        compare("1", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors1() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.pdb")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.pdb")
                .comparisonAlgorithmType(ComparisonAlgorithms.BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER);
        compare("2", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors2() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.pdb")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.pdb")
                .comparisonAlgorithmType(ComparisonAlgorithms.BACKTRACKING_DRIVEN_FIRST_ALIGNMENT_ONLY)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER);
        compare("3", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors3() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1u78a2_A_69_VAL.pdb")
                .secondDescriptorFilePath("d2iw5b1_B_391_ALA.pdb")
                .comparisonAlgorithmType(
                        ComparisonAlgorithms.HUNGARIAN_METHOD_DRIVEN_FIRST_ALIGNMENT_ONLY_PARTIAL_SOLUTIONS_NOT_CONSIDERED)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalRmsdThresholdPerDuplexPair(1.75);
        compare("5", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors4() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.pdb")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.pdb")
                .comparisonAlgorithmType(
                        ComparisonAlgorithms.HUNGARIAN_METHOD_DRIVEN_FIRST_ALIGNMENT_ONLY_PARTIAL_SOLUTIONS_NOT_CONSIDERED)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalRmsdThresholdPerDuplexPair(2.0);
        compare("3", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors5() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.pdb")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.pdb")
                .comparisonAlgorithmType(
                        ComparisonAlgorithms.HUNGARIAN_METHOD_DRIVEN_FIRST_ALIGNMENT_ONLY_PARTIAL_SOLUTIONS_NOT_CONSIDERED)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalRmsdThresholdPerDuplexPair(2.33);
        compare("2", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors6() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1u78a2_A_69_VAL.pdb")
                .secondDescriptorFilePath("d2iw5b1_B_391_ALA.pdb")
                .comparisonAlgorithmType(
                        ComparisonAlgorithms.HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_NOT_CONSIDERED)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalRmsdThresholdPerDuplexPair(1.75);
        compare("5", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors7() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.pdb")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.pdb")
                .comparisonAlgorithmType(
                        ComparisonAlgorithms.HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_NOT_CONSIDERED)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalRmsdThresholdPerDuplexPair(2.0);
        compare("3", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors8() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.pdb")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.pdb")
                .comparisonAlgorithmType(
                        ComparisonAlgorithms.HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_NOT_CONSIDERED)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalRmsdThresholdPerDuplexPair(2.33);
        compare("2", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors9() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.pdb")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.pdb")
                .comparisonAlgorithmType(
                        ComparisonAlgorithms.HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_CONSIDERED)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalRmsdThresholdPerDuplexPair(1.75);
        compare("4", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors10() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.pdb")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.pdb")
                .comparisonAlgorithmType(
                        ComparisonAlgorithms.HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_CONSIDERED)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalRmsdThresholdPerDuplexPair(2.0);
        compare("3", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors11() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.pdb")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.pdb")
                .comparisonAlgorithmType(
                        ComparisonAlgorithms.HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_CONSIDERED)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalRmsdThresholdPerDuplexPair(2.33);
        compare("3", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors12() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.pdb")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.pdb")
                .comparisonAlgorithmType(ComparisonAlgorithms.BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).inputFormat(FormatType.PDB)
                .outputFormat(FormatType.CIF);
        compare("6", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors13() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1g1ta2_A_140_TYR.cif")
                .secondDescriptorFilePath("d1uzka1_A_1509_TYR.cif")
                .comparisonAlgorithmType(ComparisonAlgorithms.BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).inputFormat(FormatType.CIF)
                .outputFormat(FormatType.PDB);
        compare("7", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors14() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d2b97a1_A_7_LEU.pdb")
                .secondDescriptorFilePath("d2b97a1_A_8_PHE.pdb")
                .comparisonAlgorithmType(ComparisonAlgorithms.BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalOriginElementsPairAlignmentRmsd(3.6);
        compare("9", descriptorsComparatorInputModelBuilder);
    }

    @Test(expected = UncomparableDescriptorsException.class)
    public void testComparisonOfMultiSegmentProteinDescriptors15() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d2b97a1_A_7_LEU.pdb")
                .secondDescriptorFilePath("d2b97a1_A_8_PHE.pdb")
                .comparisonAlgorithmType(ComparisonAlgorithms.BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalOriginElementsPairAlignmentRmsd(3.6);
        compare("10", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentRNADescriptors() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.RNA)
                .firstDescriptorFilePath("430d_A_7_U.pdb")
                .secondDescriptorFilePath("430d_A_8_C.pdb")
                .comparisonAlgorithmType(ComparisonAlgorithms.BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER).maximalOriginElementsPairAlignmentRmsd(2.5)
                .maximalDuplexesPairAlignmentRmsd(4.0);
        compare("8", descriptorsComparatorInputModelBuilder);
    }

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
                "430d_A_7_U.pdb", "430d_A_8_C.pdb", "430d_A_9_A.pdb", "430d_A_10_G.pdb", "430d_A_11_U.pdb",
                "430d_A_12_A.pdb", "430d_A_20_A.pdb", "430d_A_21_A.pdb", "430d_A_22_C.pdb",
                "430d_A_23_C.pdb",
                "atom-names-considered-by-in-contact-residues-identification-expression.list" });
    }

    private void compare(final String dataPackageNo,
            final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder)
            throws Exception {
        final Class<?> clazz = this.getClass();
        final String executionModeString = ExecutionMode.DESCRIPTORS_COMPARISON.toString();
        final String firstDescriptorFileName = descriptorsComparatorInputModelBuilder
                .getFirstDescriptorFilePath();
        final String secondDescriptorFileName = descriptorsComparatorInputModelBuilder
                .getSecondDescriptorFilePath();
        final File firstDescriptorFile = getFile(clazz, "./", executionModeString, "/", dataPackageNo, "/",
                firstDescriptorFileName);
        descriptorsComparatorInputModelBuilder
                .firstDescriptorFilePath(firstDescriptorFile.getCanonicalPath());
        final File secondDescriptorFile = getFile(clazz, "./", executionModeString, "/", dataPackageNo, "/",
                secondDescriptorFileName);
        descriptorsComparatorInputModelBuilder.secondDescriptorFilePath(secondDescriptorFile
                .getCanonicalPath());
        final File atomNamesFile = getFile(clazz, "./", executionModeString, "/", dataPackageNo, "/",
                descriptorsComparatorInputModelBuilder.getAlignmentAtomNamesFilePath());
        descriptorsComparatorInputModelBuilder.alignmentAtomNamesFilePath(atomNamesFile.getCanonicalPath());
        final File outputFolder = temporaryFolder.newFolder();
        descriptorsComparatorInputModelBuilder.outputDirPath(outputFolder.getCanonicalPath());
        final FormatType inputFormat = descriptorsComparatorInputModelBuilder.getInputFormat();
        final FormatType outputFormat = descriptorsComparatorInputModelBuilder.getOutputFormat();
        final DescriptorsComparatorInputModel descriptorsComparatorInputModel = descriptorsComparatorInputModelBuilder
                .build();
        final String[] args = descriptorsComparatorInputModel.getArgs();
        final String newFirstDescriptorFileName = (outputFormat == inputFormat) ? firstDescriptorFileName
                : CommonWriter.transformOutputFilePath(firstDescriptorFileName,
                        StringUtils.lowerCase(outputFormat.toString()));
        final String newSecondDescriptorFileName = (outputFormat == inputFormat) ? secondDescriptorFileName
                : CommonWriter.transformOutputFilePath(secondDescriptorFileName,
                        StringUtils.lowerCase(outputFormat.toString()));
        final File expectedFirstDescriptorFile = getFile(clazz, "./", executionModeString, "/",
                dataPackageNo, "/expected/", newFirstDescriptorFileName);
        final File expectedSecondDescriptorFile = getFile(clazz, "./", executionModeString, "/",
                dataPackageNo, "/expected/", newSecondDescriptorFileName);
        app.execute(args);
        final File outputFirstDescriptorFile = FileUtils.getFile(outputFolder, newFirstDescriptorFileName);
        final File outputSecondDescriptorFile = FileUtils.getFile(outputFolder, newSecondDescriptorFileName);
        FileAssert.assertEquals(expectedFirstDescriptorFile, outputFirstDescriptorFile);
        FileAssert.assertEquals(expectedSecondDescriptorFile, outputSecondDescriptorFile);
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
        FileAssert.assertEquals(expectedFile, outputFile);
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

    private static final File getFile(final Class<?> clazz, final String... options) {
        final StringBuilder path = new StringBuilder();
        final int optionsCount = ArrayUtils.getLength(options);
        for (int optionIndex = 0; optionIndex < optionsCount; optionIndex++) {
            path.append(options[optionIndex]);
            if (optionIndex < optionsCount - 1) {
                path.append("/");
            }
        }
        return FileUtils.toFile(clazz.getResource(path.toString()));
    }
}

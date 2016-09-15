package edu.put.ma;

import java.io.File;

import junitx.framework.FileAssert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import edu.put.ma.descs.AlignmentMode;
import edu.put.ma.descs.UncomparableDescriptorsException;
import edu.put.ma.descs.algorithms.AlignmentAcceptanceMode;
import edu.put.ma.descs.algorithms.ComparisonAlgorithms;
import edu.put.ma.io.FormatType;
import edu.put.ma.io.writer.CommonWriter;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.model.input.DescriptorsComparatorInputModel;
import edu.put.ma.model.input.DescriptorsComparatorInputModelImpl;

public class DescriptorsComparisonTest extends CommonTest {

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
        compare("2", descriptorsComparatorInputModelBuilder);
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
        compare("4", descriptorsComparatorInputModelBuilder);
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
        compare("4", descriptorsComparatorInputModelBuilder);
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
        compare("2", descriptorsComparatorInputModelBuilder);
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
        compare("2", descriptorsComparatorInputModelBuilder);
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
        compare("2", descriptorsComparatorInputModelBuilder);
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
        compare("5", descriptorsComparatorInputModelBuilder);
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
        compare("6", descriptorsComparatorInputModelBuilder);
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
        compare("8", descriptorsComparatorInputModelBuilder);
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
        compare("9", descriptorsComparatorInputModelBuilder);
    }

    @Test
    public void testComparisonOfMultiSegmentProteinDescriptors16() throws Exception {
        final DescriptorsComparatorInputModelImpl.Builder descriptorsComparatorInputModelBuilder = new DescriptorsComparatorInputModelImpl.Builder()
                .moleculeType(MoleculeType.PROTEIN)
                .firstDescriptorFilePath("d1quba4_A_213_PHE.pdb")
                .secondDescriptorFilePath("d1ppqa__A_988_TYR.pdb")
                .comparisonAlgorithmType(ComparisonAlgorithms.BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT)
                .alignmentAtomNamesFilePath(
                        "atom-names-considered-by-in-contact-residues-identification-expression.list")
                .alignmentMode(AlignmentMode.CONSIDER)
                .alignmentAcceptanceMode(AlignmentAcceptanceMode.ALIGNED_RESIDUES_ONLY);
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
        compare("7", descriptorsComparatorInputModelBuilder);
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

}

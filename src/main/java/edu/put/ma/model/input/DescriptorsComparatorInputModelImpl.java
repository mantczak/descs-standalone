package edu.put.ma.model.input;

import lombok.Getter;

import org.apache.commons.cli.Options;

import com.google.common.collect.ImmutableMap;

import edu.put.ma.descs.AlignmentMode;
import edu.put.ma.descs.SimilarDescriptorsVerifier;
import edu.put.ma.descs.SimilarDescriptorsVerifierImpl;
import edu.put.ma.descs.algorithms.ComparisonAlgorithm;
import edu.put.ma.descs.algorithms.ComparisonAlgorithms;
import edu.put.ma.io.FormatType;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.utils.ArrayUtils;
import edu.put.ma.utils.PreconditionUtils;
import edu.put.ma.utils.ResidueUtils;
import static edu.put.ma.utils.PreconditionUtils.MAXIMAL_PERCENTAGE_VALUE;

@Getter
public class DescriptorsComparatorInputModelImpl extends CommonInputModelImpl implements
        DescriptorsComparatorInputModel {

    public static final double DEFAULT_MAXIMAL_RMSD_BASED_COST_OF_PAIR_OF_ALIGNED_DUPLEXES = 2.33;

    private String firstDescriptorFilePath;

    private MoleculeType moleculeType;

    private String secondDescriptorFilePath;

    private String alignmentAtomNamesFilePath;

    private ComparisonAlgorithms comparisonAlgorithmType;

    private SimilarDescriptorsVerifier similarDescriptorsVerifier;

    private double maximalRmsdThresholdPerDuplexPair;

    private AlignmentMode alignmentMode;

    private String outputDirPath;

    public DescriptorsComparatorInputModelImpl(final String[] args, final String artifactId) {
        super(args);
        secureInitState(artifactId);
    }

    public DescriptorsComparatorInputModelImpl(final Builder descriptorsComparatorInputModelBuilder) {
        super(descriptorsComparatorInputModelBuilder.inputFormat,
                descriptorsComparatorInputModelBuilder.outputFormat);
        this.firstDescriptorFilePath = descriptorsComparatorInputModelBuilder.firstDescriptorFilePath;
        this.moleculeType = descriptorsComparatorInputModelBuilder.moleculeType;
        this.secondDescriptorFilePath = descriptorsComparatorInputModelBuilder.secondDescriptorFilePath;
        this.alignmentAtomNamesFilePath = descriptorsComparatorInputModelBuilder.alignmentAtomNamesFilePath;
        this.comparisonAlgorithmType = descriptorsComparatorInputModelBuilder.comparisonAlgorithmType;
        this.similarDescriptorsVerifier = descriptorsComparatorInputModelBuilder.similarDescriptorsVerifierBuilder
                .build();
        this.maximalRmsdThresholdPerDuplexPair = descriptorsComparatorInputModelBuilder.maximalRmsdThresholdPerDuplexPair;
        this.alignmentMode = descriptorsComparatorInputModelBuilder.alignmentMode;
        this.outputDirPath = descriptorsComparatorInputModelBuilder.outputDirPath;
        initOptionsMapping();
    }

    @Override
    public boolean isInputInitializedProperly() {
        return isInputFilesInitializedProperly() && (isCommandLineHasOption("cat"))
                && (isCommandLineHasOption("mt"));
    }

    @Override
    public Options constructSpecificOptions() {
        final String formatTypePostfix = ArrayUtils.getEnumNamesString(FormatType.class) + " [default="
                + DEFAULT_FORMAT + "]";
        final Options options = new Options();
        options.addOption("mt", "molecule-type", true,
                "provided molecule types: " + ArrayUtils.getEnumNamesString(MoleculeType.class));
        options.addOption("fd", "file-path-of-first-descriptor", true, "file path of first descriptor");
        options.addOption("sd", "file-path-of-second-descriptor", true, "file path of second descriptor");
        options.addOption("aan", "file-path-of-atom-names-used-during-alignment-building", true,
                "file path of atom names considered during alignment building");
        options.addOption("cat", "comparison-algorithm-type", true, "provided comparison algorithm types: "
                + ArrayUtils.getEnumNamesString(ComparisonAlgorithms.class));
        options.addOption("od", "output-directory", true, "output directory path");
        options.addOption("if", "input-format", true, "(optional) provided file formats: "
                + formatTypePostfix);
        options.addOption("of", "output-format", true, "(optional) provided file formats: "
                + formatTypePostfix);
        options.addOption("moeparmsd", "maximal-rmsd-of-central-elements-alignment", true,
                "(optional) maximal RMSD of the central elements alignment [default=1.2A]");
        options.addOption("mdparmsd", "maximal-rmsd-of-pair-of-aligned-duplexes", true,
                "(optional) maximal RMSD of a pair of aligned duplexes [default=3.5A]");
        options.addOption("maep", "minimal-fraction-of-aligned-elements", true,
                "(optional) minimal fraction of aligned elements [default=4/5]");
        options.addOption("marp", "minimal-fraction-of-aligned-residues", true,
                "(optional) minimal fraction of aligned residues [default=2/3]");
        options.addOption("magrmsd", "maximal-rmsd-of-total-alignment", true,
                "(optional) maximal RMSD of the total alignment [default=3.5A]");
        options.addOption("mrmsdtpdp", "maximal-cost-of-pair-of-aligned-duplexes", true,
                "(optional) maximal RMSD-based cost of a pair of aligned duplexes [default=2.33A]");
        options.addOption(
                "wa",
                "with-alignment",
                true,
                "(optional) a result of the comparison can be complemented with 3D structures of aligned descriptors, provided modes: "
                        + ArrayUtils.getEnumNamesString(AlignmentMode.class)
                        + " [default="
                        + AlignmentMode.IGNORE + "]");
        return options;
    }

    @Override
    public ComparisonAlgorithm getComparisonAlgorithm() {
        return comparisonAlgorithmType.getComparisonAlgorithm();
    }

    @Override
    protected void initOptionsMapping() {
        super.initOptionsMapping();
        this.optionsMapping.putAll(new ImmutableMap.Builder<String, String>()
                .put("firstDescriptorFilePath", "-fd").put("moleculeType", "-mt")
                .put("secondDescriptorFilePath", "-sd").put("alignmentAtomNamesFilePath", "-aan")
                .put("comparisonAlgorithmType", "-cat")
                .put("maximalRmsdThresholdPerDuplexPair", "-mrmsdtpdp").put("alignmentMode", "-wa")
                .put("outputDirPath", "-od").put("maximalOriginElementsPairAlignmentRmsd", "-moeparmsd")
                .put("maximalDuplexesPairAlignmentRmsd", "-mdparmsd")
                .put("minimalAlignedElementsPercentage", "-maep")
                .put("minimalAlignedResiduesPercentage", "-marp")
                .put("maximalAlignmentGlobalRmsd", "-magrmsd").build());
    }

    @Override
    protected void initState() {
        super.initState();
        setMoleculeType();
        setFirstDescriptorFilePath();
        setSecondDescriptorFilePath();
        setAlignmentAtomNamesFilePath();
        setComparisonAlgorithmType();
        setSimilarDescriptorsVerifier();
        setAlignmentMode();
        setOutputDirPath();
        setMaximalRmsdThresholdPerDuplexPair();
        initInputModelString();
    }

    public static class Builder extends CommonInputModelImpl.Builder {

        @Getter
        private String firstDescriptorFilePath;

        private MoleculeType moleculeType;

        @Getter
        private String secondDescriptorFilePath;

        @Getter
        private String alignmentAtomNamesFilePath;

        private ComparisonAlgorithms comparisonAlgorithmType;

        private SimilarDescriptorsVerifierImpl.Builder similarDescriptorsVerifierBuilder;

        private double maximalRmsdThresholdPerDuplexPair;

        private AlignmentMode alignmentMode;

        private String outputDirPath;

        public Builder() {
            similarDescriptorsVerifierBuilder = new SimilarDescriptorsVerifierImpl.Builder();
        }

        public Builder inputFormat(final FormatType inputFormat) {
            this.inputFormat = inputFormat;
            return this;
        }

        public Builder firstDescriptorFilePath(final String firstDescriptorFilePath) {
            this.firstDescriptorFilePath = firstDescriptorFilePath;
            return this;
        }

        public Builder moleculeType(final MoleculeType moleculeType) {
            this.moleculeType = moleculeType;
            return this;
        }

        public Builder secondDescriptorFilePath(final String secondDescriptorFilePath) {
            this.secondDescriptorFilePath = secondDescriptorFilePath;
            return this;
        }

        public Builder alignmentAtomNamesFilePath(final String alignmentAtomNamesFilePath) {
            this.alignmentAtomNamesFilePath = alignmentAtomNamesFilePath;
            return this;
        }

        public Builder comparisonAlgorithmType(final ComparisonAlgorithms comparisonAlgorithmType) {
            this.comparisonAlgorithmType = comparisonAlgorithmType;
            return this;
        }

        public Builder maximalOriginElementsPairAlignmentRmsd(
                final double maximalOriginElementsPairAlignmentRmsd) {
            this.similarDescriptorsVerifierBuilder = similarDescriptorsVerifierBuilder
                    .maximalOriginElementsPairAlignmentRmsd(maximalOriginElementsPairAlignmentRmsd);
            return this;
        }

        public Builder maximalDuplexesPairAlignmentRmsd(final double maximalDuplexesPairAlignmentRmsd) {
            this.similarDescriptorsVerifierBuilder = similarDescriptorsVerifierBuilder
                    .maximalDuplexesPairAlignmentRmsd(maximalDuplexesPairAlignmentRmsd);
            return this;
        }

        public Builder minimalAlignedElementsPercentage(final double minimalAlignedElementsPercentage) {
            this.similarDescriptorsVerifierBuilder = similarDescriptorsVerifierBuilder
                    .minimalAlignedElementsPercentage(minimalAlignedElementsPercentage);
            return this;
        }

        public Builder minimalAlignedResiduesPercentage(final double minimalAlignedResiduesPercentage) {
            this.similarDescriptorsVerifierBuilder = similarDescriptorsVerifierBuilder
                    .minimalAlignedResiduesPercentage(minimalAlignedResiduesPercentage);
            return this;
        }

        public Builder maximalAlignmentGlobalRmsd(final double maximalAlignmentGlobalRmsd) {
            this.similarDescriptorsVerifierBuilder = similarDescriptorsVerifierBuilder
                    .maximalAlignmentGlobalRmsd(maximalAlignmentGlobalRmsd);
            return this;
        }

        public Builder maximalRmsdThresholdPerDuplexPair(final double maximalRmsdThresholdPerDuplexPair) {
            this.maximalRmsdThresholdPerDuplexPair = maximalRmsdThresholdPerDuplexPair;
            return this;
        }

        public Builder alignmentMode(final AlignmentMode alignmentMode) {
            this.alignmentMode = alignmentMode;
            return this;
        }

        public Builder outputFormat(final FormatType outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        public Builder outputDirPath(final String outputDirPath) {
            this.outputDirPath = outputDirPath;
            return this;
        }

        public DescriptorsComparatorInputModel build() {
            return new DescriptorsComparatorInputModelImpl(this);
        }
    }

    private void initInputModelString() {
        inputModelString = new StringBuilder(inputModelString)
                .append("Molecule type: ")
                .append(moleculeType)
                .append("\n")
                .append("First descriptor file path: ")
                .append(firstDescriptorFilePath)
                .append("\n")
                .append("Second descriptor file path: ")
                .append(secondDescriptorFilePath)
                .append("\n")
                .append("Alignment atom names file path: ")
                .append(alignmentAtomNamesFilePath)
                .append("\n")
                .append("Comparison algorithm type: ")
                .append(comparisonAlgorithmType)
                .append("\n")
                .append("Features of structurally similar descriptors:\n")
                .append(similarDescriptorsVerifier.toString())
                .append("\n")
                .append("Maximal RMSD-based cost of a pair of aligned duplexes: ")
                .append(String.format("%.2f",
                        ResidueUtils.ensureCommonDoubleFormat(maximalRmsdThresholdPerDuplexPair)))
                .append("\n").append("Alignment mode: ").append(alignmentMode).append("\n")
                .append("Output dir path: ").append(outputDirPath).toString();
    }

    private void setMoleculeType() {
        moleculeType = getEnumValue("mt", MoleculeType.class, DEFAULT_MOLECULE);
    }

    private void setFirstDescriptorFilePath() {
        firstDescriptorFilePath = getOptionString("fd");
    }

    private void setSecondDescriptorFilePath() {
        secondDescriptorFilePath = getOptionString("sd");
    }

    private void setAlignmentAtomNamesFilePath() {
        alignmentAtomNamesFilePath = getOptionString("aan");
    }

    private void setComparisonAlgorithmType() {
        comparisonAlgorithmType = getEnumValue("cat", ComparisonAlgorithms.class, DEFAULT_ALGORITHM_TYPE);
    }

    private void setMaximalRmsdThresholdPerDuplexPair() {
        this.maximalRmsdThresholdPerDuplexPair = DEFAULT_MAXIMAL_RMSD_BASED_COST_OF_PAIR_OF_ALIGNED_DUPLEXES;
        final double newMaximalRmsdThresholdPerDuplexPair = getDoubleWhenNotLessZero("mrmsdtpdp",
                "maximal RMSD-based cost of a pair of aligned duplexes");
        if (Double.compare(newMaximalRmsdThresholdPerDuplexPair, 0.0) > 0) {
            this.maximalRmsdThresholdPerDuplexPair = newMaximalRmsdThresholdPerDuplexPair;
        }
    }

    private void setSimilarDescriptorsVerifier() {
        similarDescriptorsVerifier = new SimilarDescriptorsVerifierImpl();
        final double maximalOriginElementsPairAlignmentRmsd = getDoubleWhenNotLessZero("moeparmsd",
                "maximal RMSD of the central elements alignment");
        if (Double.compare(maximalOriginElementsPairAlignmentRmsd, 0.0) >= 0) {
            similarDescriptorsVerifier
                    .setMaximalOriginElementsPairAlignmentRmsd(maximalOriginElementsPairAlignmentRmsd);
        }
        final double maximalDuplexesPairAlignmentRmsd = getDoubleWhenNotLessZero("mdparmsd",
                "maximal RMSD of a pair of aligned duplexes");
        if (Double.compare(maximalDuplexesPairAlignmentRmsd, 0.0) >= 0) {
            similarDescriptorsVerifier.setMaximalDuplexesPairAlignmentRmsd(maximalDuplexesPairAlignmentRmsd);
        }
        double minimalAlignedElementsRatio = getDoubleWhenNotLessZero("maep",
                "minimal fraction of aligned elements");
        minimalAlignedElementsRatio = makePercentageFromFraction(minimalAlignedElementsRatio);
        if (Double.compare(minimalAlignedElementsRatio, 0.0) >= 0) {
            PreconditionUtils.checkIfValueIsAPercentage(minimalAlignedElementsRatio,
                    "Minimal fraction of aligned elements");
            similarDescriptorsVerifier.setMinimalAlignedElementsPercentage(minimalAlignedElementsRatio);
        }
        double minimalAlignedResiduesRatio = getDoubleWhenNotLessZero("marp",
                "minimal fraction of aligned residues");
        minimalAlignedResiduesRatio = makePercentageFromFraction(minimalAlignedResiduesRatio);
        if (Double.compare(minimalAlignedResiduesRatio, 0.0) >= 0) {
            PreconditionUtils.checkIfValueIsAPercentage(minimalAlignedResiduesRatio,
                    "Minimal fraction of aligned residues");
            similarDescriptorsVerifier.setMinimalAlignedResiduesPercentage(minimalAlignedResiduesRatio);
        }
        final double maximalAlignmentGlobalRmsd = getDoubleWhenNotLessZero("magrmsd",
                "maximal RMSD of the total alignment");
        if (Double.compare(maximalAlignmentGlobalRmsd, 0.0) >= 0) {
            similarDescriptorsVerifier.setMaximalAlignmentGlobalRmsd(maximalAlignmentGlobalRmsd);
        }
    }

    private void setAlignmentMode() {
        alignmentMode = getEnumValue("wa", AlignmentMode.class, DEFAULT_ALIGNMENT_MODE);
    }

    private void setOutputDirPath() {
        outputDirPath = getOptionString("od");
    }

    private boolean isInputFilesInitializedProperly() {
        return (isCommandLineHasOption("fd")) && (isCommandLineHasOption("od"))
                && (isCommandLineHasOption("sd")) && (isCommandLineHasOption("aan"));
    }

    private static final double makePercentageFromFraction(final double val) {
        if ((Double.compare(val, 0.0) >= 0) && (Double.compare(val, 1.0) <= 0)) {
            return val * MAXIMAL_PERCENTAGE_VALUE;
        }
        return val;
    }

}

package edu.put.ma.model.input;

import static edu.put.ma.descs.DescriptorsBuilderImpl.DEFAULT_ELEMENT_SIZE;

import java.io.File;
import java.io.IOException;

import lombok.Getter;

import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import edu.put.ma.descs.DescriptorsFilter;
import edu.put.ma.descs.DescriptorsFilterImpl;
import edu.put.ma.io.FormatType;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.utils.ArrayUtils;
import edu.put.ma.utils.PreconditionUtils;

@Getter
public class DescriptorsBuilderInputModelImpl extends CommonInputModelImpl implements
        DescriptorsBuilderInputModel {

    private static final int MINIMAL_EVEN_NUMBER = 2;

    private static final int ODD_NUMBER_REMAINDER = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorsBuilderInputModelImpl.class);

    private String inputFilePath;

    private MoleculeType moleculeType;

    private String inContactResiduesExpressionString;

    private int elementSize;

    private int threadsCount;

    private DescriptorsFilter descriptorsFilter;

    private String outputDirPath;

    public DescriptorsBuilderInputModelImpl(final String[] args, final String artifactId) {
        super(args);
        secureInitState(artifactId);
    }

    public DescriptorsBuilderInputModelImpl(final Builder descriptorsBuilderInputModelBuilder) {
        super(descriptorsBuilderInputModelBuilder.inputFormat,
                descriptorsBuilderInputModelBuilder.outputFormat);
        this.inputFilePath = descriptorsBuilderInputModelBuilder.inputFilePath;
        this.moleculeType = descriptorsBuilderInputModelBuilder.moleculeType;
        this.inContactResiduesExpressionString = descriptorsBuilderInputModelBuilder.inContactResiduesExpressionString;
        this.elementSize = descriptorsBuilderInputModelBuilder.elementSize;
        this.threadsCount = descriptorsBuilderInputModelBuilder.threadsCount;
        this.descriptorsFilter = descriptorsBuilderInputModelBuilder.descriptorsFilterBuilder.build();
        this.outputDirPath = descriptorsBuilderInputModelBuilder.outputDirPath;
        initOptionsMapping();
    }

    @Override
    public boolean isInputInitializedProperly() {
        return (isCommandLineHasOption("i"))
                && (isCommandLineHasOption("mt") && (isCommandLineHasOption("ice")));
    }

    @Override
    public Options constructSpecificOptions() {
        final Options options = new Options();
        options.addOption("i", "input-file", true, "input file path");
        options.addOption("mt", "molecule-type", true,
                "supported molecule types: " + ArrayUtils.getEnumNamesString(MoleculeType.class));
        options.addOption("ice", "in-contact-residues-expression-file", true,
                "file path of the expression that should be fulfilled by each in-contact residues pair");
        options.addOption("od", "output-directory", true, "(optional) output directory path");
        options.addOption("es", "element-size", true,
                "(optional) number of residues in a single element [default=" + DEFAULT_ELEMENT_SIZE + "]");
        options.addOption("tc", "threads-count", true,
                "(optional) number of threads used during processing [default=" + AVAILABLE_PROCESSORS + "]");
        options.addOption(
                "fscge",
                "filter-of-descriptors-that-characterized-with-lower-value-of-segments-count",
                true,
                "(optional) a filter on descriptors that are characterized by lower value of segments count than the given bound [default=1]");
        options.addOption(
                "fscle",
                "filter-of-descriptors-that-characterized-with-higher-value-of-segments-count",
                true,
                "(optional) a filter on descriptors that are characterized by higher value of segments count than the given bound [default=50]");
        options.addOption(
                "fecge",
                "filter-of-descriptors-that-characterized-with-lower-value-of-elements-count",
                true,
                "(optional) a filter on descriptors that are characterized by lower value of elements count than the given bound [default=1]");
        options.addOption(
                "fecle",
                "filter-of-descriptors-that-characterized-with-higher-value-of-elements-count",
                true,
                "(optional) a filter on descriptors that are characterized by higher value of elements count than the given bound [default=200]");
        options.addOption(
                "frcge",
                "filter-of-descriptors-that-characterized-with-lower-value-of-residues-count",
                true,
                "(optional) a filter on descriptors that are characterized by lower value of residues count than the given bound [default=1]");
        options.addOption(
                "frcle",
                "filter-of-descriptors-that-characterized-with-higher-value-of-residues-count",
                true,
                "(optional) a filter on descriptors that are characterized by higher value of residues count than the given bound [default=1000]");
        return options;
    }

    @Override
    public boolean areOptionalFormatOptions() {
        return true;
    }

    @Override
    protected void initOptionsMapping() {
        super.initOptionsMapping();
        this.optionsMapping.putAll(new ImmutableMap.Builder<String, String>().put("inputFilePath", "-i")
                .put("outputDirPath", "-od").put("moleculeType", "-mt").put("elementSize", "-es")
                .put("threadsCount", "-tc").put("minimalSegmentsCount", "-fscge")
                .put("maximalSegmentsCount", "-fscle").put("minimalElementsCount", "-fecge")
                .put("maximalElementsCount", "-fecle").put("minimalResiduesCount", "-frcge")
                .put("maximalResiduesCount", "-frcle").build());
    }

    @Override
    protected void initState() {
        super.initState();
        setInputFilePath();
        setMoleculeType();
        setInContactResiduesExpressionString();
        setElementSize();
        setThreadsCount();
        setDescriptorsFilter();
        setOutputDirPath();
        initInputModelString();
    }

    public static class Builder extends CommonInputModelImpl.Builder {

        @Getter
        private String inputFilePath;

        private MoleculeType moleculeType;

        private String inContactResiduesExpressionString;

        private int elementSize;

        private int threadsCount;

        private DescriptorsFilterImpl.Builder descriptorsFilterBuilder;

        private String outputDirPath;

        public Builder() {
            descriptorsFilterBuilder = new DescriptorsFilterImpl.Builder();
        }

        public Builder inputFilePath(final String inputFilePath) {
            this.inputFilePath = inputFilePath;
            return this;
        }

        public Builder inputFormat(final FormatType inputFormat) {
            this.inputFormat = inputFormat;
            return this;
        }

        public Builder moleculeType(final MoleculeType moleculeType) {
            this.moleculeType = moleculeType;
            return this;
        }

        public Builder inContactResiduesExpressionString(final String inContactResiduesExpressionString) {
            this.inContactResiduesExpressionString = inContactResiduesExpressionString;
            return this;
        }

        public Builder elementSize(final int elementSize) {
            this.elementSize = elementSize;
            return this;
        }

        public Builder threadsCount(final int threadsCount) {
            this.threadsCount = threadsCount;
            return this;
        }

        public Builder minimalSegmentsCount(final int minimalSegmentsCount) {
            this.descriptorsFilterBuilder = descriptorsFilterBuilder
                    .minimalSegmentsCount(minimalSegmentsCount);
            return this;
        }

        public Builder maximalSegmentsCount(final int maximalSegmentsCount) {
            this.descriptorsFilterBuilder = descriptorsFilterBuilder
                    .maximalSegmentsCount(maximalSegmentsCount);
            return this;
        }

        public Builder minimalElementsCount(final int minimalElementsCount) {
            this.descriptorsFilterBuilder = descriptorsFilterBuilder
                    .minimalElementsCount(minimalElementsCount);
            return this;
        }

        public Builder maximalElementsCount(final int maximalElementsCount) {
            this.descriptorsFilterBuilder = descriptorsFilterBuilder
                    .maximalElementsCount(maximalElementsCount);
            return this;
        }

        public Builder minimalResiduesCount(final int minimalResiduesCount) {
            this.descriptorsFilterBuilder = descriptorsFilterBuilder
                    .minimalResiduesCount(minimalResiduesCount);
            return this;
        }

        public Builder maximalResiduesCount(final int maximalResiduesCount) {
            this.descriptorsFilterBuilder = descriptorsFilterBuilder
                    .maximalResiduesCount(maximalResiduesCount);
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

        public DescriptorsBuilderInputModel build() {
            return new DescriptorsBuilderInputModelImpl(this);
        }
    }

    private void initInputModelString() {
        inputModelString = new StringBuilder(inputModelString).append("Input file path: ")
                .append(inputFilePath).append("\nMolecule type: ").append(moleculeType)
                .append("\nExpression that should be fulfilled by each in-contact residues pair: ")
                .append(inContactResiduesExpressionString)
                .append("\nNumber of residues considered by a single element: ").append(elementSize)
                .append("\nThreads count used during processing: ").append(threadsCount)
                .append("\nDescriptors filter properties:\n").append(descriptorsFilter.toString())
                .append("\nOutput dir path: ").append(outputDirPath).toString();
    }

    private void setInputFilePath() {
        inputFilePath = getOptionString("i");
    }

    private void setMoleculeType() {
        moleculeType = getEnumValue("mt", MoleculeType.class, DEFAULT_MOLECULE);
    }

    private void setInContactResiduesExpressionString() {
        String newInContactResiduesExpressionString = null;
        final String expressionFilePath = getOptionString("ice");
        PreconditionUtils.checkIfStringIsBlank(expressionFilePath, "Expression file path");
        final File expressionFile = FileUtils.getFile(expressionFilePath);
        PreconditionUtils.checkIfFileExistsAndIsNotADirectory(expressionFile, "Expression");
        try {
            newInContactResiduesExpressionString = FileUtils.readFileToString(expressionFile);
            PreconditionUtils.checkIfStringIsBlank(newInContactResiduesExpressionString,
                    "Expression used to identify in-contact residues");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        this.inContactResiduesExpressionString = newInContactResiduesExpressionString;
    }

    private void setElementSize() {
        this.elementSize = DEFAULT_ELEMENT_SIZE;
        final String elementSizeString = getOptionString("es");
        if (StringUtils.isNotBlank(elementSizeString)) {
            int inputElementSize = PreconditionUtils.parseInt(elementSizeString, "element size");
            if (inputElementSize % MINIMAL_EVEN_NUMBER == ODD_NUMBER_REMAINDER) {
                elementSize = inputElementSize;
            } else {
                throw new IllegalArgumentException("Element size should be odd number");
            }
        }
    }

    private void setThreadsCount() {
        this.threadsCount = AVAILABLE_PROCESSORS;
        final String threadsCountString = getOptionString("tc");
        if (StringUtils.isNotBlank(threadsCountString)) {
            threadsCount = PreconditionUtils.parseInt(threadsCountString, "threads count");
        }
    }

    private void setDescriptorsFilter() {
        descriptorsFilter = new DescriptorsFilterImpl();
        final int minimalSegmentsCount = getIntWhenNotLessOne("fscge", "minimal segments count");
        if (minimalSegmentsCount > 0) {
            descriptorsFilter.setMinimalSegmentsCount(minimalSegmentsCount);
        }
        final int maximalSegmentsCount = getIntWhenNotLessOne("fscle", "maximal segments count");
        if (maximalSegmentsCount > 0) {
            descriptorsFilter.setMaximalSegmentsCount(maximalSegmentsCount);
        }
        final int minimalElementsCount = getIntWhenNotLessOne("fecge", "minimal elements count");
        if (minimalElementsCount > 0) {
            descriptorsFilter.setMinimalElementsCount(minimalElementsCount);
        }
        final int maximalElementsCount = getIntWhenNotLessOne("fecle", "maximal elements count");
        if (maximalElementsCount > 0) {
            descriptorsFilter.setMaximalElementsCount(maximalElementsCount);
        }
        final int minimalResiduesCount = getIntWhenNotLessOne("frcge", "minimal residues count");
        if (minimalResiduesCount > 0) {
            descriptorsFilter.setMinimalResiduesCount(minimalResiduesCount);
        }
        final int maximalResiduesCount = getIntWhenNotLessOne("frcle", "maximal residues count");
        if (maximalResiduesCount > 0) {
            descriptorsFilter.setMaximalResiduesCount(maximalResiduesCount);
        }
    }

    private void setOutputDirPath() {
        outputDirPath = getOptionString("od");
        if (StringUtils.isBlank(outputDirPath)) {
            outputDirPath = FilenameUtils.getFullPath(getInputFilePath()) + "out";
        }
    }
}

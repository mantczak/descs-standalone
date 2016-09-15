package edu.put.ma.model.input;

import lombok.Getter;

import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;

import edu.put.ma.archiver.ArchiverFactory;
import edu.put.ma.io.FormatType;

public class FormatConverterInputModelImpl extends CommonInputModelImpl implements FormatConverterInputModel {

    @Getter
    private String inputFilePath;

    @Getter
    private String outputFilePath;

    public FormatConverterInputModelImpl(final String[] args, final String artifactId) {
        super(args);
        secureInitState(artifactId);
    }

    public FormatConverterInputModelImpl(final Builder formatConverterInputModelBuilder) {
        super(formatConverterInputModelBuilder.inputFormat, formatConverterInputModelBuilder.outputFormat);
        this.inputFilePath = formatConverterInputModelBuilder.inputFilePath;
        this.outputFilePath = formatConverterInputModelBuilder.outputFilePath;
        initOptionsMapping();
    }

    @Override
    public boolean isInputInitializedProperly() {
        return (isCommandLineHasOption("i"))
                && (isCommandLineHasOption("if") && (isCommandLineHasOption("of")));
    }

    @Override
    public Options constructSpecificOptions() {
        final Options options = new Options();
        options.addOption("i", "input-file", true, "input file path");
        options.addOption("o", "output-file", true, "(optional) output file path");
        return options;
    }

    @Override
    public boolean isValid() {
        return getInputFormat() != getOutputFormat() && (isPdb2CifValid() || isCif2PdbValid());
    }

    @Override
    protected void initOptionsMapping() {
        super.initOptionsMapping();
        this.optionsMapping.putAll(ImmutableMap.of("inputFilePath", "-i", "outputFilePath", "-o"));
    }

    @Override
    protected void initState() {
        super.initState();
        setInputFilePath();
        setOutputFilePath();
        initInputModelString();
    }

    @Getter
    public static class Builder extends CommonInputModelImpl.Builder {

        private String inputFilePath;

        private String outputFilePath;

        public Builder(final FormatType inputFormat, final String inputFilePath,
                final FormatType outputFormat, final String outputFilePath) {
            this.inputFilePath = inputFilePath;
            this.outputFilePath = outputFilePath;
            this.inputFormat = inputFormat;
            this.outputFormat = outputFormat;
        }

        public Builder inputFormat(final FormatType inputFormat) {
            this.inputFormat = inputFormat;
            return this;
        }

        public Builder inputFilePath(final String inputFilePath) {
            this.inputFilePath = inputFilePath;
            return this;
        }

        public Builder outputFormat(final FormatType outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        public Builder outputFilePath(final String outputFilePath) {
            this.outputFilePath = outputFilePath;
            return this;
        }

        public FormatConverterInputModel build() {
            return new FormatConverterInputModelImpl(this);
        }
    }

    private boolean isPdb2CifValid() {
        return (FormatType.PDB == getInputFormat())
                && (((StringUtils.endsWithIgnoreCase(inputFilePath, ".pdb")) || (StringUtils
                        .endsWithIgnoreCase(inputFilePath, ".tar.gz"))) && (StringUtils.endsWithIgnoreCase(
                        outputFilePath, ".cif")));
    }

    private boolean isCif2PdbValid() {
        return (FormatType.PDB == getOutputFormat())
                && (((StringUtils.endsWithIgnoreCase(outputFilePath, ".pdb")) || (StringUtils
                        .endsWithIgnoreCase(outputFilePath, ".tar.gz"))) && (StringUtils.endsWithIgnoreCase(
                        inputFilePath, ".cif")));
    }

    private void initInputModelString() {
        inputModelString = new StringBuilder(inputModelString).append("Input file path: ")
                .append(inputFilePath).append("\nOutput file path: ").append(outputFilePath).toString();
    }

    private void setInputFilePath() {
        inputFilePath = getOptionString("i");
    }

    private void setOutputFilePath() {
        this.outputFilePath = getOptionString("o");
        if (StringUtils.isBlank(outputFilePath)) {
            final String inputFileFullPath = FilenameUtils.getFullPath(inputFilePath);
            final String inputFilename = FilenameUtils.getName(inputFilePath);
            String inputFileBasename = null;
            if (ArchiverFactory.isArchive(inputFilename)) {
                final String currentPostfix = ArchiverFactory.getArchiverPostfix(inputFilename);
                inputFileBasename = StringUtils.substring(inputFilename, 0,
                        StringUtils.indexOf(inputFilename, currentPostfix));
            } else {
                inputFileBasename = FilenameUtils.getBaseName(inputFilePath);
            }
            final String expectedExtension = StringUtils.lowerCase(getOutputFormat().toString());
            this.outputFilePath = FilenameUtils.concat(inputFileFullPath, inputFileBasename + "."
                    + expectedExtension);
        }
    }
}

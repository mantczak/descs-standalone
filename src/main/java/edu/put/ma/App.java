package edu.put.ma;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.put.ma.descs.AlignmentMode;
import edu.put.ma.descs.DescriptorsBuilder;
import edu.put.ma.descs.DescriptorsBuilderImpl;
import edu.put.ma.descs.DescriptorsComparator;
import edu.put.ma.descs.DescriptorsComparatorImpl;
import edu.put.ma.descs.UnappropriateDescriptorException;
import edu.put.ma.descs.UncomparableDescriptorsException;
import edu.put.ma.descs.contacts.ContactsInspector;
import edu.put.ma.descs.contacts.ContactsInspectorImpl;
import edu.put.ma.descs.contacts.ExpressionValidatorImpl;
import edu.put.ma.io.reader.Reader;
import edu.put.ma.io.reader.ReaderFactory;
import edu.put.ma.io.writer.Writer;
import edu.put.ma.io.writer.WriterFactory;
import edu.put.ma.model.ComparisonResult;
import edu.put.ma.model.DescriptorsPair;
import edu.put.ma.model.DescriptorsPairImpl;
import edu.put.ma.model.ModelProperties;
import edu.put.ma.model.ModelPropertiesImpl;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.model.StructureType;
import edu.put.ma.model.input.CommonInputModel;
import edu.put.ma.model.input.CommonInputModelImpl;
import edu.put.ma.model.input.DescriptorsBuilderInputModel;
import edu.put.ma.model.input.DescriptorsBuilderInputModelImpl;
import edu.put.ma.model.input.DescriptorsComparatorInputModel;
import edu.put.ma.model.input.DescriptorsComparatorInputModelImpl;
import edu.put.ma.model.input.FormatConverterInputModel;
import edu.put.ma.model.input.FormatConverterInputModelImpl;
import edu.put.ma.structure.StructureExtension;
import edu.put.ma.structure.StructureExtensionImpl;
import edu.put.ma.utils.ArgumentUtils;
import edu.put.ma.utils.ArrayUtils;
import edu.put.ma.utils.CommandLineUtils;
import edu.put.ma.utils.PreconditionUtils;

public class App {

    private static final String ERROR_OCCURED = "Error occured during input processing - a cause can be found in log file";

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static final ExecutionMode DEFAULT_EXECUTION_MODE = ExecutionMode.FORMAT_CONVERSION;

    private static final ImmutableSet<String> EXECUTION_MODE_CODES = ImmutableSet.of("em", "execution-mode");

    private final Properties properties;

    App() {
        this.properties = new Properties();
        loadProperties();
    }

    public static void main(String[] args) {
        final App app = new App();
        try {
            app.execute(args);
        } catch (UncomparableDescriptorsException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    public void execute(final String[] args) throws UncomparableDescriptorsException {
        final ExecutionMode executionMode = getExecutionMode(args);
        final String[] newArgs = ArgumentUtils.removeArgByOpt(args, EXECUTION_MODE_CODES);
        final CommonInputModel inputModel = constructInputModel(executionMode, newArgs, getArtifactId());
        if (inputModel.isInputInitializedProperly()) {
            execute(executionMode, inputModel);
        } else {
            inputModel.printHelp(getArtifactId());
        }
    }

    private void loadProperties() {
        InputStream inputStream = null;
        try {
            inputStream = this.getClass().getResourceAsStream("app.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private String getPropertyValueByKey(final String key) {
        final String propertyValue = properties.getProperty(key);
        PreconditionUtils.checkIfStringIsBlank(propertyValue, String.format("Value of property [%s]", key));
        return propertyValue;
    }

    private String getArtifactId() {
        return getPropertyValueByKey("artifactId");
    }

    private static final Options getExecutionModeOption() {
        final Options options = new Options();
        options.addOption("em", "execution-mode", true,
                "provided execution modes: " + ArrayUtils.getEnumNamesString(ExecutionMode.class)
                        + " [default=" + App.DEFAULT_EXECUTION_MODE + "]");
        return options;
    }

    private ExecutionMode getExecutionMode(final String[] args) {
        final Options options = getExecutionModeOption();
        try {
            final String[] executionModeArg = ArgumentUtils.retrieveArgByOpt(args, EXECUTION_MODE_CODES);
            if (!CollectionUtils.sizeIsEmpty(executionModeArg)) {
                return getExecutionMode(options, executionModeArg);
            } else {
                CommandLineUtils.printHelp(getArtifactId(), options);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            CommandLineUtils.printHelp(getArtifactId(), options);
        }
        return DEFAULT_EXECUTION_MODE;
    }

    private ExecutionMode getExecutionMode(final Options options, final String[] executionModeArg) {
        final String executionModeCode = getExecutionModeCode(executionModeArg[0]);
        final CommandLine commandLine = CommandLineUtils.parseArgs(executionModeArg, options);
        if (commandLine.hasOption(executionModeCode)) {
            final ExecutionMode executionMode = CommonInputModelImpl.getEnumValue(commandLine,
                    executionModeCode, ExecutionMode.class);
            if (executionMode != null) {
                return executionMode;
            } else {
                CommandLineUtils.printHelp(getArtifactId(), options);
            }
        }
        return DEFAULT_EXECUTION_MODE;
    }

    private static final String getExecutionModeCode(final String code) {
        PreconditionUtils.checkIfStringIsBlank(code, "Execution code");
        final String longPrefix = "--";
        final String shortPrefix = "-";
        String result = !StringUtils.startsWith(code, longPrefix) ? code : StringUtils.substring(code,
                StringUtils.length(longPrefix));
        result = !StringUtils.startsWith(result, shortPrefix) ? result : StringUtils.substring(result,
                StringUtils.length(shortPrefix));
        return result;
    }

    private <T extends CommonInputModel> void convert(final T inputModel) {
        try {
            LOGGER.info("Start of formats conversion...");
            PreconditionUtils.checkIfInstanceOfInputModelIsAsExpectedOne(inputModel, inputModel.getClass(),
                    FormatConverterInputModel.class);
            final FormatConverterInputModel specificInputModel = (FormatConverterInputModel) inputModel;
            LOGGER.info(specificInputModel.getInputModelString());
            if (specificInputModel.getInputFormat() != specificInputModel.getOutputFormat()) {
                final Reader reader = ReaderFactory.construct(specificInputModel.getInputFormat());
                final Structure structure = reader.read(specificInputModel.getInputFilePath());
                Preconditions.checkNotNull(
                        structure,
                        String.format("%s descriptor should be initialized properly",
                                FilenameUtils.getBaseName(specificInputModel.getInputFilePath())));
                final Writer writer = WriterFactory.construct(specificInputModel.getOutputFormat());
                writer.write(structure, specificInputModel.getOutputFilePath());
            } else {
                LOGGER.info("Input and output formats are the same - there is no need to perform the conversion");
            }
        } catch (Exception e) {
            LOGGER.info(ERROR_OCCURED);
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("Formats conversion done");
        }
    }

    private <T extends CommonInputModel> void buildDescriptors(final T inputModel) {
        try {
            LOGGER.info("Start of descriptors building...");
            PreconditionUtils.checkIfInstanceOfInputModelIsAsExpectedOne(inputModel, inputModel.getClass(),
                    DescriptorsBuilderInputModel.class);
            final DescriptorsBuilderInputModel specificInputModel = (DescriptorsBuilderInputModel) inputModel;
            LOGGER.info(specificInputModel.getInputModelString());
            final ContactsInspector contactsInspector = new ContactsInspectorImpl(
                    specificInputModel.getInContactResiduesExpressionString(),
                    specificInputModel.getMoleculeType(), specificInputModel.getThreadsCount());
            if (contactsInspector.isValid()) {
                final DescriptorsBuilder descriptorsBuilder = new DescriptorsBuilderImpl(contactsInspector,
                        specificInputModel.getThreadsCount());
                final Reader reader = ReaderFactory.construct(specificInputModel.getInputFormat());
                final StructureExtension extendedStructure = getExtendedStructure(
                        specificInputModel.getInputFilePath(), reader, specificInputModel.getMoleculeType(),
                        StructureType.MOLECULE);
                if (extendedStructure.isValid()) {
                    final Writer writer = WriterFactory.construct(specificInputModel.getOutputFormat());
                    final File outputDir = FileUtils.getFile(specificInputModel.getOutputDirPath());
                    FileUtils.deleteDirectory(outputDir);
                    FileUtils.forceMkdir(outputDir);
                    descriptorsBuilder
                            .saveAtomNamePairsConsideredByInContactResiduesIdentificationExpression(
                                    outputDir, writer);
                    processStructure(specificInputModel, descriptorsBuilder, extendedStructure, writer,
                            outputDir);
                } else {
                    throw new IllegalArgumentException(String.format("Input 3D structure [%s] is invalid",
                            FilenameUtils.getBaseName(specificInputModel.getInputFilePath())));
                }
                if (descriptorsBuilder != null) {
                    descriptorsBuilder.close();
                }
            } else {
                throw new IllegalArgumentException(
                        "Inproper format of expression used to identify in-contact residues that should be refined");
            }
        } catch (Exception e) {
            LOGGER.info(ERROR_OCCURED);
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("Descriptors building done");
        }
    }

    private void processStructure(final DescriptorsBuilderInputModel specificInputModel,
            final DescriptorsBuilder descriptorsBuilder, final StructureExtension extendedStructure,
            final Writer writer, final File outputDir) {
        final int modelsCount = extendedStructure.getModelsNo();
        for (int modelIndex = 0; modelIndex < modelsCount; modelIndex++) {
            final List<Chain> model = extendedStructure.getModelByIndex(modelIndex);
            final ModelProperties modelProperties = new ModelPropertiesImpl(model,
                    specificInputModel.getMoleculeType(), specificInputModel.getElementSize());
            if (modelProperties.isValid()) {
                descriptorsBuilder.build(extendedStructure, (modelsCount > 1) ? modelIndex : -1,
                        modelProperties);
                final String summary = descriptorsBuilder.saveDescriptors(outputDir,
                        (modelsCount > 1) ? String.valueOf(modelIndex + 1) : "", writer,
                        specificInputModel.getDescriptorsFilter());
                if (StringUtils.isNotBlank(summary)) {
                    LOGGER.info(summary);
                } else {
                    LOGGER.info("There is no descriptors that meet defined requirements");
                }
            }
        }
    }

    private <T extends CommonInputModel> void compareDescriptors(final T inputModel)
            throws UncomparableDescriptorsException {
        try {
            LOGGER.info("Start of descriptors comparison...");
            PreconditionUtils.checkIfInstanceOfInputModelIsAsExpectedOne(inputModel, inputModel.getClass(),
                    DescriptorsComparatorInputModel.class);
            final DescriptorsComparatorInputModel specificInputModel = (DescriptorsComparatorInputModel) inputModel;
            LOGGER.info(specificInputModel.getInputModelString());
            final File outputDir = FileUtils.getFile(specificInputModel.getOutputDirPath());
            FileUtils.deleteDirectory(outputDir);
            FileUtils.forceMkdir(outputDir);
            final Reader reader = ReaderFactory.construct(specificInputModel.getInputFormat());
            final StructureExtension firstDescriptorExtendedStructure = getExtendedStructure(
                    specificInputModel.getFirstDescriptorFilePath(), reader,
                    specificInputModel.getMoleculeType(), StructureType.DESCRIPTOR);
            final StructureExtension secondDescriptorExtendedStructure = getExtendedStructure(
                    specificInputModel.getSecondDescriptorFilePath(), reader,
                    specificInputModel.getMoleculeType(), StructureType.DESCRIPTOR);
            if ((firstDescriptorExtendedStructure.isValid()) && (secondDescriptorExtendedStructure.isValid())) {
                compareDescriptors(specificInputModel, outputDir, firstDescriptorExtendedStructure,
                        secondDescriptorExtendedStructure);
            } else {
                if (!firstDescriptorExtendedStructure.isValid()) {
                    throw new IllegalArgumentException(String.format(
                            "Descriptor 3D structure [%s] is invalid",
                            FilenameUtils.getBaseName(specificInputModel.getFirstDescriptorFilePath())));
                }
                if (!secondDescriptorExtendedStructure.isValid()) {
                    throw new IllegalArgumentException(String.format(
                            "Descriptor 3D structure [%s] is invalid",
                            FilenameUtils.getBaseName(specificInputModel.getSecondDescriptorFilePath())));
                }
            }
        } catch (UncomparableDescriptorsException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.info(ERROR_OCCURED);
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("Descriptors comparison done");
        }
    }

    private void compareDescriptors(final DescriptorsComparatorInputModel specificInputModel,
            final File outputDir, final StructureExtension firstDescriptorExtendedStructure,
            final StructureExtension secondDescriptorExtendedStructure)
            throws UncomparableDescriptorsException {
        PreconditionUtils.checkIfMoleculeTypesOfComparedDescriptorsAreConsistent(
                firstDescriptorExtendedStructure, secondDescriptorExtendedStructure);
        final ImmutableList<String> alignmentAtomNames = getAlignmentAtomNames(
                specificInputModel.getAlignmentAtomNamesFilePath(), specificInputModel.getMoleculeType());
        LOGGER.info("Alignment atom names: " + alignmentAtomNames.toString());
        try {
            DescriptorsPair descriptorsPair = new DescriptorsPairImpl(firstDescriptorExtendedStructure,
                    secondDescriptorExtendedStructure, specificInputModel.getAlignmentMode());
            PreconditionUtils.checkIfDescriptorsAreComparable(descriptorsPair);
            final DescriptorsComparator descriptorsComparator = new DescriptorsComparatorImpl(
                    specificInputModel.getComparisonAlgorithm(),
                    specificInputModel.getSimilarDescriptorsVerifier(),
                    specificInputModel.getMaximalRmsdThresholdPerDuplexPair(), alignmentAtomNames);
            final ComparisonResult comparisonResult = descriptorsComparator.compare(descriptorsPair);
            if ((comparisonResult != null) && (comparisonResult.isStructurallySimilar())) {
                LOGGER.info(String.format("Following descriptors %s %s",
                        descriptorsPair.getDescriptorsPairId(), comparisonResult.toString()));
                saveDescriptorsAlignment(specificInputModel, outputDir, descriptorsPair, comparisonResult);
            } else {
                LOGGER.info(String.format("Following descriptors %s are not structurally similar",
                        descriptorsPair.getDescriptorsPairId()));
            }
        } catch (UnappropriateDescriptorException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private void saveDescriptorsAlignment(final DescriptorsComparatorInputModel specificInputModel,
            final File outputDir, final DescriptorsPair descriptorsPair,
            final ComparisonResult comparisonResult) {
        if (specificInputModel.getAlignmentMode() == AlignmentMode.CONSIDER) {
            final Writer writer = WriterFactory.construct(specificInputModel.getOutputFormat());
            descriptorsPair.saveFirstDescriptor(outputDir, writer);
            descriptorsPair.rotateAndShiftSecondDescriptor(comparisonResult.getSuperimposer());
            descriptorsPair.saveSecondDescriptor(outputDir, writer);
        }
    }

    private void execute(final ExecutionMode executionMode, final CommonInputModel inputModel)
            throws UncomparableDescriptorsException {
        switch (executionMode) {
            case DESCRIPTORS_BUILDING:
                buildDescriptors(inputModel);
                break;
            case DESCRIPTORS_COMPARISON:
                compareDescriptors(inputModel);
                break;
            default:
                convert(inputModel);
        }
    }

    private ImmutableList<String> getAlignmentAtomNames(final String alignmentAtomNamesFilePath,
            final MoleculeType moleculeType) {
        final File alignmentAtomNamesFile = FileUtils.getFile(alignmentAtomNamesFilePath);
        PreconditionUtils.checkIfFileExistsAndIsNotADirectory(alignmentAtomNamesFile, "Alignment atom names");
        try {
            final String alignmentAtomNamesString = FileUtils.readFileToString(alignmentAtomNamesFile);
            PreconditionUtils.checkIfStringIsBlank(alignmentAtomNamesString, "Alignment atom names string");
            return constructAlignmentAtomNames(alignmentAtomNamesString, moleculeType);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        throw new IllegalArgumentException("Format of alignment atom names string is inproper");
    }

    private static final CommonInputModel constructInputModel(final ExecutionMode executionMode,
            final String[] args, final String artifactId) {
        CommonInputModel inputModel = null;
        switch (executionMode) {
            case DESCRIPTORS_BUILDING:
                inputModel = new DescriptorsBuilderInputModelImpl(args, artifactId);
                break;
            case DESCRIPTORS_COMPARISON:
                inputModel = new DescriptorsComparatorInputModelImpl(args, artifactId);
                break;
            default:
                inputModel = new FormatConverterInputModelImpl(args, artifactId);
        }
        Preconditions.checkNotNull(inputModel, "Input model");
        return inputModel;
    }

    private static final StructureExtension getExtendedStructure(final String inputFilePath,
            final Reader reader, final MoleculeType moleculeType, final StructureType structureType) {
        final Structure structure = reader.read(inputFilePath);
        final String inputFileBasename = FilenameUtils.getBaseName(inputFilePath);
        Preconditions.checkNotNull(structure,
                String.format("%s structure should be initialized properly", inputFileBasename));
        if (structureType == StructureType.DESCRIPTOR) {
            PreconditionUtils.checkIfDescriptorDoesNotContainMultipleModels(structure, inputFileBasename);
        }
        return new StructureExtensionImpl(inputFileBasename, structure, moleculeType);
    }

    private static final ImmutableList<String> constructAlignmentAtomNames(
            final String alignmentAtomNamesString, final MoleculeType moleculeType) {
        final String unifiedAlignmentAtomNamesString = StringUtils.deleteWhitespace(StringUtils
                .upperCase(alignmentAtomNamesString));
        final Pattern atomNamesPattern = ExpressionValidatorImpl.getAtomNamesPattern(moleculeType);
        final Matcher matcher = atomNamesPattern.matcher(unifiedAlignmentAtomNamesString);
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        while (matcher.find()) {
            builder.add(matcher.group());
        }
        return builder.build();
    }
}

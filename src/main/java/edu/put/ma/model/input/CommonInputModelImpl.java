package edu.put.ma.model.input;

import static edu.put.ma.utils.StringUtils.NEW_LINE;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.put.ma.ExecutionMode;
import edu.put.ma.descs.AlignmentMode;
import edu.put.ma.descs.DescriptorsFilterImpl;
import edu.put.ma.descs.SimilarDescriptorsVerifierImpl;
import edu.put.ma.descs.algorithms.AlignmentAcceptanceMode;
import edu.put.ma.descs.algorithms.ComparisonAlgorithms;
import edu.put.ma.io.FormatType;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.utils.ArrayUtils;
import edu.put.ma.utils.CommandLineUtils;
import edu.put.ma.utils.PreconditionUtils;

public abstract class CommonInputModelImpl implements CommonInputModel {

    protected static final MoleculeType DEFAULT_MOLECULE = MoleculeType.PROTEIN;

    protected static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    protected static final AlignmentMode DEFAULT_ALIGNMENT_MODE = AlignmentMode.IGNORE;

    protected static final AlignmentAcceptanceMode DEFAULT_ALIGNMENT_ACCEPTANCE_MODE = AlignmentAcceptanceMode.ALIGNED_RESIDUES_AND_AVERAGE_RMSD_OF_ALIGNED_DUPLEXES;

    protected static final ComparisonAlgorithms DEFAULT_ALGORITHM_TYPE = ComparisonAlgorithms.BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT;

    private static final FormatType DEFAULT_FORMAT = FormatType.PDB;

    private static final int COMMON_ARGUMENTS_COUNT = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonInputModelImpl.class);

    @Getter
    protected String inputModelString;

    protected Map<String, String> optionsMapping;

    private CommandLine commandLine;

    private Options options;

    @Getter
    private FormatType inputFormat;

    @Getter
    private FormatType outputFormat;

    protected CommonInputModelImpl(final String[] args) {
        this.options = constructCommonOptions(isOptionalFormatOptions());
        extendOptions(options, constructSpecificOptions());
        this.commandLine = CommandLineUtils.parseArgs(args, options);
    }

    protected CommonInputModelImpl(final FormatType inputFormat, final FormatType outputFormat) {
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
    }

    @Override
    public void printHelp(final String artifactId) {
        CommandLineUtils.printHelp(artifactId, options);
    }

    @Override
    public String[] getArgs() {
        final List<String> result = Lists.newArrayList();
        result.add("-em");
        if (this instanceof FormatConverterInputModelImpl) {
            result.add(ExecutionMode.FORMAT_CONVERSION.toString());
        } else if (this instanceof DescriptorsBuilderInputModelImpl) {
            result.add(ExecutionMode.DESCRIPTORS_BUILDING.toString());
        } else if (this instanceof DescriptorsComparatorInputModelImpl) {
            result.add(ExecutionMode.DESCRIPTORS_COMPARISON.toString());
        } else {
            result.remove(0);
        }
        int resultSize = CollectionUtils.size(result);
        if (resultSize == COMMON_ARGUMENTS_COUNT) {
            Class<?> currentClass = this.getClass();
            while (currentClass.getSuperclass() != null) {
                CollectionUtils.addAll(result, processDeclaredFields(currentClass, this));
                currentClass = currentClass.getSuperclass();
            }
        }
        resultSize = CollectionUtils.size(result);
        return result.toArray(new String[resultSize]);
    }

    @Override
    public boolean isOptionalFormatOptions() {
        return false;
    }

    public static final <T extends Enum<T>> T getEnumValue(final CommandLine commandLine,
            final String option, final Class<T> enumClass) {
        final String optionString = getOptionString(commandLine, option);
        if (StringUtils.isNotBlank(optionString)) {
            T enumVal = getEnumValue(optionString, enumClass);
            if (enumVal != null) {
                return enumVal;
            }
        }
        return null;
    }

    protected void initOptionsMapping() {
        this.optionsMapping = Maps.newHashMap();
        this.optionsMapping.putAll(ImmutableMap.of("inputFormat", "-if", "outputFormat", "-of"));
    }

    protected boolean isCommandLineHasOption(final String option) {
        return commandLine.hasOption(option);
    }

    protected String getOptionString(final String option) {
        return getOptionString(commandLine, option);
    }

    protected void secureInitState(final String artifactId) {
        try {
            initState();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            printHelp(artifactId);
        }
    }

    protected void initState() {
        setInputFormat();
        setOutputFormat();
        initInputModelString();
    }

    protected int getIntWhenNotLessOne(final String option, final String postfix) {
        return getIntWhenNotLessOne(commandLine, option, postfix);
    }

    protected <T extends Enum<T>> T getEnumValue(final String option, final Class<T> enumClass,
            final T defaultValue) {
        return getEnumValue(commandLine, option, enumClass, defaultValue);
    }

    protected double getDoubleWhenNotLessZero(final String option, final String postfix) {
        return getDoubleWhenNotLessZero(commandLine, option, postfix);
    }

    @Getter
    protected static class Builder {

        protected FormatType inputFormat;

        protected FormatType outputFormat;

        public Builder inputFormat(final FormatType inputFormat) {
            this.inputFormat = inputFormat;
            return this;
        }

        public Builder outputFormat(final FormatType outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }
    }

    private List<String> processDeclaredFields(final Class<?> currentClass, final Object object) {
        final List<String> result = Lists.newArrayList();
        for (Field field : currentClass.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                final String fieldName = field.getName();
                final Object fieldObject = field.get(object);
                if ((this.optionsMapping.containsKey(fieldName))
                        && (isConsideredString(field, fieldObject)
                                || isConsideredPrimitive(field, fieldObject) || isNotNullOtherInstance(field,
                                    fieldObject))) {
                    result.add(optionsMapping.get(fieldName));
                    result.add(String.valueOf(fieldObject));
                } else if (areParticularInstancesConsidered(field, fieldObject)) {
                    final List<String> nestedResult = processDeclaredFields(fieldObject.getClass(),
                            fieldObject);
                    CollectionUtils.addAll(result, nestedResult);
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return result;
    }

    private boolean isNotNullOtherInstance(final Field field, final Object fieldObject) {
        return (!((field.getType().isAssignableFrom((Class<?>) Integer.TYPE)) || (field.getType()
                .isAssignableFrom((Class<?>) Double.TYPE)))) && (fieldObject != null);
    }

    private boolean isConsideredString(final Field field, final Object fieldObject) {
        return (field.getType().isAssignableFrom((Class<?>) String.class))
                && (StringUtils.isNotBlank(String.valueOf(fieldObject)));
    }

    private boolean isConsideredPrimitive(final Field field, final Object fieldObject) {
        return ((field.getType().isAssignableFrom((Class<?>) Integer.TYPE)) && (((Integer) fieldObject)
                .intValue() > 0))
                || ((field.getType().isAssignableFrom((Class<?>) Double.TYPE)) && (Double.compare(
                        ((Double) fieldObject).doubleValue(), 0.0) > 0));
    }

    private boolean areParticularInstancesConsidered(final Field field, final Object fieldObject) {
        return ((field.getType().isAssignableFrom((Class<?>) DescriptorsFilterImpl.class)) || (field
                .getType().isAssignableFrom((Class<?>) SimilarDescriptorsVerifierImpl.class)))
                && (fieldObject != null);
    }

    private void initInputModelString() {
        inputModelString = new StringBuilder("Input format: ").append(inputFormat)
                .append("\nOutput format: ").append(outputFormat).append(NEW_LINE).toString();
    }

    private void setInputFormat() {
        inputFormat = getFormat(commandLine, "if");
    }

    private void setOutputFormat() {
        outputFormat = getFormat(commandLine, "of");
    }

    private void extendOptions(Options options, Options additionalOptions) {
        for (Option option : additionalOptions.getOptions()) {
            options.addOption(option);
        }
    }

    private static final <T extends Enum<T>> T getEnumValue(final CommandLine commandLine,
            final String option, final Class<T> enumClass, final T defaultValue) {
        T enumValue = getEnumValue(commandLine, option, enumClass);
        if (enumValue == null) {
            enumValue = defaultValue;
        }
        return enumValue;
    }

    private static final String getOptionString(final CommandLine commandLine, final String option) {
        if (commandLine.hasOption(option)) {
            return commandLine.getOptionValue(option);
        }
        return null;
    }

    private static final Options constructCommonOptions(final boolean optionalFormatOptions) {
        final String formatTypeNamesString = ArrayUtils.getEnumNamesString(FormatType.class);
        final StringBuilder formatsDescriptionStringBuilder = (optionalFormatOptions) ? new StringBuilder(
                "(optional) ") : new StringBuilder();
        formatsDescriptionStringBuilder.append("supported file formats: ").append(formatTypeNamesString)
                .append(" [default=" + DEFAULT_FORMAT + "]");
        final String formatsDescriptionString = formatsDescriptionStringBuilder.toString();
        final Options options = new Options();
        options.addOption("if", "input-format", true, formatsDescriptionString);
        options.addOption("of", "output-format", true, formatsDescriptionString);
        return options;
    }

    private static final FormatType getFormat(final CommandLine commandLine, final String option) {
        final FormatType formatType = getEnumValue(commandLine, option, FormatType.class);
        if (formatType != null) {
            return formatType;
        }
        return FormatType.PDB;
    }

    private static final <T extends Enum<T>> T getEnumValue(final String value, final Class<T> enumClass) {
        if ((enumClass != null) && (StringUtils.isNotBlank(value))) {
            return Enum.valueOf(enumClass, StringUtils.upperCase(StringUtils.trim(value)));
        }
        return null;
    }

    private static final int getIntWhenNotLessOne(final CommandLine commandLine, final String option,
            final String postfix) {
        final String valueString = getOptionString(commandLine, option);
        if (StringUtils.isNotBlank(valueString)) {
            final int value = PreconditionUtils.parseInt(valueString, postfix);
            if (value > 0) {
                return value;
            }
        }
        return 0;
    }

    private static final double getDoubleWhenNotLessZero(final CommandLine commandLine, final String option,
            final String postfix) {
        final String valueString = getOptionString(commandLine, option);
        if (StringUtils.isNotBlank(valueString)) {
            final double value = PreconditionUtils.parseDouble(valueString, postfix);
            if (Double.compare(value, 0.0) >= 0) {
                return value;
            }
        }
        return -1.0;
    }

}

package edu.put.ma.utils;

import java.io.File;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Structure;

import edu.put.ma.descs.UncomparableDescriptorsException;
import edu.put.ma.model.DescriptorsPair;
import edu.put.ma.model.input.CommonInputModel;
import edu.put.ma.structure.StructureExtension;

public final class PreconditionUtils {

    public static final double MAXIMAL_PERCENTAGE_VALUE = 100.0;

    private static final double MINIMAL_PERCENTAGE_VALUE = 0.0;

    private PreconditionUtils() {
        // hidden constructor
    }

    public static final void checkIfIndexInRange(final int index, final int min, final int max,
            final String prefix) {
        if (!((index >= min) && (index < max))) {
            throw new IndexOutOfBoundsException(String.format("%s index '%s' out of range [%s:%s]", prefix,
                    index, min, max));
        }
    }

    public static final void checkIfValueIsAPercentage(final double value, final String prefix) {
        if (!((Double.compare(value, MINIMAL_PERCENTAGE_VALUE) >= 0) && (Double.compare(value,
                MAXIMAL_PERCENTAGE_VALUE) <= 0))) {
            throw new IllegalArgumentException(String.format("%s value '%f' out of range [0.0:100.0]",
                    prefix, value));
        }
    }

    public static final int parseInt(final String value, final String postfix) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Inproper number format of %s '%s'", postfix,
                    value));
        }
    }

    public static final void checkEqualityOfMatrixBothDimensions(final double[][] matrix,
            final String description) {
        final int rowsNo = ArrayUtils.getLength(matrix);
        boolean valid = true;
        for (int rowIndex = 0; rowIndex < rowsNo; rowIndex++) {
            final int elementsNo = ArrayUtils.getLength(matrix[rowIndex]);
            if (elementsNo != rowsNo) {
                valid = false;
                break;
            }
        }
        if (!valid) {
            throw new IllegalArgumentException(String.format(
                    "Sizes of both dimensions of %s matrix should be equal", description));
        }
    }

    public static final double parseDouble(final String value, final String postfix) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Inproper number format of %s '%s'", postfix,
                    value));
        }
    }

    public static final void checkIfDescriptorDoesNotContainMultipleModels(
            final Structure descriptorStructure, final String prefix) {
        if (descriptorStructure.nrModels() > 1) {
            throw new IllegalArgumentException(String.format("%s descriptor cannot include multiple models",
                    prefix));
        }
    }

    public static void checkIfFileExistsAndIsNotADirectory(final File file, final String prefix) {
        if (!((file.exists()) && (!file.isDirectory()))) {
            throw new IllegalArgumentException(String.format("%s file '%s' doesn't exist or is a directory",
                    prefix, file.getAbsolutePath()));
        }
    }

    public static final void checkIfFileNotExistOrExistsAndIsNotADirectory(final File file,
            final String prefix) {
        if (!((!file.exists()) || ((file.exists()) && (!file.isDirectory())))) {
            throw new IllegalArgumentException(String.format("%s file '%s' exists and is a directory",
                    prefix, file.getAbsolutePath()));
        }
    }

    public static final void checkIfStringIsBlank(final String value, final String prefix) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(String.format("%s cannot be blank and should be defined",
                    prefix));
        }
    }

    public static final void checkIfMoleculeTypesOfComparedDescriptorsAreConsistent(
            final StructureExtension firstDescriptorExtendedStructure,
            final StructureExtension secondDescriptorExtendedStructure)
            throws UncomparableDescriptorsException {
        if (firstDescriptorExtendedStructure.getMoleculeType() != secondDescriptorExtendedStructure
                .getMoleculeType()) {
            throw new UncomparableDescriptorsException(
                    String.format(
                            "There is no possibility to compare descriptors originated from different molecule types [%s, %s]",
                            firstDescriptorExtendedStructure.getMoleculeType(),
                            secondDescriptorExtendedStructure.getMoleculeType()));
        }
    }

    public static final void checkIfDescriptorsAreComparable(final DescriptorsPair descriptorsPair)
            throws UncomparableDescriptorsException {
        if (descriptorsPair.areDescriptorsUncomparable()) {
            throw new UncomparableDescriptorsException(
                    String.format(
                            "There is no possibility to compare descriptors constructed using different element size [%d, %d]",
                            descriptorsPair.getFirstDescriptorElementSize(),
                            descriptorsPair.getSecondDescriptorElementSize()));
        }
    }

    public static final <T extends CommonInputModel> void checkIfInstanceOfInputModelIsAsExpectedOne(
            final T inputModel, final Class<?> usedClass, final Class<?> expectedClass) {
        if (!expectedClass.isInstance(inputModel)) {
            throw new IllegalArgumentException(String.format(
                    "'%s' input model class was used but '%s' was expected", usedClass.getName(),
                    expectedClass.getName()));
        }
    }

    public static final <T> void checkIfInputListsHaveEqualSizes(final List<T> firstList,
            final List<T> secondList, final String prefix, final String differenceType) {
        final int firstListSize = CollectionUtils.size(firstList);
        final int secondListSize = CollectionUtils.size(secondList);
        if (firstListSize != secondListSize) {
            throw new IllegalArgumentException(String.format("%s that are varying with %s [%d, %d]", prefix,
                    differenceType, firstListSize, secondListSize));
        }
    }
}

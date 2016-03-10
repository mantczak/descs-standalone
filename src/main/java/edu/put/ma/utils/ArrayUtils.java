package edu.put.ma.utils;

import java.util.Arrays;

import org.apache.commons.lang3.EnumUtils;

import com.google.common.base.Preconditions;

public final class ArrayUtils {

    private ArrayUtils() {
        // hidden constructor
    }

    public static final double[][] clone(final double[][] sourceMatrix) {
        Preconditions.checkNotNull(sourceMatrix, "Source matrix should be initialized properly");
        final int rowsNo = org.apache.commons.lang3.ArrayUtils.getLength(sourceMatrix);
        double[][] destinationMatrix = new double[rowsNo][];
        for (int rowIndex = 0; rowIndex < rowsNo; rowIndex++) {
            final int elementsNo = org.apache.commons.lang3.ArrayUtils.getLength(sourceMatrix[rowIndex]);
            destinationMatrix[rowIndex] = Arrays.copyOf(sourceMatrix[rowIndex], elementsNo);
        }
        return destinationMatrix;
    }

    public static final <E extends Enum<E>> String getEnumNamesString(final Class<E> enumClass) {
        final StringBuilder result = new StringBuilder();
        for (E enumValue : EnumUtils.getEnumList(enumClass)) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(enumValue.name());
        }
        return result.toString();
    }
}

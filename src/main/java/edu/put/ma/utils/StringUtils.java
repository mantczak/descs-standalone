package edu.put.ma.utils;

public final class StringUtils {

    public static final char NEW_LINE = '\n';

    public static final boolean ALIGNED_LEFT_MODE = true;

    private StringUtils() {
        // hidden constructor
    }

    public static final String deleteLastCharacter(final String input) {
        final int inputLength = org.apache.commons.lang3.StringUtils.length(input);
        if (inputLength > 1) {
            return org.apache.commons.lang3.StringUtils.substring(input, 0, inputLength - 1);
        }
        return input;
    }

    public static final String extendString(final String input, final int expectedSize, final char character,
            final boolean alignedLeft) {
        final int inputLength = org.apache.commons.lang3.StringUtils.length(input);
        final StringBuilder result = new StringBuilder();
        if (alignedLeft) {
            result.append(input).append(
                    org.apache.commons.lang3.StringUtils.repeat(character, expectedSize - inputLength));
        } else {
            result.append(org.apache.commons.lang3.StringUtils.repeat(character, expectedSize - inputLength))
                    .append(input);
        }
        return result.toString();
    }

}

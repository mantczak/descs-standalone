package edu.put.ma.descs.contacts;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter
public enum ComponentPrefixes {
    DISTANCES('Z'), NUMBERS('Y'), COMPLEX_EXPRESSIONS('X'), COMPARISONS('W'), LOGICAL_FUNCTIONS('V');

    private final char singleLetterCode;

    ComponentPrefixes(final char singleLetterCode) {
        this.singleLetterCode = singleLetterCode;
    }

    public static ComponentPrefixes fromString(final char singleLetterCode) {
        if (StringUtils.isNotBlank(String.valueOf(singleLetterCode))) {
            for (ComponentPrefixes prefix : ComponentPrefixes.values()) {
                if (singleLetterCode == prefix.singleLetterCode) {
                    return prefix;
                }
            }
        }
        throw new IllegalArgumentException(String.format(
                "There is no value with singleLetterCode '%c' in Enum %s", singleLetterCode,
                ComponentPrefixes.class.getName()));
    }
}

package edu.put.ma.utils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public final class MmcifUtils {

    public static final String LABEL_ASYM_ID_FIELD_NAME = "label_asym_id";

    public static final String FIELD_NAMES_PREFIX = "_atom_site.";

    public static final String MMCIF_RECORD_LABEL = "mmCIF record";

    public static final int ASCII_CODE_OF_LOWER_CASE_Z = (int) 'z';

    public static final int ASCII_CODE_OF_LOWER_CASE_A = (int) 'a';

    public static final int ASCII_CODE_OF_CAPITAL_Z = (int) 'Z';

    public static final int ASCII_CODE_OF_CAPITAL_A = (int) 'A';

    public static final int ASCII_CODE_OF_ZERO = (int) '0';

    private MmcifUtils() {
        // hidden constructor
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static final class Field {
        private int startIndex;

        private int endIndex;

        public void move(int lengthDifference) {
            this.startIndex += lengthDifference;
            this.endIndex += lengthDifference;
        }
    }

    public static final void addFieldLabel(final List<String> fieldLabels, final String mmcifRecord) {
        final String fieldName = StringUtils.trim(MmcifUtils.getFieldLabel(mmcifRecord,
                StringUtils.length(FIELD_NAMES_PREFIX)));
        PreconditionUtils.checkIfStringIsBlank(fieldName, "Field name");
        fieldLabels.add(fieldName);
    }

    public static final String getFieldLabel(final String fieldDescription, final int prefixlength) {
        PreconditionUtils.checkIfStringIsBlank(fieldDescription, "Field description");
        return StringUtils.containsWhitespace(fieldDescription) ? StringUtils.substring(
                StringUtils.deleteWhitespace(fieldDescription), prefixlength) : StringUtils.substring(
                fieldDescription, prefixlength);
    }

    public static final List<Field> computeFieldsDistribution(final String mmcifRecord) {
        PreconditionUtils.checkIfStringIsBlank(mmcifRecord, MMCIF_RECORD_LABEL);
        final List<Field> fieldsDistribution = Lists.newArrayList();
        final Pattern whitespacesPattern = Pattern.compile("\\s+");
        final Matcher fieldsInspector = whitespacesPattern.matcher(mmcifRecord);
        int startIndex = 0;
        while (fieldsInspector.find()) {
            fieldsDistribution.add(new Field(startIndex, fieldsInspector.start()));
            startIndex = fieldsInspector.end();
        }
        return Collections.unmodifiableList(fieldsDistribution);
    }

    public static final String getFieldString(final String mmcifRecord, final Field field) {
        PreconditionUtils.checkIfStringIsBlank(mmcifRecord, MMCIF_RECORD_LABEL);
        Preconditions.checkNotNull(field, "There is no a particular field");
        return mmcifRecord.substring(field.startIndex, field.endIndex);
    }

    public static final Field getFieldByName(final String fieldName, final List<String> fieldLabels,
            final List<Field> fieldsDistribution) {
        PreconditionUtils.checkIfStringIsBlank(fieldName, "Field name");
        final int fieldIndex = fieldLabels.indexOf(fieldName);
        PreconditionUtils.checkIfIndexInRange(fieldIndex, 0, CollectionUtils.size(fieldsDistribution),
                "Fields distribution");
        return fieldsDistribution.get(fieldIndex);
    }

}

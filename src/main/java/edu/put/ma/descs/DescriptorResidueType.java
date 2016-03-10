package edu.put.ma.descs;

import lombok.Getter;

import org.apache.commons.lang3.ArrayUtils;

public enum DescriptorResidueType {
    ORIGIN_CENTER(1.0), OTHER_CENTER(2.0), OTHER(0.0);

    @Getter
    private final double flag;

    DescriptorResidueType(final double flag) {
        this.flag = flag;
    }

    public static DescriptorResidueType fromFlag(final double temperatureFactor) {
        final DescriptorResidueType[] types = DescriptorResidueType.values();
        final int typesCount = ArrayUtils.getLength(types);
        for (int typeIndex = 0; typeIndex < typesCount - 1; typeIndex++) {
            if (Double.compare(temperatureFactor, types[typeIndex].flag) == 0) {
                return types[typeIndex];
            }
        }
        return OTHER;
    }
}

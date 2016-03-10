package edu.put.ma.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AlignedDuplexesPair implements Comparable<AlignedDuplexesPair> {

    private final int firstDescriptorOtherElementIndex;

    private final int secondDescriptorOtherElementIndex;

    private final double duplexesPairAlignmentRmsd;

    @Override
    public boolean equals(final Object object) {
        if (object instanceof AlignedDuplexesPair) {
            final AlignedDuplexesPair alignedDuplexexPair = (AlignedDuplexesPair) object;
            return this.compareTo(alignedDuplexexPair) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public int compareTo(final AlignedDuplexesPair alignedDuplexexPair) {
        return Double.compare(duplexesPairAlignmentRmsd, alignedDuplexexPair.duplexesPairAlignmentRmsd);
    }

    @Override
    public String toString() {
        return new StringBuilder().append(firstDescriptorOtherElementIndex).append(':')
                .append(secondDescriptorOtherElementIndex).append('-').append(duplexesPairAlignmentRmsd)
                .toString();
    }
}

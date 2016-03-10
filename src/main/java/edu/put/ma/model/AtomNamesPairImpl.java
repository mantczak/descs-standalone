package edu.put.ma.model;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AtomNamesPairImpl implements AtomNamesPair {

    private final String firstAtomName;

    private final String secondAtomName;

    @Override
    public boolean isSingleComponent() {
        return StringUtils.equalsIgnoreCase(firstAtomName, secondAtomName);
    }

    @Override
    public String toString() {
        if (isSingleComponent()) {
            return firstAtomName;
        }
        return new StringBuilder(firstAtomName).append(";").append(secondAtomName).toString();
    }
}

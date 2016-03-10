package edu.put.ma.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.biojava.nbio.structure.Group;

import com.google.common.collect.ImmutableSet;

@RequiredArgsConstructor
public class AtomImpl implements Atom {
    private final ImmutableSet<String> alternativeNames;

    @Getter
    private final String name;

    @Override
    public boolean isAppropriateAtom(final String name) {
        return alternativeNames.contains(name);
    }

    @Override
    public boolean isAtomIncluded(final Group residue) {
        for (String name : alternativeNames) {
            if (residue.hasAtom(name)) {
                return true;
            }
        }
        return false;
    }
}

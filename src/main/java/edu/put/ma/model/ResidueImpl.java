package edu.put.ma.model;

import org.biojava.nbio.structure.Group;

import com.google.common.collect.ImmutableSet;
import org.biojava.nbio.structure.Atom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class ResidueImpl implements Residue {
    private final ImmutableSet<String> alternativeCodes;

    private final String singleLetterCode;

    private final String threeLettersCode;

    private final Backbone backbone;

    @Override
    public boolean isAppropriateResiude(final String code) {
        return alternativeCodes.contains(code);
    }

    @Override
    public final Atom getBackboneCentroid(final Group residue) {
        return backbone.getBackboneCentroid(residue);
    }

    @Override
    public final boolean isBackboneComplete(final Group residue) {
        return backbone.isComplete(residue);
    }

    @Override
    public final boolean considersAtom(final String atomName) {
        return !isAtomCanBeSkipped(atomName);
    }

    public abstract boolean isComplete(final Group residue);

    public abstract boolean isAtomCanBeSkipped(final String atomName);

    public abstract String getConsistentAtomName(final String atomName);

    protected boolean isAtomConsideredByBackbone(final String atomName) {
        return backbone.isAtomConsidered(atomName);
    }

    protected String getConsistentNameOfAtomCoveredByBackbone(final String atomName) {
        return backbone.getConsistentAtomName(atomName);
    }

}

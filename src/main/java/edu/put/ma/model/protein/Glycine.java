package edu.put.ma.model.protein;

import org.biojava.nbio.structure.Group;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;

public class Glycine extends AminoAcidImpl {

    private static final String THREE_LETTERS_CODE = "GLY";

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("G", "GLY", "ACY", "GL3");

    private static final String SINGLE_LETTER_CODE = "G";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of();

    public Glycine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }

    @Override
    public org.biojava.nbio.structure.Atom getSideChainCentroid(final Group residue) {
        return getRealAtomAsVirtual(residue, "CA", "SCGC");
    }
}

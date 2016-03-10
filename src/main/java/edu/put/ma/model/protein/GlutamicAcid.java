package edu.put.ma.model.protein;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class GlutamicAcid extends AminoAcidImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("E", "GLU", "CGU");

    private static final String SINGLE_LETTER_CODE = "E";

    private static final String THREE_LETTERS_CODE = "GLU";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("CB"), "CB"), new AtomImpl(ImmutableSet.of("CG"), "CG"), new AtomImpl(
            ImmutableSet.of("CD"), "CD"), new AtomImpl(ImmutableSet.of("OE1"), "OE1"), new AtomImpl(
            ImmutableSet.of("OE2"), "OE2"));

    public GlutamicAcid() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }
}

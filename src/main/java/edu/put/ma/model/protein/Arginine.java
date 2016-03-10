package edu.put.ma.model.protein;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Arginine extends AminoAcidImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("R", "ARG", "AAR", "AGM",
            "OPR");

    private static final String SINGLE_LETTER_CODE = "R";

    private static final String THREE_LETTERS_CODE = "ARG";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("CB"), "CB"), new AtomImpl(ImmutableSet.of("CG"), "CG"), new AtomImpl(
            ImmutableSet.of("CD"), "CD"), new AtomImpl(ImmutableSet.of("NE"), "NE"), new AtomImpl(
            ImmutableSet.of("CZ"), "CZ"), new AtomImpl(ImmutableSet.of("NH1"), "NH1"), new AtomImpl(
            ImmutableSet.of("NH2"), "NH2"));

    public Arginine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }
}

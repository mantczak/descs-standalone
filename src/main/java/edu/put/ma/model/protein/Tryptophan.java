package edu.put.ma.model.protein;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Tryptophan extends AminoAcidImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("W", "TRP", "FTR", "HTR",
            "TRF", "TRN", "TRO");

    private static final String SINGLE_LETTER_CODE = "W";

    private static final String THREE_LETTERS_CODE = "TRP";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("CB"), "CB"), new AtomImpl(ImmutableSet.of("CG"), "CG"), new AtomImpl(
            ImmutableSet.of("CD1"), "CD1"), new AtomImpl(ImmutableSet.of("CD2"), "CD2"), new AtomImpl(
            ImmutableSet.of("NE1"), "NE1"), new AtomImpl(ImmutableSet.of("CE2"), "CE2"), new AtomImpl(
            ImmutableSet.of("CE3"), "CE3"), new AtomImpl(ImmutableSet.of("CZ2"), "CZ2"), new AtomImpl(
            ImmutableSet.of("CZ3"), "CZ3"), new AtomImpl(ImmutableSet.of("CH2"), "CH2"));

    public Tryptophan() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }

}

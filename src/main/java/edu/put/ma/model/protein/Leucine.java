package edu.put.ma.model.protein;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Leucine extends AminoAcidImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("L", "LEU");

    private static final String SINGLE_LETTER_CODE = "L";

    private static final String THREE_LETTERS_CODE = "LEU";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("CB"), "CB"), new AtomImpl(ImmutableSet.of("CG"), "CG"), new AtomImpl(
            ImmutableSet.of("CD1"), "CD1"), new AtomImpl(ImmutableSet.of("CD2"), "CD2"));

    public Leucine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }

}

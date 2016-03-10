package edu.put.ma.model.protein;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Isoleucine extends AminoAcidImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("I", "ILE");

    private static final String SINGLE_LETTER_CODE = "I";

    private static final String THREE_LETTERS_CODE = "ILE";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("CB"), "CB"), new AtomImpl(ImmutableSet.of("CG1"), "CG1"), new AtomImpl(
            ImmutableSet.of("CG2"), "CG2"), new AtomImpl(ImmutableSet.of("CD1"), "CD1"));

    public Isoleucine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }

}

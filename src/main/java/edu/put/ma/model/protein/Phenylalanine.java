package edu.put.ma.model.protein;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Phenylalanine extends AminoAcidImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("F", "PHE", "EHP");

    private static final String SINGLE_LETTER_CODE = "F";

    private static final String THREE_LETTERS_CODE = "PHE";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("CB"), "CB"), new AtomImpl(ImmutableSet.of("CG"), "CG"), new AtomImpl(
            ImmutableSet.of("CD1"), "CD1"), new AtomImpl(ImmutableSet.of("CD2"), "CD2"), new AtomImpl(
            ImmutableSet.of("CE1"), "CE1"), new AtomImpl(ImmutableSet.of("CE2"), "CE2"), new AtomImpl(
            ImmutableSet.of("CZ"), "CZ"));

    public Phenylalanine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }

}

package edu.put.ma.model.protein;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Lysine extends AminoAcidImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("K", "LYS", "INI", "KCX",
            "LLP", "LLY", "LYZ", "M3L", "MLY", "MLZ");

    private static final String SINGLE_LETTER_CODE = "K";

    private static final String THREE_LETTERS_CODE = "LYS";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("CB"), "CB"), new AtomImpl(ImmutableSet.of("CG"), "CG"), new AtomImpl(
            ImmutableSet.of("CD"), "CD"), new AtomImpl(ImmutableSet.of("CE"), "CE"), new AtomImpl(
            ImmutableSet.of("NZ"), "NZ"));

    public Lysine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }

}

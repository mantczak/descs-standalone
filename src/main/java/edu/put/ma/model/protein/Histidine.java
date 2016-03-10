package edu.put.ma.model.protein;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Histidine extends AminoAcidImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("H", "HIS", "H2P", "HIC",
            "HIP", "MHS", "NEP");

    private static final String SINGLE_LETTER_CODE = "H";

    private static final String THREE_LETTERS_CODE = "HIS";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("CB"), "CB"), new AtomImpl(ImmutableSet.of("CG"), "CG"), new AtomImpl(
            ImmutableSet.of("ND1"), "ND1"), new AtomImpl(ImmutableSet.of("CD2"), "CD2"), new AtomImpl(
            ImmutableSet.of("CE1"), "CE1"), new AtomImpl(ImmutableSet.of("NE2"), "NE2"));

    public Histidine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }

}

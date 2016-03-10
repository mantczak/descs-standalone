package edu.put.ma.model.protein;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class AsparticAcid extends AminoAcidImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("D", "ASP", "ASQ", "BHD",
            "DOH", "PHD", "SNN");

    private static final String SINGLE_LETTER_CODE = "D";

    private static final String THREE_LETTERS_CODE = "ASP";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("CB"), "CB"), new AtomImpl(ImmutableSet.of("CG"), "CG"), new AtomImpl(
            ImmutableSet.of("OD1"), "OD1"), new AtomImpl(ImmutableSet.of("OD2"), "OD2"));

    public AsparticAcid() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }
}

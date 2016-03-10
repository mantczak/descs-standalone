package edu.put.ma.model.protein;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Cysteine extends AminoAcidImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("C", "CYS", "CAS", "CAY",
            "CEA", "CME", "CMT", "CSB", "CSD", "CSE", "CSO", "CSP", "CSS", "CSW", "CSX", "CYG", "CYM", "NPH",
            "OCS", "OCY", "PYX", "SMC", "SNC");

    private static final String SINGLE_LETTER_CODE = "C";

    private static final String THREE_LETTERS_CODE = "CYS";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("CB"), "CB"), new AtomImpl(ImmutableSet.of("SG"), "SG"));

    public Cysteine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }
}

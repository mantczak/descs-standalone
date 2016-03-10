package edu.put.ma.model.rna;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Cytosine extends NucleotideImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("C", "CYT", "C5", "C3",
            "RC3", "RC5", "RC", "CR", "CCC");

    private static final String SINGLE_LETTER_CODE = "C";

    private static final String THREE_LETTERS_CODE = "CYT";

    private static final ImmutableSet<Atom> BASE_ATOMS = ImmutableSet.of(
            (Atom) new AtomImpl(ImmutableSet.of("N1"), "N1"), new AtomImpl(ImmutableSet.of("C2"), "C2"),
            new AtomImpl(ImmutableSet.of("O2"), "O2"), new AtomImpl(ImmutableSet.of("N3"), "N3"),
            new AtomImpl(ImmutableSet.of("C4"), "C4"), new AtomImpl(ImmutableSet.of("N4"), "N4"),
            new AtomImpl(ImmutableSet.of("C5"), "C5"), new AtomImpl(ImmutableSet.of("C6"), "C6"));

    public Cytosine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, BASE_ATOMS);
    }
}

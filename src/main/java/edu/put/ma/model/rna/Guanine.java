package edu.put.ma.model.rna;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Guanine extends NucleotideImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("G", "GUA", "G5", "G3",
            "RG3", "RG5", "RG", "GR", "5CA", "GTP");

    private static final String SINGLE_LETTER_CODE = "G";

    private static final String THREE_LETTERS_CODE = "GUA";

    private static final ImmutableSet<Atom> BASE_ATOMS = ImmutableSet.of(
            (Atom) new AtomImpl(ImmutableSet.of("N9"), "N9"), new AtomImpl(ImmutableSet.of("C8"), "C8"),
            new AtomImpl(ImmutableSet.of("N7"), "N7"), new AtomImpl(ImmutableSet.of("C5"), "C5"),
            new AtomImpl(ImmutableSet.of("C6"), "C6"), new AtomImpl(ImmutableSet.of("O6"), "O6"),
            new AtomImpl(ImmutableSet.of("N1"), "N1"), new AtomImpl(ImmutableSet.of("C2"), "C2"),
            new AtomImpl(ImmutableSet.of("N2"), "N2"), new AtomImpl(ImmutableSet.of("N3"), "N3"),
            new AtomImpl(ImmutableSet.of("C4"), "C4"));

    public Guanine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, BASE_ATOMS);
    }

}

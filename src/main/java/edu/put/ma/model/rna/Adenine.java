package edu.put.ma.model.rna;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Adenine extends NucleotideImpl {

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("A", "ADE", "A5", "A3",
            "RA3", "RA5", "RA", "AR", "2BA", "AMP");

    private static final String SINGLE_LETTER_CODE = "A";

    private static final String THREE_LETTERS_CODE = "ADE";

    private static final ImmutableSet<Atom> BASE_ATOMS = ImmutableSet.of(
            (Atom) new AtomImpl(ImmutableSet.of("N9"), "N9"), new AtomImpl(ImmutableSet.of("C8"), "C8"),
            new AtomImpl(ImmutableSet.of("N7"), "N7"), new AtomImpl(ImmutableSet.of("C5"), "C5"),
            new AtomImpl(ImmutableSet.of("C6"), "C6"), new AtomImpl(ImmutableSet.of("N6"), "N6"),
            new AtomImpl(ImmutableSet.of("N1"), "N1"), new AtomImpl(ImmutableSet.of("C2"), "C2"),
            new AtomImpl(ImmutableSet.of("N3"), "N3"), new AtomImpl(ImmutableSet.of("C4"), "C4"));

    public Adenine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, BASE_ATOMS);
    }

}

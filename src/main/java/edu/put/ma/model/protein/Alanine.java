package edu.put.ma.model.protein;

import org.biojava.nbio.structure.Group;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;

public class Alanine extends AminoAcidImpl {

    private static final String THREE_LETTERS_CODE = "ALA";

    private static final ImmutableSet<String> ALTERNATIVE_CODES = ImmutableSet.of("A", "ALA", "AYA");

    private static final String SINGLE_LETTER_CODE = "A";

    private static final ImmutableSet<Atom> SIDE_CHAIN_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("CB"), "CB"));

    public Alanine() {
        super(ALTERNATIVE_CODES, SINGLE_LETTER_CODE, THREE_LETTERS_CODE, SIDE_CHAIN_ATOMS);
    }

    @Override
    public org.biojava.nbio.structure.Atom getSideChainCentroid(final Group residue) {
        return getRealAtomAsVirtual(residue, "CB", "SCGC");
    }
}

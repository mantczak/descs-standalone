package edu.put.ma.model;

import java.util.List;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

public final class AlignmentFactory {

    private AlignmentFactory() {
        // hidden constructor
    }

    public static final Alignment construct(final List<Group> firstDescriptorResidues,
            final List<Atom> firstDescriptorAtoms, final List<Group> secondDescriptorResidues,
            final List<Atom> secondDescriptorAtoms) {
        return new AlignmentImpl(firstDescriptorResidues, firstDescriptorAtoms, secondDescriptorResidues,
                secondDescriptorAtoms);
    }
}

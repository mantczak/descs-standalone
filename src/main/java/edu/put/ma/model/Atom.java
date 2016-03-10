package edu.put.ma.model;

import org.biojava.nbio.structure.Group;

public interface Atom {

    String getName();

    boolean isAppropriateAtom(String name);

    boolean isAtomIncluded(Group residue);
}

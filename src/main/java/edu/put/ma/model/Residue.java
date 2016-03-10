package edu.put.ma.model;

import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Atom;

public interface Residue {

    String getSingleLetterCode();

    String getThreeLettersCode();

    boolean isAppropriateResiude(String code);

    Atom getBackboneCentroid(Group residue);

    boolean isBackboneComplete(Group residue);

    boolean considersAtom(String atomName);

    boolean isComplete(Group residue);

    boolean isAtomCanBeSkipped(String atomName);

    String getConsistentAtomName(String atomName);
}

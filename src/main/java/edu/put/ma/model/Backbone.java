package edu.put.ma.model;

import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Atom;

public interface Backbone {

    boolean isComplete(Group residue);
    
    boolean isAtomConsidered(String atomName);
    
    String getConsistentAtomName(String atomName);
    
    Atom getBackboneCentroid(Group residue);
}

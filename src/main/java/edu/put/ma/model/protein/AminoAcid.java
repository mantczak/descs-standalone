package edu.put.ma.model.protein;

import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Atom;

import edu.put.ma.model.Residue;

public interface AminoAcid extends Residue {

    boolean isSideChainComplete(Group residue);

    Atom getSideChainCentroid(Group residue);

    Atom getCbx(Group residue);

    Atom getVcb(Group residue);
}

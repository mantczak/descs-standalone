package edu.put.ma.model.rna;

import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Atom;
import edu.put.ma.model.Residue;

public interface Nucleotide extends Residue {

    boolean isRiboseComplete(Group residue);

    boolean isBaseComplete(Group residue);

    Atom getRiboseCentroid(Group residue);

    Atom getBaseCentroid(Group residue);
}

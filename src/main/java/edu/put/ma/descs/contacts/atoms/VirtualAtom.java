package edu.put.ma.descs.contacts.atoms;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import edu.put.ma.model.Residue;

public interface VirtualAtom {

    Atom get(Group residue, Residue entry);
}

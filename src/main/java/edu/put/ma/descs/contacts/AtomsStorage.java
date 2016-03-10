package edu.put.ma.descs.contacts;

import java.util.regex.Pattern;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import edu.put.ma.model.Residue;

public interface AtomsStorage {

    void prepareStorage();

    Atom getAtom(Group residue, Residue residueEntry, String atomName, Pattern virtualAtomNamesPattern);
}

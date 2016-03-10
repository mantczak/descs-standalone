package edu.put.ma.descs.contacts.atoms;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import edu.put.ma.model.Residue;

public class BackboneCentroid implements VirtualAtom {

    @Override
    public Atom get(final Group residue, final Residue entry) {
        return entry.getBackboneCentroid(residue);
    }

}

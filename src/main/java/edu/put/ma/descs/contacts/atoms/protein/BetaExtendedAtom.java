package edu.put.ma.descs.contacts.atoms.protein;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import edu.put.ma.descs.contacts.atoms.VirtualAtom;
import edu.put.ma.model.Residue;
import edu.put.ma.model.protein.AminoAcid;

public class BetaExtendedAtom implements VirtualAtom {

    @Override
    public Atom get(final Group residue, final Residue entry) {
        return ((AminoAcid) entry).getCbx(residue);
    }
}

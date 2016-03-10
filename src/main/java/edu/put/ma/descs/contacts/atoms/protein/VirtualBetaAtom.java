package edu.put.ma.descs.contacts.atoms.protein;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import edu.put.ma.descs.contacts.atoms.VirtualAtom;
import edu.put.ma.model.Residue;
import edu.put.ma.model.protein.AminoAcid;

public class VirtualBetaAtom implements VirtualAtom {

    @Override
    public Atom get(Group residue, Residue entry) {
        return ((AminoAcid) entry).getVcb(residue);
    }

}

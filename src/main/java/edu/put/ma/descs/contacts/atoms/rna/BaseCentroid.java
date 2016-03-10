package edu.put.ma.descs.contacts.atoms.rna;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import edu.put.ma.descs.contacts.atoms.VirtualAtom;
import edu.put.ma.model.Residue;
import edu.put.ma.model.rna.Nucleotide;

public class BaseCentroid implements VirtualAtom {

    @Override
    public Atom get(final Group residue, final Residue entry) {
        return ((Nucleotide) entry).getBaseCentroid(residue);
    }

}

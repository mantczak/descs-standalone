package edu.put.ma.descs.contacts.atoms;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import edu.put.ma.descs.contacts.atoms.protein.BetaExtendedAtom;
import edu.put.ma.descs.contacts.atoms.protein.SideChainCentroid;
import edu.put.ma.descs.contacts.atoms.protein.VirtualBetaAtom;
import edu.put.ma.descs.contacts.atoms.rna.BaseCentroid;
import edu.put.ma.descs.contacts.atoms.rna.RiboseCentroid;
import edu.put.ma.model.Residue;

public enum VirtualAtoms {
    BBGC(new BackboneCentroid()), BBC(new BackboneCentroid()), SCGC(new SideChainCentroid()), CBX(
            new BetaExtendedAtom()), VCB(new VirtualBetaAtom()), SCC(new SideChainCentroid()), RBGC(
            new RiboseCentroid()), BSGC(new BaseCentroid()), RBC(new RiboseCentroid()), BSC(
            new BaseCentroid());

    private final VirtualAtom atom;

    VirtualAtoms(final VirtualAtom atom) {
        this.atom = atom;
    }

    public Atom getAtom(final Group residue, final Residue entry) {
        return atom.get(residue, entry);
    }
}

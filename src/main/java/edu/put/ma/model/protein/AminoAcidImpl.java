package edu.put.ma.model.protein;

import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.Group;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;
import edu.put.ma.model.BackboneFactory;
import edu.put.ma.model.ResidueImpl;
import edu.put.ma.utils.ResidueUtils;

public abstract class AminoAcidImpl extends ResidueImpl implements AminoAcid {

    private static final ImmutableSet<Atom> OBLIGATORY_BB_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("N"), "N"), new AtomImpl(ImmutableSet.of("CA"), "CA"),
            new AtomImpl(ImmutableSet.of("C"), "C"));

    private static final ImmutableSet<Atom> OPTIONAL_BB_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("O"), "O"));

    private final ImmutableSet<Atom> sideChainAtoms;

    protected AminoAcidImpl(final ImmutableSet<String> alternativeCodes, final String singleLetterCode,
            final String threeLettersCode, final ImmutableSet<Atom> sideChainAtoms) {
        super(alternativeCodes, singleLetterCode, threeLettersCode, BackboneFactory.construct(
                OBLIGATORY_BB_ATOMS, OPTIONAL_BB_ATOMS));
        this.sideChainAtoms = sideChainAtoms;
    }

    @Override
    public boolean isSideChainComplete(final Group residue) {
        return ResidueUtils.isAllAtomsIncluded(sideChainAtoms, residue);
    }

    @Override
    public boolean isComplete(final Group residue) {
        return isBackboneComplete(residue) && isSideChainComplete(residue);
    }

    @Override
    public boolean isAtomCanBeSkipped(final String atomName) {
        return !isAtomConsideredByBackbone(atomName)
                && !ResidueUtils.isAtomConsidered(sideChainAtoms, atomName);
    }

    @Override
    public String getConsistentAtomName(final String atomName) {
        String consistentAtomName = getConsistentNameOfAtomCoveredByBackbone(atomName);
        if (StringUtils.isBlank(consistentAtomName)) {
            consistentAtomName = ResidueUtils.getConsistentAtomName(sideChainAtoms, atomName);
        }
        return consistentAtomName;
    }

    @Override
    public org.biojava.nbio.structure.Atom getSideChainCentroid(final Group residue) {
        return ResidueUtils.getCentroidExceptFirstAtom(sideChainAtoms, residue, "SCGC");
    }

    @Override
    public org.biojava.nbio.structure.Atom getCbx(final Group residue) {
        return ResidueUtils.getCbxAtom(residue);
    }

    @Override
    public org.biojava.nbio.structure.Atom getVcb(final Group residue) {
        return ResidueUtils.getVirtualCbAtom(residue);
    }

    protected static org.biojava.nbio.structure.Atom getRealAtomAsVirtual(final Group residue,
            final String realAtomName, final String virtualAtomName) {
        final org.biojava.nbio.structure.Atom realAtom = residue.getAtom(realAtomName);
        Preconditions.checkNotNull(realAtom, String.format("There is no atom '%s' in residue '%s'", realAtom,
                residue.getResidueNumber().printFull()));
        final org.biojava.nbio.structure.Atom virtualAtom = (org.biojava.nbio.structure.Atom) realAtom
                .clone();
        virtualAtom.setName(virtualAtomName);
        virtualAtom.setAltLoc(' ');
        virtualAtom.setElement(Element.R);
        virtualAtom.setPDBserial(0);
        virtualAtom.setGroup(residue);
        return virtualAtom;
    }
}

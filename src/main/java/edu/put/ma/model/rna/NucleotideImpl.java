package edu.put.ma.model.rna;

import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Group;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.model.Atom;
import edu.put.ma.model.AtomImpl;
import edu.put.ma.model.BackboneFactory;
import edu.put.ma.model.ResidueImpl;
import edu.put.ma.utils.ResidueUtils;

public abstract class NucleotideImpl extends ResidueImpl implements Nucleotide {

    private static final ImmutableSet<Atom> OBLIGATORY_BB_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("O5*", "O5'", "O5`", "O5T"), "O5'"),
            new AtomImpl(ImmutableSet.of("C5*", "C5'", "C5`"), "C5'"),
            new AtomImpl(ImmutableSet.of("C4*", "C4'", "C4`"), "C4'"),
            new AtomImpl(ImmutableSet.of("C3*", "C3'", "C3`"), "C3'"),
            new AtomImpl(ImmutableSet.of("O3*", "O3'", "O3`"), "O3'"));

    private static final ImmutableSet<Atom> OPTIONAL_BB_ATOMS = ImmutableSet.of((Atom) new AtomImpl(
            ImmutableSet.of("O1P", "OP1", "OL"), "OP1"), new AtomImpl(ImmutableSet.of("O2P", "OP2", "OR"),
            "OP2"), new AtomImpl(ImmutableSet.of("P"), "P"));

    private static final ImmutableSet<Atom> RIBOSE_ATOMS = ImmutableSet.of(
            (Atom) new AtomImpl(ImmutableSet.of("O2*", "O2'", "O2`"), "O2'"),
            new AtomImpl(ImmutableSet.of("O4*", "O4'", "O4`"), "O4'"),
            new AtomImpl(ImmutableSet.of("C1*", "C1'", "C1`"), "C1'"),
            new AtomImpl(ImmutableSet.of("C2*", "C2'", "C2`"), "C2'"));

    private final ImmutableSet<Atom> riboseAtoms;

    private final ImmutableSet<Atom> baseAtoms;

    protected NucleotideImpl(final ImmutableSet<String> alternativeCodes, final String singleLetterCode,
            final String threeLettersCode, final ImmutableSet<Atom> baseAtoms) {
        super(alternativeCodes, singleLetterCode, threeLettersCode, BackboneFactory.construct(
                OBLIGATORY_BB_ATOMS, OPTIONAL_BB_ATOMS));
        this.riboseAtoms = RIBOSE_ATOMS;
        this.baseAtoms = baseAtoms;
    }

    @Override
    public boolean isRiboseComplete(final Group residue) {
        return ResidueUtils.isAllAtomsIncluded(riboseAtoms, residue);
    }

    @Override
    public boolean isBaseComplete(final Group residue) {
        return ResidueUtils.isAllAtomsIncluded(baseAtoms, residue);
    }

    @Override
    public boolean isComplete(final Group residue) {
        return isBackboneComplete(residue) && isRiboseComplete(residue) && isBaseComplete(residue);
    }

    @Override
    public boolean isAtomCanBeSkipped(final String atomName) {
        return !isAtomConsideredByBackbone(atomName) && !ResidueUtils.isAtomConsidered(riboseAtoms, atomName)
                && !ResidueUtils.isAtomConsidered(baseAtoms, atomName);
    }

    @Override
    public String getConsistentAtomName(final String atomName) {
        String consistentAtomName = getConsistentNameOfAtomCoveredByBackbone(atomName);
        if (StringUtils.isBlank(consistentAtomName)) {
            consistentAtomName = ResidueUtils.getConsistentAtomName(riboseAtoms, atomName);
            if (StringUtils.isBlank(consistentAtomName)) {
                consistentAtomName = ResidueUtils.getConsistentAtomName(baseAtoms, atomName);
            }
        }
        return consistentAtomName;
    }

    @Override
    public org.biojava.nbio.structure.Atom getRiboseCentroid(final Group residue) {
        return ResidueUtils.getCentroidExceptFirstAtom(riboseAtoms, residue, "RBGC");
    }

    @Override
    public org.biojava.nbio.structure.Atom getBaseCentroid(final Group residue) {
        return ResidueUtils.getCentroid(baseAtoms, residue, "BSGC");
    }

}

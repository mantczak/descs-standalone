package edu.put.ma.utils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.SVDSuperimposer;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.jama.Matrix;
import org.biojava.nbio.structure.AtomImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import edu.put.ma.model.Atom;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.model.Residue;
import edu.put.ma.model.ResiduesDictionary;
import edu.put.ma.model.StructureType;

public final class ResidueUtils {

    private static final String HETATM_PREFIX = "HETATM";

    private static final String ATOM_PREFIX = "ATOM";

    private static final double ELEMENT_CENTER_FLAG = 2.0;

    private static final double DESCRIPTOR_CENTER_FLAG = 1.0;

    private static final double[] CB_TEMPLATE_COORDINATES = new double[] { 0.0, 0.0, 0.0 };

    private static final double[] C_TEMPLATE_COORDINATES = new double[] { 1.26462, -0.673997, -3.024425 };

    private static final double[] CA_TEMPLATE_COORDINATES = new double[] { 0.0, 0.0, -2.5 };

    private static final double[] N_TEMPLATE_COORDINATES = new double[] { -1.2367, -0.656232, -3.010602 };

    private static final double MAXIMAL_DISTANCE_FOR_CONNECTED_RESIDUES = 2.5;

    private static final String EMPTY_RESIDUE = "Residue should be initialized properly";

    private static final Logger LOGGER = LoggerFactory.getLogger(ResidueUtils.class);

    private ResidueUtils() {
        // hidden constructor
    }

    public static boolean isCoordinatesRecord(final String record) {
        return (StringUtils.startsWith(record, ATOM_PREFIX))
                || (StringUtils.startsWith(record, HETATM_PREFIX));
    }

    public static final boolean isAllAtomsIncluded(final ImmutableSet<Atom> atoms, final Group residue) {
        if (residue == null) {
            throw new IllegalArgumentException(EMPTY_RESIDUE);
        }
        for (Atom atom : atoms) {
            if (!atom.isAtomIncluded(residue)) {
                return false;
            }
        }
        return true;
    }

    public static final org.biojava.nbio.structure.Atom getCentroid(final ImmutableSet<Atom> atoms,
            final Group residue, final String atomName) {
        return getCentroidSinceIndex(atoms, residue, atomName, 0);
    }

    public static final org.biojava.nbio.structure.Atom getCentroidExceptFirstAtom(
            final ImmutableSet<Atom> atoms, final Group residue, final String atomName) {
        return getCentroidSinceIndex(atoms, residue, atomName, 1);
    }

    public static final double ensureCommonDoubleFormat(final double val) {
        return Double.parseDouble(String.format(Locale.US, "%.3f", val));
    }

    public static final void formatCoordinates(final org.biojava.nbio.structure.Atom atom) {
        atom.setX(ensureCommonDoubleFormat(atom.getX()));
        atom.setY(ensureCommonDoubleFormat(atom.getY()));
        atom.setZ(ensureCommonDoubleFormat(atom.getZ()));
    }

    public static final org.biojava.nbio.structure.Atom getVirtualCbAtom(final Group residue) {
        if (residue == null) {
            throw new IllegalArgumentException(EMPTY_RESIDUE);
        }
        final org.biojava.nbio.structure.AminoAcid aminoAcid = (org.biojava.nbio.structure.AminoAcid) residue;
        try {
            final org.biojava.nbio.structure.Atom cbx = Calc.createVirtualCBAtom(aminoAcid);
            unifyProperties(cbx, "VCB", residue);
            return cbx;
        } catch (StructureException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static final org.biojava.nbio.structure.Atom getCbxAtom(final Group residue) {
        if (residue == null) {
            throw new IllegalArgumentException(EMPTY_RESIDUE);
        }
        org.biojava.nbio.structure.Atom aN = new AtomImpl();
        aN.setCoords(N_TEMPLATE_COORDINATES);
        org.biojava.nbio.structure.Atom aCA = new AtomImpl();
        aCA.setCoords(CA_TEMPLATE_COORDINATES);
        org.biojava.nbio.structure.Atom aC = new AtomImpl();
        aC.setCoords(C_TEMPLATE_COORDINATES);
        org.biojava.nbio.structure.Atom aCB = new AtomImpl();
        aCB.setCoords(CB_TEMPLATE_COORDINATES);

        org.biojava.nbio.structure.Atom[] arr1 = new org.biojava.nbio.structure.Atom[] { aN, aCA, aC };

        org.biojava.nbio.structure.Atom[] arr2 = new org.biojava.nbio.structure.Atom[] {
                residue.getAtom("N"), residue.getAtom("CA"), residue.getAtom("C") };

        SVDSuperimposer svd;
        try {
            svd = new SVDSuperimposer(arr2, arr1);

            Matrix rotMatrix = svd.getRotation();
            org.biojava.nbio.structure.Atom tranMatrix = svd.getTranslation();

            Calc.rotate(aCB, rotMatrix);

            org.biojava.nbio.structure.Atom virtualCB = Calc.add(aCB, tranMatrix);
            unifyProperties(virtualCB, "CBX", residue);
            return virtualCB;
        } catch (StructureException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static final boolean isAtomConsidered(final ImmutableSet<Atom> atoms, final String atomName) {
        return getIndexOfConsideredAtom(atoms, atomName) >= 0;
    }

    public static final String getConsistentAtomName(final ImmutableSet<Atom> atoms, final String atomName) {
        final int index = getIndexOfConsideredAtom(atoms, atomName);
        if (index >= 0) {
            return atoms.asList().get(index).getName();
        }
        return null;
    }

    public static final boolean areResiduesConnected(final MoleculeType moleculeType,
            final Group previousResidue, final Group currentResidue) {
        return (moleculeType == MoleculeType.PROTEIN) ? ResidueUtils.areAminoAcidsConnected(previousResidue,
                currentResidue) || ResidueUtils.areAminoAcidsConnected(currentResidue, previousResidue)
                : ResidueUtils.areNucleotidesConnected(previousResidue, currentResidue)
                        || ResidueUtils.areNucleotidesConnected(currentResidue, previousResidue);
    }

    public static final String getResidueKey(final Group residue) {
        return new StringBuilder(residue.getResidueNumber().printFull()).append("_")
                .append(residue.getPDBName()).toString();
    }

    public static final List<Integer> filterResidues(final List<Chain> chains,
            final MoleculeType moleculeType, final StructureType structureType,
            final boolean considerAppropriateResiduesOnly) {
        final ImmutableList<? extends Residue> dictionary = ResiduesDictionary
                .getResidueDictionary(moleculeType);
        final List<Integer> chainsThatShouldBeDeleted = Lists.newArrayList();
        int chainIndex = 0;
        for (Chain chain : chains) {
            final List<Group> residues = chain.getAtomGroups();
            final int residuesCount = CollectionUtils.size(residues);
            if (residuesCount > 0) {
                processResidues(moleculeType, dictionary, residues, residuesCount, structureType,
                        considerAppropriateResiduesOnly);
                if (!ResidueUtils.isAtLeastOneResidueInChain(chain)) {
                    chainsThatShouldBeDeleted.add(chainIndex);
                }
            }
            chainIndex++;
        }
        return Collections.unmodifiableList(chainsThatShouldBeDeleted);
    }

    public static final boolean isAtLeastOneResidueInChain(final Chain chain) {
        final List<Group> residues = chain.getAtomGroups();
        final int residuesCount = CollectionUtils.size(residues);
        return residuesCount > 0;
    }

    private static final org.biojava.nbio.structure.Atom getCentroidSinceIndex(
            final ImmutableSet<Atom> atoms, final Group residue, final String atomName, final int startIndex) {
        final int atomsCount = CollectionUtils.size(atoms);
        if ((atomsCount == 0) || (residue == null)) {
            throw new IllegalArgumentException(EMPTY_RESIDUE + " and atoms set cannot be empty");
        }
        PreconditionUtils.checkIfIndexInRange(startIndex, 0, atomsCount, "Starting");
        final int currentAtomsCount = atomsCount - startIndex;
        final org.biojava.nbio.structure.Atom[] residueAtoms = new org.biojava.nbio.structure.Atom[currentAtomsCount];
        int consideredAtomsNo = 0;
        final ImmutableList<Atom> atomsList = atoms.asList();
        for (int i = startIndex; i < atomsCount; i++) {
            final Atom atom = atomsList.get(i);
            final org.biojava.nbio.structure.Atom residueAtom = residue.getAtom(atom.getName());
            if (residueAtom != null) {
                residueAtoms[consideredAtomsNo++] = residueAtom;
            }
        }
        org.biojava.nbio.structure.Atom centroid = null;
        if (currentAtomsCount == consideredAtomsNo) {
            centroid = Calc.getCentroid(residueAtoms);
            unifyProperties(centroid, atomName, residue);
        }
        return centroid;
    }

    private static final void unifyProperties(final org.biojava.nbio.structure.Atom atom,
            final String atomName, final Group residue) {
        atom.setGroup(residue);
        atom.setAltLoc(' ');
        atom.setName(atomName);
        atom.setElement(Element.R);
        atom.setPDBserial(0);
        formatCoordinates(atom);
    }

    private static final boolean isConnected(final org.biojava.nbio.structure.Atom atom1,
            final org.biojava.nbio.structure.Atom atom2) {
        if ((atom1 == null) || (atom2 == null)) {
            return false;
        }
        return Double.compare(Calc.getDistance(atom1, atom2), MAXIMAL_DISTANCE_FOR_CONNECTED_RESIDUES) < 0;
    }

    private static final int getIndexOfConsideredAtom(final ImmutableSet<Atom> atoms, final String atomName) {
        PreconditionUtils.checkIfStringIsBlank(atomName, "Atom name");
        int index = 0;
        for (Atom atom : atoms) {
            if (atom.isAppropriateAtom(atomName)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private static final void processResidues(final MoleculeType moleculeType,
            final ImmutableList<? extends Residue> dictionary, final List<Group> residues,
            final int residuesCount, final StructureType structureType,
            final boolean considerAppropriateResiduesOnly) {
        for (int residueIndex = residuesCount - 1; residueIndex >= 0; residueIndex--) {
            final Group residue = residues.get(residueIndex);
            String residueKey = ResidueUtils.getResidueKey(residue);
            if ((residue.hasAltLoc()) || (residue.isWater())) {
                if (residue.hasAltLoc()) {
                    LOGGER.warn(String.format(
                            "Residue [%s] is ignored because considers alternate locations", residueKey));
                }
                residues.remove(residueIndex);
            } else {
                residueKey = processResidue(moleculeType, dictionary, residues, residueIndex, residueKey,
                        structureType, considerAppropriateResiduesOnly);
            }
        }
    }

    private static final String processResidue(final MoleculeType moleculeType,
            final ImmutableList<? extends Residue> dictionary, final List<Group> residues,
            final int residueIndex, final String residueKey, final StructureType structureType,
            final boolean considerAppropriateResiduesOnly) {
        final Group residue = residues.get(residueIndex);
        String localResidueKey = residueKey;
        if ((considerAppropriateResiduesOnly) && (isInappropriateResidue(moleculeType, residue))) {
            residues.remove(residueIndex);
        } else {
            final Residue resEntry = getResidueEntryByName(residue.getPDBName(), dictionary, moleculeType);
            if (resEntry != null) {
                final String consistentResName = (moleculeType == MoleculeType.PROTEIN) ? resEntry
                        .getThreeLettersCode() : resEntry.getSingleLetterCode();
                final String residueName = residue.getPDBName();
                if (!StringUtils.equals(residueName, consistentResName)) {
                    LOGGER.warn(String.format("Residue name [%s] is changed for [%s] in residue [%s]",
                            residueName, consistentResName, localResidueKey));
                    residue.setPDBName(consistentResName);
                    localResidueKey = ResidueUtils.getResidueKey(residue);
                }
                if (!resEntry.isComplete(residue)) {
                    LOGGER.warn(String.format("Residue [%s] is ignored as incomplete", localResidueKey));
                    residues.remove(residueIndex);
                } else {
                    updateAtoms(residue, localResidueKey, resEntry, structureType);
                }
            } else {
                LOGGER.warn(String.format("Residue [%s] is ignored as unrecognized",
                        ResidueUtils.getResidueKey(residue)));
                residues.remove(residueIndex);
            }
        }
        return localResidueKey;
    }

    private static final boolean isInappropriateResidue(final MoleculeType moleculeType, final Group residue) {
        return ((moleculeType == MoleculeType.PROTEIN) && (!residue.hasAminoAtoms()))
                || ((moleculeType == MoleculeType.RNA) && (residue.hasAminoAtoms()));
    }

    private static final boolean areNucleotidesConnected(final Group previousResidue,
            final Group currentResidue) {
        return isConnected(previousResidue.getAtom("O3'"), currentResidue.getAtom("P"));
    }

    private static final boolean areAminoAcidsConnected(final Group previousResidue,
            final Group currentResidue) {
        return isConnected(previousResidue.getAtom("C"), currentResidue.getAtom("N"));
    }

    private static final Residue getResidueEntryByName(final String residueName,
            final ImmutableList<? extends Residue> dictionary, final MoleculeType moleculeType) {
        Residue result = null;
        int count = 0;
        for (final Residue resEntry : dictionary) {
            if (resEntry.isAppropriateResiude(residueName)) {
                result = resEntry;
                break;
            } else {
                count++;
            }
        }
        final int residuesCount = CollectionUtils.size(dictionary);
        if ((moleculeType == MoleculeType.RNA) && (count == residuesCount)) {
            for (final Residue resEntry : dictionary) {
                if ((StringUtils.endsWithIgnoreCase(residueName, resEntry.getSingleLetterCode()))
                        || (StringUtils.startsWithIgnoreCase(residueName, resEntry.getSingleLetterCode()))) {
                    result = resEntry;
                    break;
                }
            }
        }
        return result;
    }

    private static final void updateAtoms(final Group residue, final String residueKey,
            final Residue resEntry, final StructureType structureType) {
        final Group residueCopy = (Group) residue.clone();
        final List<org.biojava.nbio.structure.Atom> atoms = residueCopy.getAtoms();
        final int atomsCount = CollectionUtils.size(atoms);
        boolean isChanged = false;
        for (int atomIndex = atomsCount - 1; atomIndex >= 0; atomIndex--) {
            final org.biojava.nbio.structure.Atom atom = atoms.get(atomIndex);
            final String atomName = atom.getName();
            if (resEntry.isAtomCanBeSkipped(atomName)) {
                atoms.remove(atomIndex);
                LOGGER.warn(String.format("Atom [%s] considered by residue [%s] is ignored", atomName,
                        residueKey));
                isChanged = true;
            } else {
                final String consistentAtomName = resEntry.getConsistentAtomName(atomName);
                if (!StringUtils.equals(atomName, consistentAtomName)) {
                    atoms.get(atomIndex).setName(consistentAtomName);
                    LOGGER.warn(String.format("Atom name [%s] is changed for [%s] in residue [%s]", atomName,
                            consistentAtomName, ResidueUtils.getResidueKey(residue)));
                    isChanged = true;
                }
                if (structureType == StructureType.MOLECULE) {
                    final boolean isTemperatureFactorChanged = updateTemperatureFactor(
                            ResidueUtils.getResidueKey(residue), atom.getTempFactor(), atoms.get(atomIndex),
                            consistentAtomName);
                    isChanged = isChanged || isTemperatureFactorChanged;
                }
            }
        }
        if (isChanged) {
            residue.clearAtoms();
            for (org.biojava.nbio.structure.Atom atom : residueCopy.getAtoms()) {
                residue.addAtom(atom);
            }
        }
    }

    private static final boolean updateTemperatureFactor(final String residueKey,
            final double temperatureFactor, final org.biojava.nbio.structure.Atom atom,
            final String consistentAtomName) {
        if ((Double.compare(temperatureFactor, DESCRIPTOR_CENTER_FLAG) == 0)
                || (Double.compare(temperatureFactor, ELEMENT_CENTER_FLAG) == 0)) {
            final double refinedTemperatureFactor = temperatureFactor + 0.01;
            atom.setTempFactor(refinedTemperatureFactor);
            LOGGER.warn(String
                    .format("Temperature factor [%s] of atom [%s] is changed for [%s] in residue [%s] to solve a conflict with a general descriptor representation",
                            String.format("%.2f", temperatureFactor), consistentAtomName,
                            String.format("%.2f", refinedTemperatureFactor), residueKey));
            return true;
        }
        return false;
    }
}

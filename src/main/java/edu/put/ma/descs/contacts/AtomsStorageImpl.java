package edu.put.ma.descs.contacts;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import com.google.common.collect.Maps;

import edu.put.ma.descs.contacts.atoms.VirtualAtoms;
import edu.put.ma.model.Residue;

public class AtomsStorageImpl implements AtomsStorage {

    private Map<String, Map<String, Atom>> virtualAtoms;

    AtomsStorageImpl() {
        prepareStorage();
    }

    @Override
    public void prepareStorage() {
        if (virtualAtoms == null) {
            virtualAtoms = Maps.newHashMap();
        } else {
            for (Map.Entry<String, Map<String, Atom>> entry : virtualAtoms.entrySet()) {
                entry.getValue().clear();
            }
            virtualAtoms.clear();
        }
    }

    @Override
    public Atom getAtom(final Group residue, final Residue residueEntry, final String atomName,
            final Pattern virtualAtomNamesPattern) {
        Atom result = null;
        final Matcher virtualAtomNameMatcher = virtualAtomNamesPattern.matcher(atomName);
        if (virtualAtomNameMatcher.matches()) {
            result = getVirtualAtom(residue, residueEntry, atomName);
        } else {
            result = residue.getAtom(atomName);
        }
        return result;
    }

    private boolean isVirtualAtomComputed(final String residueKey, final String atomName) {
        return virtualAtoms.containsKey(residueKey) && virtualAtoms.get(residueKey).containsKey(atomName);
    }

    private Atom getVirtualAtom(final String residueKey, final String atomName) {
        return virtualAtoms.get(residueKey).get(atomName);
    }

    private synchronized void addVirtualAtom(final String residueKey, final String atomName,
            final Atom virtualAtom) {
        if (!virtualAtoms.containsKey(residueKey)) {
            virtualAtoms.put(residueKey, new HashMap<String, Atom>());
        }
        final Map<String, Atom> virtualAtomsOfResidue = virtualAtoms.get(residueKey);
        if (!virtualAtomsOfResidue.containsKey(atomName)) {
            virtualAtomsOfResidue.put(atomName, virtualAtom);
        }
    }

    private Atom getVirtualAtom(final Group residue, final Residue residueEntry, final String atomName) {
        final String residueKey = new StringBuilder(residue.getResidueNumber().printFull()).append("_")
                .append(residue.getPDBName()).toString();
        Atom result = null;
        if (isVirtualAtomComputed(residueKey, atomName)) {
            result = getVirtualAtom(residueKey, atomName);
        } else {
            final VirtualAtoms localVirtualAtoms = VirtualAtoms.valueOf(atomName);
            result = localVirtualAtoms.getAtom(residue, residueEntry);
            addVirtualAtom(residueKey, atomName, result);
        }
        return result;
    }
}

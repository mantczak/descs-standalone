package edu.put.ma.model;

import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Group;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.utils.ResidueUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BackboneImpl implements Backbone {
    private final ImmutableSet<Atom> obligatoryAtoms;

    private final ImmutableSet<Atom> optionalAtoms;

    @Override
    public boolean isComplete(final Group residue) {
        return ResidueUtils.isAllAtomsIncluded(obligatoryAtoms, residue);
    }

    @Override
    public boolean isAtomConsidered(final String atomName) {
        return ResidueUtils.isAtomConsidered(obligatoryAtoms, atomName)
                || ResidueUtils.isAtomConsidered(optionalAtoms, atomName);
    }

    @Override
    public String getConsistentAtomName(final String atomName) {
        String consistentAtomName = ResidueUtils.getConsistentAtomName(obligatoryAtoms, atomName);
        if (StringUtils.isBlank(consistentAtomName)) {
            consistentAtomName = ResidueUtils.getConsistentAtomName(optionalAtoms, atomName);
        }
        return consistentAtomName;
    }

    @Override
    public final org.biojava.nbio.structure.Atom getBackboneCentroid(final Group residue) {
        return ResidueUtils.getCentroid(
                new ImmutableSet.Builder<Atom>().addAll(obligatoryAtoms).addAll(optionalAtoms).build(),
                residue, "BBGC");
    }
}

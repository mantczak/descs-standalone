package edu.put.ma.model;

import java.util.List;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import edu.put.ma.model.AlignmentImpl.Segment;

public interface Alignment {

    Alignment copy();

    int getAlignedResiduesCount();

    List<Atom> extendFirstDescriptorAtomsAndReturnAsNewObject(List<Atom> newAtoms);

    List<Atom> extendSecondDescriptorAtomsAndReturnAsNewObject(List<Atom> newAtoms);

    void removeFromFirstDescriptor(List<Group> firstDescriptorResidues, List<Atom> firstDescriptorAtoms);

    void removeFromSecondDescriptor(List<Group> secondDescriptorResidues, List<Atom> secondDescriptorAtoms);

    List<Group> identifyNewResiduesForFirstDescriptor(List<Group> newResidues);

    List<Group> identifyNewResiduesForSecondDescriptor(List<Group> newResidues);

    List<Atom> identifyNewAtomsForFirstDescriptor(List<Atom> newAtoms);

    List<Atom> identifyNewAtomsForSecondDescriptor(List<Atom> newAtoms);

    List<Segment> extend(Alignment extension, int alignmentAtomsCount, MoleculeType moleculeType);

    void setExtension(Alignment extension);

    void remove(Alignment extension);

    RmsdModel computeAlignmentRmsd();

    List<Group> getFirstDescriptorResidues();

    List<Group> getSecondDescriptorResidues();

    List<Atom> getFirstDescriptorAtoms();

    List<Atom> getSecondDescriptorAtoms();

    void revertSegmentStates(List<Segment> currentSegmentStates);

    boolean isResidueCoveredByFirstDescriptor(Group residue);

    boolean isResidueCoveredBySecondDescriptor(Group residue);
}

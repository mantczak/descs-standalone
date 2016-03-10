package edu.put.ma.model;

import java.util.List;

import lombok.Getter;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import com.google.common.collect.Lists;

import edu.put.ma.descs.DescriptorsComparatorImpl;
import edu.put.ma.utils.CollectionUtils;

public class AlignmentImpl implements Alignment {

    @Getter
    private final List<Group> firstDescriptorResidues;

    private final List<Atom> firstDescriptorAtoms;

    @Getter
    private final List<Group> secondDescriptorResidues;

    private final List<Atom> secondDescriptorAtoms;

    AlignmentImpl(final List<Group> firstDescriptorResidues, final List<Atom> firstDescriptorAtoms,
            final List<Group> secondDescriptorResidues, final List<Atom> secondDescriptorAtoms) {
        this();
        init(firstDescriptorResidues, firstDescriptorAtoms, secondDescriptorResidues, secondDescriptorAtoms);
    }

    private AlignmentImpl() {
        this.firstDescriptorResidues = Lists.newArrayList();
        this.firstDescriptorAtoms = Lists.newArrayList();
        this.secondDescriptorResidues = Lists.newArrayList();
        this.secondDescriptorAtoms = Lists.newArrayList();
    }

    private AlignmentImpl(final AlignmentImpl alignment) {
        this.firstDescriptorResidues = Lists.newArrayList(alignment.firstDescriptorResidues);
        this.firstDescriptorAtoms = Lists.newArrayList(alignment.firstDescriptorAtoms);
        this.secondDescriptorResidues = Lists.newArrayList(alignment.secondDescriptorResidues);
        this.secondDescriptorAtoms = Lists.newArrayList(alignment.secondDescriptorAtoms);
    }

    @Override
    public Alignment copy() {
        return new AlignmentImpl(this);
    }

    @Override
    public int getAlignedResiduesCount() {
        return org.apache.commons.collections4.CollectionUtils.size(firstDescriptorResidues);
    }

    @Override
    public List<Atom> extendFirstDescriptorAtomsAndReturnAsNewObject(final List<Atom> newAtoms) {
        return CollectionUtils.extendAndReturnAsNewObject(firstDescriptorAtoms, newAtoms);
    }

    @Override
    public List<Atom> extendSecondDescriptorAtomsAndReturnAsNewObject(final List<Atom> newAtoms) {
        return CollectionUtils.extendAndReturnAsNewObject(secondDescriptorAtoms, newAtoms);
    }

    @Override
    public void removeFromFirstDescriptor(final List<Group> firstDescriptorResidues,
            final List<Atom> firstDescriptorAtoms) {
        CollectionUtils.remove(this.firstDescriptorResidues, firstDescriptorResidues);
        CollectionUtils.remove(this.firstDescriptorAtoms, firstDescriptorAtoms);
    }

    @Override
    public void removeFromSecondDescriptor(final List<Group> secondDescriptorResidues,
            final List<Atom> secondDescriptorAtoms) {
        CollectionUtils.remove(this.secondDescriptorResidues, secondDescriptorResidues);
        CollectionUtils.remove(this.secondDescriptorAtoms, secondDescriptorAtoms);
    }

    @Override
    public List<Group> identifyNewResiduesForFirstDescriptor(final List<Group> newResidues) {
        return CollectionUtils.identifyNewElements(firstDescriptorResidues, newResidues);
    }

    @Override
    public List<Group> identifyNewResiduesForSecondDescriptor(final List<Group> newResidues) {
        return CollectionUtils.identifyNewElements(secondDescriptorResidues, newResidues);
    }

    @Override
    public List<Atom> identifyNewAtomsForFirstDescriptor(final List<Atom> newAtoms) {
        return CollectionUtils.identifyNewElements(firstDescriptorAtoms, newAtoms);
    }

    @Override
    public List<Atom> identifyNewAtomsForSecondDescriptor(final List<Atom> newAtoms) {
        return CollectionUtils.identifyNewElements(secondDescriptorAtoms, newAtoms);
    }

    @Override
    public void extend(final Alignment extension, final int alignmentAtomsCount) {
        final AlignmentImpl extensionImpl = (AlignmentImpl) extension;
        extendFirstDescriptor(extensionImpl.firstDescriptorResidues, extensionImpl.firstDescriptorAtoms,
                alignmentAtomsCount);
        extendSecondDescriptor(extensionImpl.secondDescriptorResidues, extensionImpl.secondDescriptorAtoms,
                alignmentAtomsCount);
    }

    @Override
    public void setExtension(final Alignment extension) {
        final AlignmentImpl extensionImpl = (AlignmentImpl) extension;
        setFirstDescriptor(extensionImpl.firstDescriptorResidues, extensionImpl.firstDescriptorAtoms);
        setSecondDescriptor(extensionImpl.secondDescriptorResidues, extensionImpl.secondDescriptorAtoms);
    }

    @Override
    public void remove(final Alignment extension) {
        final AlignmentImpl extensionImpl = (AlignmentImpl) extension;
        removeFromFirstDescriptor(extensionImpl.firstDescriptorResidues, extensionImpl.firstDescriptorAtoms);
        removeFromSecondDescriptor(extensionImpl.secondDescriptorResidues,
                extensionImpl.secondDescriptorAtoms);
    }

    @Override
    public RmsdModel computeAlignmentRmsd() {
        return DescriptorsComparatorImpl.computeAlignmentRmsd(firstDescriptorAtoms, secondDescriptorAtoms);
    }

    private void init(final List<Group> firstDescriptorResidues, final List<Atom> firstDescriptorAtoms,
            final List<Group> secondDescriptorResidues, final List<Atom> secondDescriptorAtoms) {
        CollectionUtils.init(this.firstDescriptorResidues, firstDescriptorResidues);
        CollectionUtils.init(this.firstDescriptorAtoms, firstDescriptorAtoms);
        CollectionUtils.init(this.secondDescriptorResidues, secondDescriptorResidues);
        CollectionUtils.init(this.secondDescriptorAtoms, secondDescriptorAtoms);
    }

    private void extendFirstDescriptor(final List<Group> firstDescriptorResidues,
            final List<Atom> firstDescriptorAtoms, final int alignmentAtomsCount) {
        extendPreservingOrder(this.firstDescriptorResidues, this.firstDescriptorAtoms,
                firstDescriptorResidues, firstDescriptorAtoms, alignmentAtomsCount);
    }

    private void extendSecondDescriptor(final List<Group> secondDescriptorResidues,
            final List<Atom> secondDescriptorAtoms, final int alignmentAtomsCount) {
        extendPreservingOrder(this.secondDescriptorResidues, this.secondDescriptorAtoms,
                secondDescriptorResidues, secondDescriptorAtoms, alignmentAtomsCount);
    }

    private void setFirstDescriptor(final List<Group> firstDescriptorResidues,
            final List<Atom> firstDescriptorAtoms) {
        update(this.firstDescriptorResidues, this.firstDescriptorAtoms, firstDescriptorResidues,
                firstDescriptorAtoms);
    }

    private void setSecondDescriptor(final List<Group> secondDescriptorResidues,
            final List<Atom> secondDescriptorAtoms) {
        update(this.secondDescriptorResidues, this.secondDescriptorAtoms, secondDescriptorResidues,
                secondDescriptorAtoms);
    }

    private static final void update(final List<Group> currentResidues, final List<Atom> currentAtoms,
            final List<Group> newResidues, final List<Atom> newAtoms) {
        currentResidues.clear();
        org.apache.commons.collections4.CollectionUtils.addAll(currentResidues, newResidues);
        currentAtoms.clear();
        org.apache.commons.collections4.CollectionUtils.addAll(currentAtoms, newAtoms);
    }

    private static final void extendPreservingOrder(final List<Group> currentResidues,
            final List<Atom> currentAtoms, final List<Group> newResidues, final List<Atom> newAtoms,
            final int alignmentAtomsCount) {
        final int currentResiduesCount = org.apache.commons.collections4.CollectionUtils
                .size(currentResidues);
        final Group firstNewResidue = newResidues.get(0);
        int residueIndex = 0;
        for (residueIndex = 0; residueIndex < currentResiduesCount; residueIndex++) {
            final Group residueWithHigherResidueNumber = currentResidues.get(residueIndex);
            if (residueWithHigherResidueNumber.getResidueNumber().compareTo(
                    firstNewResidue.getResidueNumber()) > 0) {
                CollectionUtils.extendFromSpecificPosition(currentResidues, newResidues, residueIndex);
                CollectionUtils.extendFromSpecificPosition(currentAtoms, newAtoms, residueIndex
                        * alignmentAtomsCount);
                break;
            }
        }
        if (residueIndex == currentResiduesCount) {
            CollectionUtils.extend(currentResidues, newResidues);
            CollectionUtils.extend(currentAtoms, newAtoms);
        }
    }
}

package edu.put.ma.model;

import java.io.File;
import java.util.List;

import lombok.Getter;

import org.apache.commons.io.FileUtils;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.SVDSuperimposer;

import com.google.common.base.Preconditions;

import edu.put.ma.access.ResiduesAccess;
import edu.put.ma.access.ResiduesAccessFactory;
import edu.put.ma.descs.AlignmentMode;
import edu.put.ma.descs.UnappropriateDescriptorException;
import edu.put.ma.io.writer.Writer;
import edu.put.ma.structure.StructureExtension;

@Getter
public class DescriptorsPairImpl implements DescriptorsPair {

    private static final boolean NOT_COPY_STRUCTURE = false;

    private final ResiduesAccess firstDescriptorResiduesAccess;

    private final Descriptor firstDescriptor;

    private final ResiduesAccess secondDescriptorResiduesAccess;

    private final Descriptor secondDescriptor;

    private final MoleculeType moleculeType;

    public DescriptorsPairImpl(final StructureExtension firstDescriptorExtendedStructure,
            final StructureExtension secondDescriptorExtendedStructure, final AlignmentMode alignmentMode)
            throws UnappropriateDescriptorException {
        final int modelIndex = 0;
        this.moleculeType = firstDescriptorExtendedStructure.getMoleculeType();
        this.firstDescriptorResiduesAccess = ResiduesAccessFactory.construct(firstDescriptorExtendedStructure
                .getModelByIndex(modelIndex));
        this.firstDescriptor = DescriptorImpl.constructDescriptor(firstDescriptorExtendedStructure,
                firstDescriptorResiduesAccess, NOT_COPY_STRUCTURE);
        this.secondDescriptorResiduesAccess = ResiduesAccessFactory
                .construct(secondDescriptorExtendedStructure.getModelByIndex(modelIndex));
        this.secondDescriptor = DescriptorImpl.constructDescriptor(secondDescriptorExtendedStructure,
                secondDescriptorResiduesAccess, alignmentMode == AlignmentMode.CONSIDER);
    }

    @Override
    public String getDescriptorsPairId() {
        return new StringBuilder(firstDescriptor.getId()).append(", ").append(secondDescriptor.getId())
                .toString();
    }

    @Override
    public boolean areSingleElementDescriptors() {
        return (firstDescriptor.getElementsCount() == 1) && (secondDescriptor.getElementsCount() == 1);
    }

    @Override
    public List<Group> getFirstDescriptorOriginElementResidues() {
        return firstDescriptor.getOriginElementResidues(firstDescriptorResiduesAccess);
    }

    @Override
    public List<Group> getSecondDescriptorOriginElementResidues() {
        return secondDescriptor.getOriginElementResidues(secondDescriptorResiduesAccess);
    }

    @Override
    public List<Group> getFirstDescriptorOtherElementResiduesByIndex(int otherElementIndex) {
        return firstDescriptor.getOtherElementResiduesByIndex(otherElementIndex,
                firstDescriptorResiduesAccess);
    }

    @Override
    public List<Group> getSecondDescriptorOtherElementResiduesByIndex(int otherElementIndex) {
        return secondDescriptor.getOtherElementResiduesByIndex(otherElementIndex,
                secondDescriptorResiduesAccess);
    }

    @Override
    public void saveFirstDescriptor(final File outputDir, final Writer writer) {
        saveDescriptor(firstDescriptor, outputDir, writer);
    }

    @Override
    public void rotateAndShiftSecondDescriptor(final SVDSuperimposer superimposer) {
        Preconditions.checkNotNull(superimposer, "It is impossible to rotate and shift second descriptor");
        secondDescriptor.rotateAndShift(superimposer);
    }

    @Override
    public void saveSecondDescriptor(final File outputDir, final Writer writer) {
        saveDescriptor(secondDescriptor, outputDir, writer);
    }

    @Override
    public int getFirstDescriptorElementsCount() {
        return firstDescriptor.getElementsCount();
    }

    @Override
    public int getSecondDescriptorElementsCount() {
        return secondDescriptor.getElementsCount();
    }

    @Override
    public int getFirstDescriptorResiduesCount() {
        return firstDescriptor.getResiduesCount();
    }

    @Override
    public int getSecondDescriptorResiduesCount() {
        return secondDescriptor.getResiduesCount();
    }

    @Override
    public boolean areDescriptorsUncomparable() {
        return firstDescriptor.getNeighbourhoodSize() != secondDescriptor.getNeighbourhoodSize();
    }

    @Override
    public int getFirstDescriptorElementSize() {
        return getDescriptorElementSize(firstDescriptor.getNeighbourhoodSize());
    }

    @Override
    public int getSecondDescriptorElementSize() {
        return getDescriptorElementSize(secondDescriptor.getNeighbourhoodSize());
    }

    @Override
    public String getFirstDescriptorString() {
        return firstDescriptor.toStringWithoutOriginResidueIndex();
    }

    @Override
    public String getSecondDescriptorString() {
        return secondDescriptor.toStringWithoutOriginResidueIndex();
    }

    @Override
    public List<Group> getFirstDescriptorResidues() {
        return firstDescriptor.getResidues();
    }

    @Override
    public List<Group> getSecondDescriptorResidues() {
        return secondDescriptor.getResidues();
    }

    private static final int getDescriptorElementSize(final int neighbourhoodSize) {
        return (neighbourhoodSize << 1) + 1;
    }

    private static final void saveDescriptor(final Descriptor descriptor, final File outputDir,
            final Writer writer) {
        final File descriptorFile = FileUtils.getFile(outputDir, descriptor.getId());
        synchronized (writer) {
            writer.write(descriptor.getStructure(), descriptorFile);
        }
    }

}

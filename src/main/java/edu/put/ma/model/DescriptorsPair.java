package edu.put.ma.model;

import java.io.File;
import java.util.List;

import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.SVDSuperimposer;

import edu.put.ma.io.writer.Writer;

public interface DescriptorsPair {

    MoleculeType getMoleculeType();

    String getDescriptorsPairId();

    boolean areSingleElementDescriptors();

    List<Group> getFirstDescriptorOriginElementResidues();

    List<Group> getSecondDescriptorOriginElementResidues();

    int getFirstDescriptorElementsCount();

    int getSecondDescriptorElementsCount();

    int getFirstDescriptorResiduesCount();

    int getSecondDescriptorResiduesCount();

    void saveFirstDescriptor(File outputDir, Writer writer);

    void rotateAndShiftSecondDescriptor(SVDSuperimposer superimposer);

    void saveSecondDescriptor(File outputDir, Writer writer);

    List<Group> getFirstDescriptorOtherElementResiduesByIndex(int firstDescriptorElementIndex);

    List<Group> getSecondDescriptorOtherElementResiduesByIndex(int secondDescriptorElementIndex);
    
    boolean areDescriptorsUncomparable();
    
    int getFirstDescriptorElementSize();
    
    int getSecondDescriptorElementSize();

}

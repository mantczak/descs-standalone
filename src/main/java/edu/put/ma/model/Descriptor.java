package edu.put.ma.model;

import java.util.List;

import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.SVDSuperimposer;
import org.biojava.nbio.structure.Structure;

import edu.put.ma.access.ResiduesAccess;

public interface Descriptor {

    int getSegmentsCount();

    int getElementsCount();

    int getResiduesCount();

    String getId();

    int getNeighbourhoodSize();

    boolean isValid();

    Structure getStructure();

    String toString();

    String toStringWithoutOriginResidueIndex();

    List<Group> getOriginElementResidues(ResiduesAccess residuesAccess);

    void rotateAndShift(SVDSuperimposer superimposer);

    List<Group> getOtherElementResiduesByIndex(int otherElementIndex, ResiduesAccess residuesAccess);
    
    List<Group> getResidues();
}

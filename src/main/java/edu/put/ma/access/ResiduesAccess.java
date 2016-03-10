package edu.put.ma.access;

import java.util.List;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;

public interface ResiduesAccess {

    int getChainIndex(int index);

    Group getResidueByIndex(int index, List<Chain> model);

    Group cloneResidueByIndex(int index, List<Chain> model);

    int getResiduesAccessIndexesSize();

    List<Integer> getResiduesAccessIndexes();

    List<Group> getElementResiduesByCenterIndex(int centerIndex, List<Chain> model, int neighbourhoodSize);
}

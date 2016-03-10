package edu.put.ma.descs.contacts;

import java.util.List;

import org.biojava.nbio.structure.Chain;

import com.google.common.collect.ImmutableList;
import edu.put.ma.model.ModelProperties;
import edu.put.ma.model.MoleculeType;

public interface ContactsInspector {

    boolean isValid();

    void constructInContactResiduesMatrix(List<Chain> model, ModelProperties modelProperties);

    String getInContactResiduesMatrixString();

    ImmutableList<Boolean> getContactsOfResidueByIndex(int residueIndex);

    String getAtomNamePairsString();

    int getThreadsCount();

    void setThreadsCount(int threadsCount);

    void setExpression(String expressionString, MoleculeType moleculeType);

    void setExpression(String expressionString);

    void close();
}

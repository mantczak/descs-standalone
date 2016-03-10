package edu.put.ma.descs.contacts;

import java.util.Set;

import edu.put.ma.model.AtomNamesPair;
import edu.put.ma.model.MoleculeType;

public interface ExpressionValidator {

    void setInContactResiduesExpressionString(String inContactResiduesExpressionString,
            MoleculeType moleculeType);

    boolean isValid();

    String getAtomNamePairsString();

    String getInContactResiduesExpressionString();

    Set<String> getVariableNames();

    AtomNamesPair getDistance(String variableName);
}

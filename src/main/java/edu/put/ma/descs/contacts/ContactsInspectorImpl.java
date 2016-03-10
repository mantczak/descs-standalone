package edu.put.ma.descs.contacts;

import static edu.put.ma.gaps.GapsDistributionImpl.RESIDUE_OUTSIDE_GAP;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.primitives.Booleans;

import edu.put.ma.access.ResiduesAccess;
import edu.put.ma.descs.DescriptorResidueType;
import edu.put.ma.descs.DescriptorsBuilderImpl;
import edu.put.ma.gaps.GapsDistribution;
import edu.put.ma.model.AtomNamesPair;
import edu.put.ma.model.ModelProperties;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.model.Residue;
import edu.put.ma.utils.ExecutorUtils;
import edu.put.ma.utils.PreconditionUtils;
import edu.put.ma.model.ResiduesDictionary;

public class ContactsInspectorImpl implements ContactsInspector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactsInspectorImpl.class);

    private final ExpressionValidator expressionValidator;

    private final ExpressionEvaluator expressionEvaluator;

    private final AtomsStorage atomsStorage;

    private MoleculeType moleculeType;

    private boolean[][] inContactResiduesMatrix;

    private ExecutorService executor;

    public ContactsInspectorImpl(final String inContactResiduesExpressionString,
            final MoleculeType moleculeType, final int threadsCount) {
        this.moleculeType = moleculeType;
        this.atomsStorage = AtomsStorageFactory.construct();
        this.expressionValidator = new ExpressionValidatorImpl(inContactResiduesExpressionString,
                moleculeType);
        this.expressionEvaluator = new ExpressionEvaluatorImpl(
                expressionValidator.getInContactResiduesExpressionString(),
                expressionValidator.getVariableNames(), threadsCount);
        prepareExecutorService(threadsCount);
    }

    @Override
    public boolean isValid() {
        return expressionValidator.isValid();
    }

    @Override
    public void constructInContactResiduesMatrix(final List<Chain> model,
            final ModelProperties modelProperties) {
        final ResiduesAccess residuesAccess = modelProperties.getResiduesAccess();
        final GapsDistribution gapsDistribution = modelProperties.getGapsDistribution();
        final int residuesCount = residuesAccess.getResiduesAccessIndexesSize();
        prepare(residuesCount);
        for (int residueIndex = 0; residueIndex < residuesCount; residueIndex++) {
            final int residueGapFlag = gapsDistribution.getResidueGapFlag(residueIndex);
            final Group residue = residuesAccess.getResidueByIndex(residueIndex, model);
            if ((residueGapFlag == RESIDUE_OUTSIDE_GAP)
                    && (DescriptorsBuilderImpl.isElementCanBeCreated(residuesCount, residueIndex,
                            residueGapFlag, DescriptorResidueType.ORIGIN_CENTER, gapsDistribution))) {
                analysePotentialInContactResidues(model, residuesAccess, gapsDistribution, residuesCount,
                        residueIndex, residue);
            }
        }
    }

    @Override
    public String getInContactResiduesMatrixString() {
        final StringBuilder sb = new StringBuilder("In-contact residues matrix:\n");
        final int rowsCount = ArrayUtils.getLength(inContactResiduesMatrix);
        int rowIndex = 0;
        for (boolean[] row : inContactResiduesMatrix) {
            for (boolean val : row) {
                sb.append(String.valueOf((val) ? 1 : 0)).append("\t");
            }
            if (rowIndex < rowsCount - 1) {
                sb.append("\n");
            }
            rowIndex++;
        }
        return sb.toString();
    }

    @Override
    public ImmutableList<Boolean> getContactsOfResidueByIndex(final int residueIndex) {
        final int residuesCount = ArrayUtils.getLength(inContactResiduesMatrix);
        PreconditionUtils.checkIfIndexInRange(residueIndex, 0, residuesCount, "Residue");
        final boolean[] residueContacts = inContactResiduesMatrix[residueIndex];
        return ImmutableList.copyOf(Booleans.asList(residueContacts));
    }

    @Override
    public String getAtomNamePairsString() {
        return expressionValidator.getAtomNamePairsString();
    }

    @Override
    public int getThreadsCount() {
        return expressionEvaluator.getThreadsCount();
    }

    @Override
    public void setThreadsCount(final int threadsCount) {
        expressionEvaluator.setThreadsCount(threadsCount);
        prepareExecutorService(threadsCount);
    }

    @Override
    public void setExpression(final String expressionString, final MoleculeType moleculeType) {
        this.moleculeType = moleculeType;
        updateExpression(expressionString, moleculeType);
    }

    @Override
    public void setExpression(final String expressionString) {
        updateExpression(expressionString, moleculeType);
    }

    @Override
    public void close() {
        expressionEvaluator.close();
        ExecutorUtils.closeExecutor(executor);
    }

    private void analysePotentialInContactResidues(final List<Chain> model,
            final ResiduesAccess residuesAccess, final GapsDistribution gapsDistribution,
            final int residuesCount, final int residueIndex, final Group residue) {
        for (int indexOfPotentiallyInContactResidue = residueIndex + 1; indexOfPotentiallyInContactResidue < residuesCount; indexOfPotentiallyInContactResidue++) {
            final int potentiallyInContactResidueGapFlag = gapsDistribution
                    .getResidueGapFlag(indexOfPotentiallyInContactResidue);
            if (potentiallyInContactResidueGapFlag == RESIDUE_OUTSIDE_GAP) {
                final Group potentiallyInContactResidue = residuesAccess.getResidueByIndex(
                        indexOfPotentiallyInContactResidue, model);
                boolean areResiduesInContact = false;
                if (getThreadsCount() == 1) {
                    areResiduesInContact = verify(residue, potentiallyInContactResidue);
                } else {
                    areResiduesInContact = verifyConcurrently(residue, potentiallyInContactResidue);
                }
                if (areResiduesInContact) {
                    inContactResiduesMatrix[residueIndex][indexOfPotentiallyInContactResidue] = inContactResiduesMatrix[indexOfPotentiallyInContactResidue][residueIndex] = areResiduesInContact;
                }
            }
        }
    }

    private boolean verifyConcurrently(final Group residue, final Group potentiallyInContactResidue) {
        final Callable<Boolean> verificator = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return verify(residue, potentiallyInContactResidue);
            }
        };
        final Future<Boolean> result = executor.submit(verificator);
        try {
            return result.get().booleanValue();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    private void updateExpression(final String expressionString, final MoleculeType moleculeType) {
        expressionValidator.setInContactResiduesExpressionString(expressionString, moleculeType);
        expressionEvaluator.setExpression(expressionValidator.getInContactResiduesExpressionString(),
                expressionValidator.getVariableNames());
    }

    private void prepareExecutorService(final int threadsCount) {
        executor = ExecutorUtils.prepareExecutorService(executor, threadsCount);
    }

    private void prepare(final int residuesCount) {
        initInContactResiduesMatrix(residuesCount);
        atomsStorage.prepareStorage();
    }

    private void initInContactResiduesMatrix(final int residuesCount) {
        inContactResiduesMatrix = new boolean[residuesCount][residuesCount];
    }

    private boolean verify(final Group residue, final Group potentiallyInContactResidue) {
        final Pattern virtualAtomNamesPattern = ExpressionValidatorImpl
                .getVirtualAtomNamesPattern(moleculeType);
        final Residue residueEntry = ResiduesDictionary.getResidueEntry(residue.getPDBName(), moleculeType);
        final Residue potentiallyInContactResidueEntry = ResiduesDictionary.getResidueEntry(
                potentiallyInContactResidue.getPDBName(), moleculeType);
        final Map<String, Double> variableValues = Maps.newHashMap();
        final Set<String> variableNames = expressionValidator.getVariableNames();
        for (String variableName : variableNames) {
            final AtomNamesPair currentDistance = expressionValidator.getDistance(variableName);
            Atom residueAtom = null, potentiallyInContactResidueAtom = null;
            if (currentDistance.isSingleComponent()) {
                final String commonAtomName = currentDistance.getFirstAtomName();
                residueAtom = atomsStorage.getAtom(residue, residueEntry, commonAtomName,
                        virtualAtomNamesPattern);
                potentiallyInContactResidueAtom = atomsStorage.getAtom(potentiallyInContactResidue,
                        potentiallyInContactResidueEntry, commonAtomName, virtualAtomNamesPattern);
            } else {
                residueAtom = atomsStorage.getAtom(residue, residueEntry, currentDistance.getFirstAtomName(),
                        virtualAtomNamesPattern);
                potentiallyInContactResidueAtom = atomsStorage.getAtom(potentiallyInContactResidue,
                        potentiallyInContactResidueEntry, currentDistance.getSecondAtomName(),
                        virtualAtomNamesPattern);
            }
            if ((residueAtom != null) && (potentiallyInContactResidueAtom != null)) {
                double distance = Calc.getDistance(residueAtom, potentiallyInContactResidueAtom);
                variableValues.put(variableName, distance);
            }
        }
        if (variableNames.size() == variableValues.size()) {
            return this.expressionEvaluator.evaluateAndTransform(variableValues);
        }
        return false;
    }
}

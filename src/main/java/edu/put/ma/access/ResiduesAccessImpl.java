package edu.put.ma.access;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;

import com.google.common.collect.Lists;

import edu.put.ma.utils.PreconditionUtils;

public class ResiduesAccessImpl implements ResiduesAccess {

    private static final int COMMON_ORDER = 10;

    private final int order;

    private final List<Integer> indexes;

    ResiduesAccessImpl(final List<Chain> model) {
        final int maxResiduesCount = getMaxResiduesCount(model);
        this.order = getOrder(maxResiduesCount);
        this.indexes = Lists.newArrayList();
        initIndexes(model);
    }

    @Override
    public int getChainIndex(int index) {
        final int residueAccessIndex = getResidueAccessIndex(index);
        return getChainIndex(residueAccessIndex, order);
    }

    @Override
    public Group getResidueByIndex(final int index, final List<Chain> model) {
        final int residueAccessIndex = getResidueAccessIndex(index);
        final int chainIndex = getChainIndex(residueAccessIndex, order);
        final int residueIndex = getResidueIndex(residueAccessIndex, order);
        PreconditionUtils.checkIfIndexInRange(chainIndex, 0, CollectionUtils.size(model), "Chain");
        final List<Group> residues = model.get(chainIndex).getAtomGroups();
        PreconditionUtils.checkIfIndexInRange(residueIndex, 0, CollectionUtils.size(residues), "Residue");
        return residues.get(residueIndex);
    }

    @Override
    public List<Group> getElementResiduesByCenterIndex(final int centerIndex, final List<Chain> model,
            final int neighbourhoodSize) {
        final int leftBoundaryIndex = centerIndex - neighbourhoodSize;
        final int elementSize = (neighbourhoodSize << 1) + 1;
        final int leftBoundaryResidueAccessIndex = getResidueAccessIndex(leftBoundaryIndex);
        final int leftBoundaryChainIndex = getChainIndex(leftBoundaryResidueAccessIndex, order);
        final int leftBoundaryResidueIndex = getResidueIndex(leftBoundaryResidueAccessIndex, order);
        PreconditionUtils
                .checkIfIndexInRange(leftBoundaryChainIndex, 0, CollectionUtils.size(model), "Chain");
        final List<Group> residues = model.get(leftBoundaryChainIndex).getAtomGroups();
        PreconditionUtils.checkIfIndexInRange(leftBoundaryResidueIndex, 0, CollectionUtils.size(residues),
                "Residue");
        return residues.subList(leftBoundaryResidueIndex, leftBoundaryResidueIndex + elementSize);
    }

    @Override
    public Group cloneResidueByIndex(final int index, final List<Chain> model) {
        final Group residue = getResidueByIndex(index, model);
        if (residue != null) {
            return (Group) residue.clone();
        }
        return null;
    }

    @Override
    public int getResiduesAccessIndexesSize() {
        return CollectionUtils.size(indexes);
    }

    @Override
    public List<Integer> getResiduesAccessIndexes() {
        return Collections.unmodifiableList(indexes);
    }

    @Override
    public String toString() {
        return new StringBuilder(String.valueOf(getResiduesAccessIndexesSize())).append("\t")
                .append(indexes.toString()).toString();
    }

    private void initIndexes(final List<Chain> model) {
        final int chainsCount = CollectionUtils.size(model);
        for (int chainIndex = 0; chainIndex < chainsCount; chainIndex++) {
            final Chain chain = model.get(chainIndex);
            final int residuesCount = CollectionUtils.size(chain.getAtomGroups());
            for (int residueIndex = 0; residueIndex < residuesCount; residueIndex++) {
                indexes.add(computeResidueIndex(chainIndex, residueIndex, order));
            }
        }
    }

    private int getResidueAccessIndex(final int index) {
        final int residuesAccessIndexesSize = getResiduesAccessIndexesSize();
        PreconditionUtils.checkIfIndexInRange(index, 0, residuesAccessIndexesSize, "Residue access");
        return indexes.get(index);
    }

    private static final int getMaxResiduesCount(final List<Chain> model) {
        int maxResiduesCount = 0;
        for (Chain chain : model) {
            final int residuesCount = CollectionUtils.size(chain.getAtomGroups());
            maxResiduesCount = Math.max(maxResiduesCount, residuesCount);
        }
        return maxResiduesCount;
    }

    private static final int computeResidueIndex(final int chainIndex, final int residueIndex, final int order) {
        return chainIndex * order + residueIndex;
    }

    private static final int getChainIndex(final int index, final int order) {
        return index / order;
    }

    private static final int getResidueIndex(final int index, final int order) {
        return index % order;
    }

    private static final int getOrder(final int number) {
        int order = COMMON_ORDER;
        while (order < number) {
            order *= COMMON_ORDER;
        }
        return order;
    }
}

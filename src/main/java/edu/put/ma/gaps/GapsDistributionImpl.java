package edu.put.ma.gaps;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import org.apache.commons.collections4.CollectionUtils;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;

import com.google.common.collect.Lists;

import edu.put.ma.model.MoleculeType;
import edu.put.ma.utils.PreconditionUtils;
import edu.put.ma.utils.ResidueUtils;

public class GapsDistributionImpl implements GapsDistribution {

    public static final int RESIDUE_IN_GAP_PROXIMITY = Integer.MAX_VALUE;

    public static final int RESIDUE_OUTSIDE_GAP = 0;

    private final List<Integer> distribution;

    @Getter
    private final int gapsDistributionSize;

    @Getter
    private final int neibhourhoodSize;

    @Getter
    private final int elementSize;

    @Getter
    private boolean valid;

    public GapsDistributionImpl(final List<Chain> model, final MoleculeType moleculeType,
            final int elementSize) {
        this.distribution = Lists.newArrayList();
        this.elementSize = elementSize;
        this.neibhourhoodSize = elementSize >> 1;
        this.valid = initDistribution(model, moleculeType);
        this.gapsDistributionSize = CollectionUtils.size(distribution);
    }

    @Override
    public int getResidueGapFlag(final int index) {
        PreconditionUtils.checkIfIndexInRange(index, 0, getGapsDistributionSize(), "Residue");
        return distribution.get(index);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(elementSize).append("\t").append(neibhourhoodSize).append("\t").append(valid).append("\t")
                .append(getGapsDistributionSize()).append("\t").append(distribution.toString());
        return sb.toString();
    }

    private boolean initDistribution(final List<Chain> chains, final MoleculeType moleculeType) {
        int index = 0;
        int startChainIndex = 0;
        int residuesCount = 0;
        for (Chain chain : chains) {
            final List<Group> residues = chain.getAtomGroups();
            residuesCount = CollectionUtils.size(residues);
            for (int i = 0; i < neibhourhoodSize; i++) {
                if (residuesCount > ((i + 1) << 1) - 1) {
                    final int boundary = neibhourhoodSize - i;
                    distribution.addAll(index++, Arrays.asList(new Integer[] { boundary, (-1) * boundary }));
                }
            }
            if (residuesCount > (neibhourhoodSize << 1)) {
                index = computeDistribution(moleculeType, index, startChainIndex, residuesCount, residues);
            }
            index += neibhourhoodSize;
            startChainIndex += residuesCount;
        }
        if (startChainIndex == CollectionUtils.size(distribution)) {
            return true;
        }
        return false;
    }

    private int computeDistribution(final MoleculeType moleculeType, final int index, int startChainIndex,
            int residuesCount, final List<Group> residues) {
        int lastGapIndex = 0;
        int localIndex = index;
        for (int i = neibhourhoodSize; i < residuesCount - neibhourhoodSize; i++) {
            distribution.add(localIndex++, 0);
        }
        for (int residueIndex = 1; residueIndex < residuesCount; residueIndex++) {
            final Group previousResidue = residues.get(residueIndex - 1);
            final Group currentResidue = residues.get(residueIndex);
            if (!ResidueUtils.areResiduesConnected(moleculeType, previousResidue, currentResidue)) {
                if (residueIndex - lastGapIndex < elementSize) {
                    processElementResiduesInGapProximity(startChainIndex, lastGapIndex, residueIndex,
                            residuesCount);
                } else {
                    processOtherResiduesOutsideGap(startChainIndex, residueIndex, residuesCount);
                }
                if (residuesCount - residueIndex < elementSize) {
                    fillTailResidues(startChainIndex, residueIndex, residuesCount);
                    break;
                }
                lastGapIndex = residueIndex;
            }
        }
        return localIndex;
    }

    private void fillTailResidues(final int startChainIndex, final int residueIndex, final int residuesCount) {
        for (int neighbourResidueIndex = residueIndex; neighbourResidueIndex < residuesCount; neighbourResidueIndex++) {
            distribution.set(startChainIndex + neighbourResidueIndex, RESIDUE_IN_GAP_PROXIMITY);
        }
    }

    private void processOtherResiduesOutsideGap(final int startChainIndex, final int residueIndex,
            final int residuesCount) {
        for (int neighbourResidueIndex = (-1) * neibhourhoodSize; neighbourResidueIndex < neibhourhoodSize; neighbourResidueIndex++) {
            final int currentIndex = startChainIndex + residueIndex + neighbourResidueIndex;
            if (currentIndex >= residuesCount) {
                break;
            }
            if (neighbourResidueIndex < 0) {
                final int boundary = neibhourhoodSize + neighbourResidueIndex + 1;
                distribution.set(currentIndex, boundary);
            } else {
                final int boundary = (-1) * neibhourhoodSize + neighbourResidueIndex;
                distribution.set(currentIndex, boundary);
            }
        }
    }

    private void processElementResiduesInGapProximity(final int startChainIndex, final int lastGapIndex,
            final int residueIndex, final int residuesCount) {
        fillTailResidues(startChainIndex, lastGapIndex, residueIndex);
        for (int neighbourResidueIndex = 0; neighbourResidueIndex < neibhourhoodSize; neighbourResidueIndex++) {
            final int currentIndex = startChainIndex + residueIndex + neighbourResidueIndex;
            if (currentIndex >= residuesCount) {
                break;
            }
            final int boundary = (-1) * neibhourhoodSize + neighbourResidueIndex;
            distribution.set(currentIndex, boundary);
        }
    }
}

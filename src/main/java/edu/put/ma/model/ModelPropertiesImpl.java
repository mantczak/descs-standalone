package edu.put.ma.model;

import java.util.List;

import lombok.Getter;

import org.biojava.nbio.structure.Chain;

import edu.put.ma.access.ResiduesAccess;
import edu.put.ma.access.ResiduesAccessFactory;
import edu.put.ma.gaps.GapsDistribution;
import edu.put.ma.gaps.GapsDistributionImpl;

@Getter
public class ModelPropertiesImpl implements ModelProperties {

    private final ResiduesAccess residuesAccess;

    private final GapsDistribution gapsDistribution;

    public ModelPropertiesImpl(final List<Chain> model, final MoleculeType moleculeType, final int elementSize) {
        this.residuesAccess = ResiduesAccessFactory.construct(model);
        this.gapsDistribution = new GapsDistributionImpl(model, moleculeType, elementSize);
    }

    @Override
    public boolean isValid() {
        return (gapsDistribution != null)
                && (gapsDistribution.isValid())
                && (residuesAccess != null)
                && (gapsDistribution.getGapsDistributionSize() == residuesAccess
                        .getResiduesAccessIndexesSize());
    }
}

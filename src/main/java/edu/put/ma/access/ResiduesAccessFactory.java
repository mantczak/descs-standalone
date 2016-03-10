package edu.put.ma.access;

import java.util.List;

import org.biojava.nbio.structure.Chain;

public final class ResiduesAccessFactory {

    private ResiduesAccessFactory() {
        // hidden constructor
    }

    public static final ResiduesAccess construct(final List<Chain> model) {
        return new ResiduesAccessImpl(model);
    }
}

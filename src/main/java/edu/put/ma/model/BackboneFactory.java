package edu.put.ma.model;

import com.google.common.collect.ImmutableSet;

public final class BackboneFactory {

    private BackboneFactory() {
        // hidden constructor
    }

    public static final Backbone construct(final ImmutableSet<Atom> obligatoryAtoms,
            final ImmutableSet<Atom> optionalAtoms) {
        return new BackboneImpl(obligatoryAtoms, optionalAtoms);
    }
}

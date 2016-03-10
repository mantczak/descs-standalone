package edu.put.ma.model;

public final class ExtendedAlignmentFactory {

    private ExtendedAlignmentFactory() {
        // hidden constructor
    }

    public static final ExtendedAlignment construct(final Alignment alignment,
            final ComparisonResult comparisonResult) {
        return new ExtendedAlignmentImpl(alignment, comparisonResult);
    }
}

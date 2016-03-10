package edu.put.ma.io.reader;

import edu.put.ma.io.FormatType;

public final class ReaderFactory {

    private ReaderFactory() {
        // hidden constructor
    }

    public static final Reader construct(final FormatType type) {
        Reader reader = null;
        switch (type) {
            case PDB:
                reader = new PdbReader();
                break;
            case CIF:
                reader = new MmcifReader();
                break;
            default:
                throw new IllegalArgumentException("Unknown input format! PDB or CIF are only considered");
        }
        return reader;
    }
}

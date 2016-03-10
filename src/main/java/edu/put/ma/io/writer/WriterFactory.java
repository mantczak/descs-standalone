package edu.put.ma.io.writer;

import edu.put.ma.io.FormatType;

public class WriterFactory {

    private WriterFactory() {
        // hidden constructor
    }

    public static final Writer construct(final FormatType type) {
        Writer writer = null;
        switch (type) {
            case PDB:
                writer = new PdbWriter();
                break;
            case CIF:
                writer = new MmcifWriter();
                break;
            default:
                throw new IllegalArgumentException("Unknown output format! PDB or CIF are only considered");
        }
        return writer;
    }
}

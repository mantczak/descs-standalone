package edu.put.ma.descs.contacts;

public final class AtomsStorageFactory {

    private AtomsStorageFactory() {
        // hidden constructor
    }

    public static final AtomsStorage construct() {
        return new AtomsStorageImpl();
    }
}

package edu.put.ma.model.input;

import edu.put.ma.descs.AlignmentMode;
import edu.put.ma.descs.algorithms.ComparisonAlgorithm;
import edu.put.ma.descs.algorithms.ComparisonAlgorithms;
import edu.put.ma.descs.SimilarDescriptorsVerifier;
import edu.put.ma.model.MoleculeType;

public interface DescriptorsComparatorInputModel extends CommonInputModel {

    MoleculeType getMoleculeType();

    String getFirstDescriptorFilePath();

    String getSecondDescriptorFilePath();

    String getAlignmentAtomNamesFilePath();

    ComparisonAlgorithms getComparisonAlgorithmType();

    ComparisonAlgorithm getComparisonAlgorithm();

    SimilarDescriptorsVerifier getSimilarDescriptorsVerifier();

    AlignmentMode getAlignmentMode();

    String getOutputDirPath();

    double getMaximalRmsdThresholdPerDuplexPair();
    
}

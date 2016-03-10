package edu.put.ma.descs.algorithms;

import static edu.put.ma.descs.algorithms.CommonAlgorithm.COMPREHENSIVE_SEARCH;
import static edu.put.ma.model.input.DescriptorsComparatorInputModelImpl.DEFAULT_MAXIMAL_RMSD_BASED_COST_OF_PAIR_OF_ALIGNED_DUPLEXES;
import static edu.put.ma.descs.algorithms.CommonAlgorithm.FIRST_ALIGNMENT_ONLY;

import edu.put.ma.descs.ComparisonPrecision;

public enum ComparisonAlgorithms {

    BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT(new BacktrackingDrivenSearch(COMPREHENSIVE_SEARCH,
            ComparisonPrecision.ALL_RULES_CONSIDERED)), BACKTRACKING_DRIVEN_FIRST_ALIGNMENT_ONLY(
            new BacktrackingDrivenSearch(FIRST_ALIGNMENT_ONLY, ComparisonPrecision.ALL_RULES_CONSIDERED)), HUNGARIAN_METHOD_DRIVEN_FIRST_ALIGNMENT_ONLY_PARTIAL_SOLUTIONS_NOT_CONSIDERED(
            new HungarianMethodDrivenSearch(FIRST_ALIGNMENT_ONLY, ComparisonPrecision.ALL_RULES_CONSIDERED,
                    DEFAULT_MAXIMAL_RMSD_BASED_COST_OF_PAIR_OF_ALIGNED_DUPLEXES,
                    HungarianMethodDrivenSearch.AlgorithmType.FIRST)), HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_NOT_CONSIDERED(
            new HungarianMethodDrivenSearch(COMPREHENSIVE_SEARCH, ComparisonPrecision.ALL_RULES_CONSIDERED,
                    DEFAULT_MAXIMAL_RMSD_BASED_COST_OF_PAIR_OF_ALIGNED_DUPLEXES,
                    HungarianMethodDrivenSearch.AlgorithmType.SECOND)), HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_CONSIDERED(
            new HungarianMethodDrivenSearch(COMPREHENSIVE_SEARCH, ComparisonPrecision.ALL_RULES_CONSIDERED,
                    DEFAULT_MAXIMAL_RMSD_BASED_COST_OF_PAIR_OF_ALIGNED_DUPLEXES,
                    HungarianMethodDrivenSearch.AlgorithmType.THIRD));

    private final ComparisonAlgorithm algorithm;

    ComparisonAlgorithms(final ComparisonAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public ComparisonAlgorithm getComparisonAlgorithm() {
        return algorithm;
    }
}

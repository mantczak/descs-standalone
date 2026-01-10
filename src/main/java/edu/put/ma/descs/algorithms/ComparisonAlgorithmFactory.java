package edu.put.ma.descs.algorithms;

import static edu.put.ma.descs.algorithms.CommonAlgorithm.COMPREHENSIVE_SEARCH;
import static edu.put.ma.model.input.DescriptorsComparatorInputModelImpl.DEFAULT_MAXIMAL_RMSD_BASED_COST_OF_PAIR_OF_ALIGNED_DUPLEXES;
import static edu.put.ma.descs.algorithms.CommonAlgorithm.FIRST_ALIGNMENT_ONLY;
import edu.put.ma.descs.ComparisonPrecision;

public class ComparisonAlgorithmFactory {

        ComparisonAlgorithmFactory() {
        }

        public static CommonAlgorithm getBacktrackingDrivenLongestAlignmentAlgorithm() {
                return new BacktrackingDrivenSearch(COMPREHENSIVE_SEARCH,
                                ComparisonPrecision.ALL_RULES_CONSIDERED);
        }

        public static CommonAlgorithm getBacktrackingDrivenFirstAlignmentOnlyAlgorithm() {
                return new BacktrackingDrivenSearch(FIRST_ALIGNMENT_ONLY,
                                ComparisonPrecision.ALL_RULES_CONSIDERED);
        }

        public static CommonAlgorithm getHungarianMethodDrivenLongestAligmentOnlyPatrialSolutionNotConsideredAlgorithm() {
                return new HungarianMethodDrivenSearch(FIRST_ALIGNMENT_ONLY,
                                ComparisonPrecision.ALL_RULES_CONSIDERED,
                                DEFAULT_MAXIMAL_RMSD_BASED_COST_OF_PAIR_OF_ALIGNED_DUPLEXES,
                                HungarianMethodDrivenSearch.AlgorithmType.FIRST);
        }

        public static CommonAlgorithm getHungarianMethodDrivenLongestAligmentPatrialSolutionNotConsideredAlgorithm() {
                return new HungarianMethodDrivenSearch(COMPREHENSIVE_SEARCH,
                                ComparisonPrecision.ALL_RULES_CONSIDERED,
                                DEFAULT_MAXIMAL_RMSD_BASED_COST_OF_PAIR_OF_ALIGNED_DUPLEXES,
                                HungarianMethodDrivenSearch.AlgorithmType.SECOND);
        }

        public static CommonAlgorithm getHungarianMethodDrivenLongestAligmentPatrialSolutionConsideredAlgorithm() {
                return new HungarianMethodDrivenSearch(COMPREHENSIVE_SEARCH,
                                ComparisonPrecision.ALL_RULES_CONSIDERED,
                                DEFAULT_MAXIMAL_RMSD_BASED_COST_OF_PAIR_OF_ALIGNED_DUPLEXES,
                                HungarianMethodDrivenSearch.AlgorithmType.THIRD);
        }

        public static CommonAlgorithm getComparisonAlgorithm(final ComparisonAlgorithmType algorithm) {
                switch (algorithm) {
                        case BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT:
                                return ComparisonAlgorithmFactory
                                                .getBacktrackingDrivenLongestAlignmentAlgorithm();
                        case BACKTRACKING_DRIVEN_FIRST_ALIGNMENT_ONLY:
                                return ComparisonAlgorithmFactory
                                                .getBacktrackingDrivenFirstAlignmentOnlyAlgorithm();
                        case HUNGARIAN_METHOD_DRIVEN_FIRST_ALIGNMENT_ONLY_PARTIAL_SOLUTIONS_NOT_CONSIDERED:
                                return ComparisonAlgorithmFactory
                                                .getHungarianMethodDrivenLongestAligmentOnlyPatrialSolutionNotConsideredAlgorithm();
                        case HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_NOT_CONSIDERED:
                                return ComparisonAlgorithmFactory
                                                .getHungarianMethodDrivenLongestAligmentPatrialSolutionNotConsideredAlgorithm();
                        case HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_CONSIDERED:
                                return ComparisonAlgorithmFactory
                                                .getHungarianMethodDrivenLongestAligmentPatrialSolutionConsideredAlgorithm();

                }
                return null;
        }
}

package edu.put.ma.descs;

public enum ComparisonPrecision {

    ALL_RULES_CONSIDERED(3), ALIGNED_RESIDUES_CONSIDERED_ONLY(1), ALL_RULES_EXCEPT_ALIGNMENT_RMSD(2);

    private final int level;

    ComparisonPrecision(final int level) {
        this.level = level;
    }

    public boolean atLeast(final ComparisonPrecision precision) {
        return precision.level >= level;
    }
}

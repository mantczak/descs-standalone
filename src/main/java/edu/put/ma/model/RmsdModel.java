package edu.put.ma.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.biojava.nbio.structure.SVDSuperimposer;

@RequiredArgsConstructor
@Getter
public class RmsdModel {

    public static final RmsdModel EMPTY_RMSD_MODEL = new RmsdModel(Double.MAX_VALUE, null);

    private final double alignmentRmsd;

    private SVDSuperimposer superimposer;

    public RmsdModel(final double alignmentRmsd, final SVDSuperimposer superimposer) {
        this(alignmentRmsd);
        this.superimposer = superimposer;
    }
}

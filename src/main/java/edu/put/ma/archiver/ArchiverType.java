package edu.put.ma.archiver;

import lombok.Getter;

public enum ArchiverType {

    TAR_GZ(".tar.gz"), ZIP(".zip");
    
    @Getter
    private final String postfix;
    
    ArchiverType(final String postfix) {
        this.postfix = postfix;
    }
}

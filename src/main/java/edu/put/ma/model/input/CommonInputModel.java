package edu.put.ma.model.input;

import org.apache.commons.cli.Options;

import edu.put.ma.io.FormatType;

public interface CommonInputModel {

    boolean isInputInitializedProperly();

    FormatType getInputFormat();

    FormatType getOutputFormat();

    Options constructSpecificOptions();

    void printHelp(String artifactId);
    
    String getInputModelString();
    
    String[] getArgs();
    
    boolean isOptionalFormatOptions();
}

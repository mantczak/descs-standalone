package edu.put.ma.model.input;

public interface FormatConverterInputModel extends CommonInputModel {

    String getInputFilePath();

    String getOutputFilePath();

    boolean isValid();
}

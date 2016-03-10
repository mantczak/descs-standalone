package edu.put.ma.descs;

import java.io.File;

import edu.put.ma.io.writer.Writer;
import edu.put.ma.model.ModelProperties;
import edu.put.ma.structure.StructureExtension;

public interface DescriptorsBuilder {

    void build(StructureExtension extendedStructure, int modelIndex, ModelProperties modelProperties);

    String saveDescriptors(File outputDir, String modelNo, Writer writer, DescriptorsFilter descriptorsFilter);

    void saveAtomNamePairsConsideredByInContactResiduesIdentificationExpression(File outputDir, Writer writer);

    int getThreadsCount();

    void setThreadsCount(int threadsCount);

    void close();
}

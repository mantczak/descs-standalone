package edu.put.ma.descs;

import static edu.put.ma.gaps.GapsDistributionImpl.RESIDUE_IN_GAP_PROXIMITY;
import static edu.put.ma.gaps.GapsDistributionImpl.RESIDUE_OUTSIDE_GAP;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Chain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.put.ma.access.ResiduesAccess;
import edu.put.ma.descs.algorithms.logger.ProcessingTimeLog;
import edu.put.ma.descs.contacts.ContactsInspector;
import edu.put.ma.gaps.GapsDistribution;
import edu.put.ma.io.writer.Writer;
import edu.put.ma.model.Descriptor;
import edu.put.ma.model.DescriptorImpl;
import edu.put.ma.model.ModelProperties;
import edu.put.ma.structure.StructureExtension;
import edu.put.ma.utils.ExecutorUtils;

public class DescriptorsBuilderImpl implements DescriptorsBuilder {

    public static final int DEFAULT_ELEMENT_SIZE = 5;

    public static final int DEFAULT_NEIBHOURHOOD_SZE = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorsBuilderImpl.class);

    private final ContactsInspector contactsInspector;

    private Map<Integer, Descriptor> descriptors;

    private ExecutorService executor;

    private int residuesCount;

    public DescriptorsBuilderImpl(final ContactsInspector contactsInspector, final int threadsCount) {
        this.contactsInspector = contactsInspector;
        prepareExecutorService(threadsCount);
    }

    @Override
    @ProcessingTimeLog
    public void build(final StructureExtension extendedStructure, final int modelIndex,
            final ModelProperties modelProperties) {
        final ResiduesAccess residuesAccess = modelProperties.getResiduesAccess();
        this.residuesCount = residuesAccess.getResiduesAccessIndexesSize();
        prepareDescriptors();
        final List<Chain> model = extendedStructure.getModelByIndex(Math.max(0, modelIndex));
        contactsInspector.constructInContactResiduesMatrix(model, modelProperties);
        for (int residueIndex = 0; residueIndex < residuesCount; residueIndex++) {
            final ImmutableList<Boolean> residueContacts = contactsInspector
                    .getContactsOfResidueByIndex(residueIndex);
            Descriptor descriptor = null;
            if (getThreadsCount() == 1) {
                descriptor = build(extendedStructure, modelIndex, modelProperties, residueIndex,
                        residueContacts);
            } else {
                descriptor = buildConcurrently(extendedStructure, modelIndex, modelProperties, residueIndex,
                        residueContacts);
            }
            if (descriptor != null) {
                descriptors.put(residueIndex, descriptor);
            }
        }
    }

    @Override
    public String saveDescriptors(final File outputDir, final String modelNo, final Writer writer,
            final DescriptorsFilter descriptorsFilter) {
        if (CollectionUtils.isNotEmpty(descriptors.values())) {
            final StringBuilder summary = new StringBuilder("Descriptors list");
            if (StringUtils.isNotBlank(modelNo)) {
                summary.append(String.format(" for model %s", modelNo));
            }
            summary.append(" is presented in the following table:").append("\n")
                    .append("ResNo\tId\tSegmentsNo\tElementsNo\tResiduesNo").append("\n");
            boolean isFirst = true;
            for (int residueIndex = 0; residueIndex < residuesCount; residueIndex++) {
                if (descriptors.containsKey(residueIndex)) {
                    final Descriptor descriptor = descriptors.get(residueIndex);
                    isFirst = saveSingleDescriptor(outputDir, writer, descriptorsFilter, summary, isFirst,
                            descriptor);
                }
            }
            if (!isFirst) {
                return summary.toString();
            }
        }
        return null;
    }

    @Override
    public void saveAtomNamePairsConsideredByInContactResiduesIdentificationExpression(final File outputDir,
            final Writer writer) {
        final String atomNamePairsString = contactsInspector.getAtomNamePairsString();
        if (StringUtils.isNotBlank(atomNamePairsString)) {
            final File outputFile = FileUtils.getFile(outputDir,
                    "atom-names-considered-by-in-contact-residues-identification-expression.list");
            LOGGER.info(String
                    .format("Atom names considered by in-contact residues identification expression: %s saved in file %s",
                            atomNamePairsString, outputFile.getAbsolutePath()));
            writer.write(atomNamePairsString, outputFile, "Atom name pairs");
        }
    }

    @Override
    public int getThreadsCount() {
        return contactsInspector.getThreadsCount();
    }

    @Override
    public void setThreadsCount(final int threadsCount) {
        prepareExecutorService(threadsCount);
        contactsInspector.setThreadsCount(threadsCount);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int residueIndex = 0; residueIndex < residuesCount; residueIndex++) {
            if (descriptors.containsKey(residueIndex)) {
                final Descriptor descriptor = descriptors.get(residueIndex);
                sb.append(descriptor.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public void close() {
        contactsInspector.close();
        ExecutorUtils.closeExecutor(executor);
    }

    private boolean saveSingleDescriptor(final File outputDir, final Writer writer,
            final DescriptorsFilter descriptorsFilter, final StringBuilder summary, final boolean isFirst,
            final Descriptor descriptor) {
        boolean first = isFirst;
        if (isDescriptorValid(descriptor) && isDescriptorAppropriate(descriptor, descriptorsFilter)) {
            final File descriptorFile = FileUtils.getFile(outputDir, descriptor.getId());
            if (first) {
                first = false;
            } else {
                summary.append("\n");
            }
            summary.append(descriptor.toString());
            writer.writeAtomsOnly(descriptor.getStructure(), descriptorFile);
        }
        return first;
    }

    private void prepareDescriptors() {
        descriptors = edu.put.ma.utils.CollectionUtils.prepareMap(descriptors);
    }

    private void prepareExecutorService(final int threadsCount) {
        executor = ExecutorUtils.prepareExecutorService(executor, threadsCount);
    }

    public static final boolean isElementCanBeCreated(final int residuesCount, final int residueIndex,
            final int residueGapFlag, final DescriptorResidueType residueType,
            final GapsDistribution gapsDistribution) {
        int count = 0;
        if (residueGapFlag == RESIDUE_OUTSIDE_GAP) {
            count++;
        }
        final int neibhourhoodSize = gapsDistribution.getNeibhourhoodSize();
        final int leftBoundaryIndex = Math.max(0, residueIndex - neibhourhoodSize);
        final int rightBoundaryIndex = Math.min(residuesCount - 1, residueIndex + neibhourhoodSize);
        for (int neibhourIndex = leftBoundaryIndex; neibhourIndex <= rightBoundaryIndex; neibhourIndex++) {
            if (neibhourIndex != residueIndex) {
                final int neibhourResidueGapFlag = gapsDistribution.getResidueGapFlag(neibhourIndex);
                if (((residueType == DescriptorResidueType.ORIGIN_CENTER)
                        && (residueGapFlag != RESIDUE_IN_GAP_PROXIMITY) && (residueGapFlag
                        * neibhourResidueGapFlag >= 0))
                        || (residueType == DescriptorResidueType.OTHER_CENTER)) {
                    count++;
                }
            }
        }
        return count == gapsDistribution.getElementSize();
    }

    public static void removeRedundantElementCenterIndexes(final int originResidueIndex,
            final List<Integer> elementsCenters, final int elementSize) {
        int originIndex = elementsCenters.indexOf(originResidueIndex);
        for (int currentElementCenterIndex = originIndex - 1; currentElementCenterIndex > 0; currentElementCenterIndex--) {
            if (isElementCenterIndexRedundant(elementsCenters, currentElementCenterIndex, elementSize)) {
                elementsCenters.remove(currentElementCenterIndex);
            }
        }
        originIndex = elementsCenters.indexOf(originResidueIndex);
        int currentElementCenterIndex = originIndex + 1;
        while (currentElementCenterIndex < CollectionUtils.size(elementsCenters) - 1) {
            if (isElementCenterIndexRedundant(elementsCenters, currentElementCenterIndex, elementSize)) {
                elementsCenters.remove(currentElementCenterIndex);
            } else {
                currentElementCenterIndex++;
            }
        }
    }

    private Descriptor build(final StructureExtension extendedStructure, final int modelIndex,
            final ModelProperties modelProperties, final int residueIndex,
            final ImmutableList<Boolean> residueContacts) {
        final ResiduesAccess residuesAccess = modelProperties.getResiduesAccess();
        final GapsDistribution gapsDistribution = modelProperties.getGapsDistribution();
        final int localResiduesCount = residuesAccess.getResiduesAccessIndexesSize();
        final List<Integer> elementsCenters = Lists.newArrayList();
        final List<Integer> inContactResidues = Lists.newArrayList();
        final int neibhourhoodSize = gapsDistribution.getNeibhourhoodSize();
        final int residueGapFlag = gapsDistribution.getResidueGapFlag(residueIndex);
        if (isElementCanBeCreated(localResiduesCount, residueIndex, residueGapFlag,
                DescriptorResidueType.ORIGIN_CENTER, gapsDistribution)) {
            buildElement(localResiduesCount, residueIndex, neibhourhoodSize, elementsCenters,
                    inContactResidues);
            buildElementForInContactResidues(residueContacts, gapsDistribution, localResiduesCount,
                    elementsCenters, inContactResidues, neibhourhoodSize);
            if ((CollectionUtils.size(elementsCenters) > 1)
                    && (CollectionUtils.size(inContactResidues) >= gapsDistribution.getElementSize())) {
                Collections.sort(elementsCenters);
                Collections.sort(inContactResidues);
                removeRedundantElementCenterIndexes(residueIndex, elementsCenters,
                        gapsDistribution.getElementSize());
                try {
                    return new DescriptorImpl(extendedStructure, modelIndex, modelProperties, residueIndex,
                            elementsCenters, inContactResidues);
                } catch (UnappropriateDescriptorException e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    private void buildElementForInContactResidues(final ImmutableList<Boolean> residueContacts,
            final GapsDistribution gapsDistribution, final int localResiduesCount,
            final List<Integer> elementsCenters, final List<Integer> inContactResidues,
            final int neibhourhoodSize) {
        for (int inContactResidueIndex = 0; inContactResidueIndex < localResiduesCount; inContactResidueIndex++) {
            if (residueContacts.get(inContactResidueIndex).booleanValue()) {
                final int inContactResidueGapFlag = gapsDistribution.getResidueGapFlag(inContactResidueIndex);
                if (isElementCanBeCreated(localResiduesCount, inContactResidueIndex, inContactResidueGapFlag,
                        DescriptorResidueType.OTHER_CENTER, gapsDistribution)) {
                    buildElement(localResiduesCount, inContactResidueIndex, neibhourhoodSize,
                            elementsCenters, inContactResidues);
                }
            }
        }
    }

    private Descriptor buildConcurrently(final StructureExtension extendedStructure, final int modelIndex,
            final ModelProperties modelProperties, final int residueIndex,
            final ImmutableList<Boolean> residueContacts) {
        final Callable<Descriptor> builder = new Callable<Descriptor>() {
            @Override
            public Descriptor call() throws Exception {
                return build(extendedStructure, modelIndex, modelProperties, residueIndex, residueContacts);
            }
        };
        final Future<Descriptor> result = executor.submit(builder);
        try {
            return result.get();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static final boolean isDescriptorValid(final Descriptor descriptor) {
        return (descriptor != null) && (descriptor.isValid());
    }

    private static final boolean isDescriptorAppropriate(final Descriptor descriptor,
            final DescriptorsFilter descriptorsFilter) {
        return (descriptorsFilter == null)
                || ((descriptorsFilter != null) && (descriptorsFilter.isAppropriate(descriptor)));
    }

    private static final void buildElement(final int residuesCount, final int residueIndex,
            final int neibhourhoodSize, final List<Integer> elementsCenters,
            final List<Integer> inContactResidues) {
        elementsCenters.add(residueIndex);
        final int leftBoundaryIndex = Math.max(0, residueIndex - neibhourhoodSize);
        final int rightBoundaryIndex = Math.min(residuesCount - 1, residueIndex + neibhourhoodSize);
        for (int inContactResidueIndex = leftBoundaryIndex; inContactResidueIndex <= rightBoundaryIndex; inContactResidueIndex++) {
            if (!inContactResidues.contains(inContactResidueIndex)) {
                inContactResidues.add(inContactResidueIndex);
            }
        }
    }

    private static final boolean isElementCenterIndexRedundant(final List<Integer> elementsCenters,
            final int currentElementCenterIndex, final int elementSize) {
        final int leftNeighbourElementCenterIndex = elementsCenters.get(currentElementCenterIndex - 1)
                .intValue();
        final int rightNeighbourElementCenterIndex = elementsCenters.get(currentElementCenterIndex + 1)
                .intValue();
        return rightNeighbourElementCenterIndex - leftNeighbourElementCenterIndex <= elementSize;
    }

}

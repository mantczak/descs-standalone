package edu.put.ma.io.writer;

import static edu.put.ma.io.model.PdbBundleImpl.CHAIN_ID_MAPPING_FILE_POSTFIX;
import static edu.put.ma.utils.StringUtils.ALIGNED_LEFT_MODE;
import static edu.put.ma.utils.StringUtils.NEW_LINE;
import static edu.put.ma.utils.MmcifUtils.ASCII_CODE_OF_CAPITAL_A;
import static edu.put.ma.utils.MmcifUtils.ASCII_CODE_OF_LOWER_CASE_A;
import static edu.put.ma.utils.MmcifUtils.ASCII_CODE_OF_ZERO;
import static edu.put.ma.io.reader.PdbReader.MODEL_NO_GROUP_INDEX;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import edu.put.ma.archiver.Archiver;
import edu.put.ma.archiver.ArchiverFactory;
import edu.put.ma.archiver.ArchiverType;
import edu.put.ma.io.model.ModelInfo;
import edu.put.ma.io.model.PdbBundleImpl;
import edu.put.ma.io.model.Structure3d;
import edu.put.ma.io.model.Structure3dFactory;
import edu.put.ma.structure.StructureExtensionImpl;
import edu.put.ma.utils.PreconditionUtils;
import edu.put.ma.utils.ResidueUtils;

public class PdbWriter extends CommonWriter {

    public static final String MODEL_PREFIX = "MODEL";

    public static final Pattern MODEL_RECORD_PATTERN = Pattern.compile("(" + MODEL_PREFIX
            + ")(\\s+)(\\d+)(\\s*)");

    private static final Logger LOGGER = LoggerFactory.getLogger(PdbWriter.class);

    private static final String TER_PREFIX = "TER";

    private static final int PDB_RECORD_LENGTH = 80;

    private static final String TER_RECORD = edu.put.ma.utils.StringUtils.extendString(TER_PREFIX,
            PDB_RECORD_LENGTH, ' ', ALIGNED_LEFT_MODE);

    private static final int MAXIMAL_ATOMS_NUMBER_SUPPORTED_BY_FORMAT_PDB = 99999;

    private static final int MAXIMAL_LENGTH_OF_CHAIN_ID_SUPPORTED_BY_FORMAT_PDB = 1;

    private static final int MAXIMAL_NUMBER_OF_CHAINS_SUPPORTED_BY_FORMAT_PDB = 62;

    @Override
    public void write(final Structure3d structure3d, final String outputFilePath) {
        final Structure structure = structure3d.getRawStructure();
        if (isValid(structure)) {
            if (isBundleNeeded(structure3d)) {
                String outputArchiveName = null;
                String filenameWithoutExtension = null;
                String outputArchivePath = null;
                if (ArchiverFactory.isArchive(outputFilePath)) {
                    outputArchivePath = outputFilePath;
                    outputArchiveName = FilenameUtils.getName(outputArchivePath);
                    filenameWithoutExtension = StringUtils.substring(
                            outputArchiveName,
                            0,
                            StringUtils.indexOf(outputArchiveName,
                                    ArchiverFactory.getArchiverPostfix(outputArchiveName)));
                } else {
                    final String outputDirPath = FilenameUtils.getFullPath(outputFilePath);
                    filenameWithoutExtension = FilenameUtils.getBaseName(outputFilePath);
                    outputArchivePath = new StringBuilder(outputDirPath).append(filenameWithoutExtension)
                            .append(ArchiverType.TAR_GZ.getPostfix()).toString();
                    outputArchiveName = FilenameUtils.getName(outputArchivePath);
                }
                if (StringUtils.contains(filenameWithoutExtension, '-')) {
                    final String outputDirPath = FilenameUtils.getFullPath(outputFilePath);
                    LOGGER.info("It is prohibited to use the following character '-' in the name of input file that stores large structures represented "
                            + "only in format CIF\nTherefore all occurrences of this character will be replaced with character '_'");
                    filenameWithoutExtension = StringUtils.replaceChars(filenameWithoutExtension, '-', '_');
                    outputArchiveName = new StringBuilder(filenameWithoutExtension).append(
                            ArchiverType.TAR_GZ.getPostfix()).toString();
                    outputArchivePath = new StringBuilder(outputDirPath).append(outputArchiveName).toString();
                }
                LOGGER.info(String
                        .format("Input structure in format CIF is too large to be stored as a single PDB file or considers various entity ids\n"
                                + "Therefore, it will be transformed into a set of PDB files archived together in a bundle %s",
                                outputArchivePath));
                final File tempDir = Files.createTempDir();
                try {
                    final List<Strand> strandsDistribution = computeStrandsDistribution(structure3d);
                    final List<StrandsBundle> strandBundles = computeStrandBundles(strandsDistribution);
                    final String chainIdMappingString = savePdbBundles(structure3d, strandBundles,
                            filenameWithoutExtension, tempDir);
                    saveChainIdMapping(chainIdMappingString, filenameWithoutExtension, tempDir);
                    final Archiver archiver = ArchiverFactory.getArchiver(outputArchiveName);
                    archiver.create(outputArchivePath, tempDir);
                } finally {
                    edu.put.ma.utils.FileUtils.deleteDirectory(tempDir);
                }
            } else {
                write(getStructureString(structure3d), outputFilePath);
            }
        }
    }

    @Override
    protected String getStructureString(final Structure3d structure3d) {
        final String structureString = structure3d.getRawStructure().toPDB();
        final List<String> records = Arrays.asList(StringUtils.split(structureString, NEW_LINE));
        final List<String> extendedRecords = (List<String>) CollectionUtils.collect(records,
                new PdbTransformer(structure3d));
        final int extendedAtomRecordsNo = CollectionUtils.size(extendedRecords);
        int recordIndex = getLastRecordIndex(extendedRecords);
        if ((++recordIndex == extendedAtomRecordsNo)
                || (!StringUtils.startsWith(extendedRecords.get(recordIndex), TER_PREFIX))) {
            extendedRecords.add(recordIndex, TER_RECORD);
        }
        return StringUtils.join(extendedRecords, NEW_LINE) + NEW_LINE;
    }

    @Override
    protected String transformOutputFilePath(final String outputFilePath) {
        return transformOutputFilePath(outputFilePath, "pdb");
    }

    private String savePdbBundles(final Structure3d structure3d, final List<StrandsBundle> strandBundles,
            final String filenameWithoutExtension, final File tempDir) {
        final int bundlesCount = CollectionUtils.size(strandBundles);
        final StringBuilder chainIdMappingStringBuilder = new StringBuilder(
                "    New chain ID            Original chain ID            Entity ID\n\n");
        int bundleNo = 1;
        for (StrandsBundle bundle : strandBundles) {
            final StringBuilder bundleFilenameBuilder = new StringBuilder(filenameWithoutExtension);
            if (structure3d.nrModels() > 1) {
                bundleFilenameBuilder.append("-").append(bundle.getModelNo());
            }
            bundleFilenameBuilder.append("-").append(bundleNo++);
            final String bundleFilename = bundleFilenameBuilder.toString();
            final File bundleFile = FileUtils.getFile(tempDir, bundleFilename);
            final Structure bundleStructure = getStructure(structure3d.getRawStructure(), bundle);
            final List<ModelInfo> bundleModelInfos = getBundleModelInfos(bundle, structure3d.getModelInfos());
            write(getStructureString(Structure3dFactory.construct(bundleStructure, bundleModelInfos)),
                    bundleFile.getAbsolutePath());
            chainIdMappingStringBuilder.append(bundleFilename + ".pdb").append(":\n");
            chainIdMappingStringBuilder.append(bundle.getChainIdMappingString());
            if (bundleNo <= bundlesCount) {
                chainIdMappingStringBuilder.append(NEW_LINE);
            }
        }
        return chainIdMappingStringBuilder.toString();
    }

    private void saveChainIdMapping(final String chainIdMappingString, final String filenameWithoutExtension,
            final File outputDir) {
        final String outputFilename = filenameWithoutExtension + CHAIN_ID_MAPPING_FILE_POSTFIX;
        final File outputFile = FileUtils.getFile(outputDir, outputFilename);
        LOGGER.info(String.format("Chain id mapping is stored in file %s", outputFilename));
        write(chainIdMappingString, outputFile, "Chain id mapping");
    }

    private static final List<ModelInfo> getBundleModelInfos(final StrandsBundle bundle,
            final List<ModelInfo> modelInfos) {
        Preconditions.checkNotNull(bundle, "Strands bundle should be defined properly");
        final List<ModelInfo> result = Lists.newArrayList();
        final int bundleModelIndex = bundle.getModelNo() - 1;
        final int modelsNo = CollectionUtils.size(modelInfos);
        if (modelsNo > 0) {
            PreconditionUtils.checkIfIndexInRange(bundleModelIndex, 0, modelsNo, "Models number");
            result.add(modelInfos.get(bundleModelIndex));
        }
        return result;
    }

    private static final int getLastRecordIndex(final List<String> records) {
        int recordIndex = CollectionUtils.size(records) - 1;
        for (; recordIndex >= 0; recordIndex--) {
            if (ResidueUtils.isCoordinatesRecord(records.get(recordIndex))) {
                break;
            }
        }
        return recordIndex;
    }

    private static final Structure getStructure(final Structure structure, final StrandsBundle bundle) {
        Preconditions.checkNotNull(structure, "Structure should be initialized properly");
        Preconditions.checkNotNull(bundle, "Strands bundle should be defined properly");
        final int bundleModelIndex = bundle.getModelNo() - 1;
        final Structure result = StructureExtensionImpl.prepareModelOfStructureByIndex(structure.clone(),
                bundleModelIndex);
        final List<String> ids = bundle.getIds();
        final List<Chain> chains = result.getChains();
        final int chainsNo = CollectionUtils.size(chains);
        for (int chainIndex = chainsNo - 1; chainIndex >= 0; chainIndex--) {
            final Chain chain = chains.get(chainIndex);
            if (!ids.contains(chain.getChainID())) {
                chains.remove(chainIndex);
            } else {
                chain.setChainID(bundle.getStrandSingleLetterIdById(chain.getChainID()));
            }
        }
        PdbBundleImpl.renumerateAtoms(result);
        return result;
    }

    private static final List<StrandsBundle> computeStrandBundles(final List<Strand> strandsDistribution) {
        final List<StrandsBundle> strandBundles = Lists.newArrayList();
        StrandsBundle bundle = new StrandsBundle();
        int currentModelNo = -1;
        for (Strand strand : strandsDistribution) {
            if (currentModelNo == -1) {
                currentModelNo = strand.modelNo;
            }
            if (!((bundle.atomsNo + strand.atomsNo <= MAXIMAL_ATOMS_NUMBER_SUPPORTED_BY_FORMAT_PDB)
                    && (bundle.getStrandsNo() + 1 <= MAXIMAL_NUMBER_OF_CHAINS_SUPPORTED_BY_FORMAT_PDB) && ((currentModelNo == -1) || (currentModelNo == strand.modelNo)))) {
                strandBundles.add(bundle);
                bundle = new StrandsBundle();
                currentModelNo = strand.modelNo;
            }
            bundle.addStrand(strand);
        }
        strandBundles.add(bundle);
        return strandBundles;
    }

    private static final List<Strand> computeStrandsDistribution(final Structure3d structure3d) {
        final List<Strand> strandsDistribution = Lists.newArrayList();
        final List<ModelInfo> modelInfos = structure3d.getModelInfos();
        for (int modelIndex = 0; modelIndex < structure3d.nrModels(); modelIndex++) {
            PreconditionUtils.checkIfIndexInRange(modelIndex, 0, CollectionUtils.size(modelInfos),
                    "Model info");
            final ModelInfo modelInfo = modelInfos.get(modelIndex);
            int strandIndex = 0;
            for (Chain chain : structure3d.getModel(modelIndex)) {
                int atomsNo = getAtomsNo(chain.getAtomGroups());
                strandsDistribution.add(new Strand((CollectionUtils.sizeIsEmpty(modelInfos)) ? modelIndex + 1
                        : modelInfos.get(modelIndex).getModelNo(), chain.getChainID(), modelInfo
                        .getEntityIdByStrandIndex(strandIndex++), atomsNo));
            }
        }
        return Collections.unmodifiableList(strandsDistribution);
    }

    private static final boolean isBundleNeeded(final Structure3d structure3d) {
        boolean bundleNeeded = false;
        if (structure3d.shouldEntityIdsBeConsidered() || isLargeStructure(structure3d)) {
            bundleNeeded = true;
        }
        return bundleNeeded;
    }

    private static final boolean isLargeStructure(final Structure3d structure3d) {
        boolean result = false;
        int atomsNo = 0;
        for (int modelIndex = 0; modelIndex < structure3d.nrModels() && !result; modelIndex++) {
            for (Chain chain : structure3d.getModel(modelIndex)) {
                if (StringUtils.length(chain.getChainID()) > MAXIMAL_LENGTH_OF_CHAIN_ID_SUPPORTED_BY_FORMAT_PDB) {
                    result = true;
                    break;
                }
                atomsNo += getAtomsNo(chain.getAtomGroups());
            }
        }
        if (atomsNo > MAXIMAL_ATOMS_NUMBER_SUPPORTED_BY_FORMAT_PDB) {
            result = true;
        }
        return result;
    }

    private static final int getAtomsNo(final List<Group> residues) {
        int atomsNo = 0;
        for (Group residue : residues) {
            atomsNo += CollectionUtils.size(residue.getAtoms());
        }
        return atomsNo;
    }

    @RequiredArgsConstructor
    private static final class PdbTransformer implements Transformer<String, String> {

        private static final int MODEL_NO_RECORD_SIZE = 14;

        private static final int PDB_CHAIN_ID_END_INDEX = 22;

        private static final int PDB_CHAIN_ID_START_INDEX = 21;

        private final Structure3d structure3d;

        private String previousChainId;

        private int modelIndex = -1;

        @Override
        public String transform(final String record) {
            String currentRecord = StringUtils.trim(record);
            final StringBuilder newAtomRecordBuilder = new StringBuilder();
            if (StringUtils.startsWith(currentRecord, MODEL_PREFIX)) {
                currentRecord = updateModelNo(structure3d, currentRecord);
            } else if (ResidueUtils.isCoordinatesRecord(currentRecord)) {
                final String currentChainId = StringUtils.substring(currentRecord, PDB_CHAIN_ID_START_INDEX,
                        PDB_CHAIN_ID_END_INDEX);
                if ((StringUtils.isNotBlank(previousChainId))
                        && (!StringUtils.equals(previousChainId, currentChainId))) {
                    newAtomRecordBuilder.append(TER_RECORD).append(NEW_LINE);
                }
                previousChainId = currentChainId;
            } else {
                if (StringUtils.isNotBlank(previousChainId)) {
                    previousChainId = null;
                    newAtomRecordBuilder.append(TER_RECORD).append(NEW_LINE);
                }
            }
            newAtomRecordBuilder.append(edu.put.ma.utils.StringUtils.extendString(currentRecord,
                    PDB_RECORD_LENGTH, ' ', ALIGNED_LEFT_MODE));
            return newAtomRecordBuilder.toString();
        }

        private String updateModelNo(final Structure3d structure3d, final String currentRecord) {
            String outputRecord = currentRecord;
            final Matcher modelRecordMatcher = MODEL_RECORD_PATTERN.matcher(outputRecord);
            if (modelRecordMatcher.matches()) {
                final String modelNo = (CollectionUtils.sizeIsEmpty(structure3d.getModelInfos())) ? String
                        .valueOf(modelRecordMatcher.group(MODEL_NO_GROUP_INDEX)) : String.valueOf(structure3d
                        .getModelInfo(++modelIndex).getModelNo());
                outputRecord = new StringBuilder(MODEL_PREFIX).append(
                        edu.put.ma.utils.StringUtils.extendString(modelNo,
                                MODEL_NO_RECORD_SIZE - StringUtils.length(MODEL_PREFIX), ' ',
                                !ALIGNED_LEFT_MODE)).toString();
            }
            return outputRecord;
        }

    }

    @ToString
    @RequiredArgsConstructor
    private static final class Strand {

        final int modelNo;

        final String id;

        final int entityId;

        final int atomsNo;

        String singleLetterId;
    }

    @ToString
    private static final class StrandsBundle {

        private static final int CHAIN_ID_FROM_CIF_AND_ENTITY_ID_FIELD_SIZE = 26;

        private static final int CHAIN_ID_FROM_PDB_FIELD_SIZE = 11;

        @ToString
        private class CodeRetriever {

            char code = (char) (ASCII_CODE_OF_CAPITAL_A - 1);

            final String retrieve() {
                switch (code) {
                    case 'Z':
                        code = (char) (ASCII_CODE_OF_LOWER_CASE_A - 1);
                        break;
                    case 'z':
                        code = (char) (ASCII_CODE_OF_ZERO - 1);
                        break;
                    case '9':
                        code = (char) (ASCII_CODE_OF_CAPITAL_A - 1);
                        break;
                    default:
                }
                return Character.toString(++code);
            }
        }

        final List<Strand> strands;

        final Map<String, Integer> idMapping;

        final CodeRetriever codeRetriever;

        int atomsNo = 0;

        StrandsBundle() {
            this.strands = Lists.newArrayList();
            this.idMapping = Maps.newHashMap();
            this.codeRetriever = new CodeRetriever();
        }

        String getChainIdMappingString() {
            final StringBuilder chainIdMappingStringBuilder = new StringBuilder();
            for (Strand strand : strands) {
                chainIdMappingStringBuilder
                        .append(edu.put.ma.utils.StringUtils.extendString(strand.singleLetterId,
                                CHAIN_ID_FROM_PDB_FIELD_SIZE, ' ', !ALIGNED_LEFT_MODE))
                        .append(edu.put.ma.utils.StringUtils.extendString(strand.id,
                                CHAIN_ID_FROM_CIF_AND_ENTITY_ID_FIELD_SIZE, ' ', !ALIGNED_LEFT_MODE))
                        .append(edu.put.ma.utils.StringUtils.extendString(String.valueOf(strand.entityId),
                                CHAIN_ID_FROM_CIF_AND_ENTITY_ID_FIELD_SIZE, ' ', !ALIGNED_LEFT_MODE))
                        .append(NEW_LINE);
            }
            return chainIdMappingStringBuilder.toString();
        }

        void addStrand(final Strand strand) {
            strand.singleLetterId = codeRetriever.retrieve();
            if (!idMapping.containsKey(strand.id)) {
                idMapping.put(strand.id, getStrandsNo());
                strands.add(strand);
                atomsNo += strand.atomsNo;
            } else {
                LOGGER.warn(String.format("Strand id [%s] is duplicated", strand.id));
            }
        }

        String getStrandSingleLetterIdById(final String id) {
            return strands.get(getStrandIndexById(id)).singleLetterId;
        }

        int getStrandsNo() {
            return CollectionUtils.size(strands);
        }

        int getModelNo() {
            if (!CollectionUtils.sizeIsEmpty(strands)) {
                return strands.get(0).modelNo;
            }
            throw new IllegalStateException("Strands bundle is not initialized properly");
        }

        List<String> getIds() {
            return Lists.newArrayList(idMapping.keySet());
        }

        private int getStrandIndexById(final String id) {
            if (!idMapping.containsKey(id)) {
                throw new IllegalArgumentException(String.format(
                        "There is no strand with the following id [%s]", id));
            }
            return idMapping.get(id);
        }
    }

}

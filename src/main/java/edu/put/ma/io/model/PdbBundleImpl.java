package edu.put.ma.io.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.ToString;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.put.ma.io.reader.Reader;

public class PdbBundleImpl implements PdbBundle {

    private static final int DEFAULT_MODEL_NO = 1;

    private static final int CHAIN_ID_FROM_PDB_GROUP_INDEX = 1;

    private static final int CHAIN_ID_FROM_CIF_GROUP_INDEX = 3;

    private static final int ENTITY_ID_GROUP_INDEX = 6;

    private static final int MODEL_NO_GROUP_INDEX = 4;

    public static final String CHAIN_ID_MAPPING_FILE_POSTFIX = "-chain-id-mapping.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(PdbBundleImpl.class);

    private Reader reader;

    private final Map<String, File> files;

    private final List<Strands> bundles;

    private final Structure structure;

    private final List<Integer> modelNos;

    private final Map<Integer, List<Integer>> modelEntityIds;

    public PdbBundleImpl(final Reader reader, final File bundleDir) {
        this.reader = reader;
        this.files = Maps.newHashMap();
        this.bundles = Lists.newArrayList();
        this.modelNos = Lists.newArrayList();
        final List<File> inputFiles = Arrays.asList(bundleDir.listFiles());
        File chainIdMappingFile = null;
        for (File file : inputFiles) {
            if (StringUtils.endsWithIgnoreCase(file.getName(), CHAIN_ID_MAPPING_FILE_POSTFIX)) {
                chainIdMappingFile = file;
            } else if (StringUtils.endsWithIgnoreCase(file.getName(), ".pdb")) {
                files.put(file.getName(), file);
            }
        }
        if (chainIdMappingFile == null) {
            throw new IllegalStateException(
                    "Inappropriate bundle representation - chain id mapping file not found [e.g., 1vvj-chain-id-mapping.txt]");
        }
        readChainIdMappingFile(chainIdMappingFile);
        this.structure = constructStructure();
        this.modelEntityIds = constructModelEntityIds();
    }

    @Override
    public Structure getStructure() {
        return structure;
    }

    @Override
    public List<Integer> getModelNos() {
        return Collections.unmodifiableList(modelNos);
    }

    @Override
    public Map<Integer, List<Integer>> getModelEntityIds() {
        return Collections.unmodifiableMap(modelEntityIds);
    }

    @Override
    public List<ModelInfo> getModelInfos() {
        final List<ModelInfo> result = Lists.newArrayList();
        for (Integer modelNo : modelNos) {
            result.add(new ModelInfoImpl(modelNo, modelEntityIds.get(modelNo - 1)));
        }
        return result;
    }

    public static final void renumerateAtoms(final Structure structure) {
        int atomNo = 1;
        for (int modelIndex = 0; modelIndex < structure.nrModels(); modelIndex++) {
            for (Chain chain : structure.getModel(modelIndex)) {
                atomNo = renumerateResidueAtoms(chain, atomNo);
            }
        }
    }

    private Structure constructStructure() {
        Structure constructedStructure = null;
        int previousModelNo = -1;
        for (Strands bundle : bundles) {
            Structure currentStructure = getBundleStructure(bundle);
            if (constructedStructure == null) {
                constructedStructure = currentStructure;
                previousModelNo = bundle.modelNo;
            } else {
                if (bundle.modelNo == previousModelNo) {
                    constructedStructure.getModel(bundle.modelNo - 1).addAll(currentStructure.getModel(0));
                } else {
                    constructedStructure.addModel(currentStructure.getModel(0));
                    previousModelNo = bundle.modelNo;
                }
            }
        }
        return constructedStructure;
    }

    private Map<Integer, List<Integer>> constructModelEntityIds() {
        Map<Integer, List<Integer>> constructedModelEntityIds = Maps.newHashMap();
        int previousModelNo = -1;
        for (Strands bundle : bundles) {
            if (!bundle.areEntityIdsDefined()) {
                constructedModelEntityIds = edu.put.ma.utils.CollectionUtils.prepareMap(modelEntityIds);
                break;
            } else {
                if ((previousModelNo == -1) || (bundle.modelNo != previousModelNo)) {
                    constructedModelEntityIds.put(bundle.modelNo - 1, bundle.entityIds);
                    previousModelNo = bundle.modelNo;
                } else {
                    constructedModelEntityIds.get(bundle.modelNo - 1).addAll(bundle.entityIds);
                }
            }
        }
        return constructedModelEntityIds;
    }

    private Structure getBundleStructure(final Strands bundle) {
        final Structure3d structure3d = reader.read(files.get(bundle.filename).getAbsolutePath());
        final Structure rwaStructure = structure3d.getRawStructure();
        if (rwaStructure.nrModels() == 1) {
            final int chainsNo = CollectionUtils.size(rwaStructure.getModel(0));
            if (chainsNo == bundle.getMappedChainsNo()) {
                for (Chain chain : rwaStructure.getChains()) {
                    chain.setChainID(bundle.getIdBySingleLetterId(chain.getChainID()));
                }
            } else {
                throw new IllegalStateException(
                        "Number of chains included in the structure and the chain id mapping file are different");
            }
        } else {
            throw new IllegalStateException("Single PDB file cannot contain chains from multiple models");
        }
        return rwaStructure;
    }

    private void readChainIdMappingFile(final File chainIdMappingFile) {
        try {
            final Pattern ourPdbFilenamePattern = Pattern
                    .compile("([^-]+)((-)([0-9]+))?(-)([0-9]+)(\\.pdb:)");
            final Pattern emblPdbFilenamePattern = Pattern.compile("(.+)([0-9]+)(\\.pdb:)");
            final Pattern chainIdMappingPattern = Pattern
                    .compile("([A-Za-z0-9])(\\s+)([A-Za-z0-9]+)((\\s+)([0-9]+))?");
            Strands bundle = null;
            for (String line : FileUtils.readLines(chainIdMappingFile)) {
                final String processedLine = (StringUtils.isNotBlank(line)) ? StringUtils.trim(line)
                        .replaceAll("\\s+", " ") : StringUtils.trim(line);
                final Matcher ourPdbFilenameMatcher = ourPdbFilenamePattern.matcher(processedLine);
                final Matcher emblPdbFilenameMatcher = emblPdbFilenamePattern.matcher(processedLine);
                if ((ourPdbFilenameMatcher.matches()) || (emblPdbFilenameMatcher.matches())) {
                    final Matcher matcherUsed = (ourPdbFilenameMatcher.matches()) ? ourPdbFilenameMatcher
                            : emblPdbFilenameMatcher;
                    addBundle(bundle);
                    final String pdbFilename = getPdbFilename(matcherUsed);
                    final int modelNo = getModelNo(matcherUsed);
                    bundle = new Strands(pdbFilename, modelNo);
                    extendModelNos(modelNo);
                } else {
                    parseChainIdMapping(chainIdMappingPattern, bundle, processedLine);
                }
            }
            addBundle(bundle);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void extendModelNos(final int modelNo) {
        if (!modelNos.contains(modelNo)) {
            modelNos.add(modelNo);
        }
    }

    private void addBundle(final Strands bundle) {
        if (bundle != null) {
            bundles.add(bundle);
        }
    }

    private String getPdbFilename(final Matcher pdbFilenameMatcher) {
        final String pdbFilename = edu.put.ma.utils.StringUtils.deleteLastCharacter(pdbFilenameMatcher
                .group(0));
        if (!files.containsKey(pdbFilename)) {
            throw new IllegalStateException(String.format(
                    "One of the considered PDB bundles is not found [%s]", pdbFilename));
        }
        return pdbFilename;
    }

    private static final int renumerateResidueAtoms(final Chain chain, final int atomNo) {
        int currentAtomNo = atomNo;
        for (Group residue : chain.getAtomGroups()) {
            for (Atom atom : residue.getAtoms()) {
                atom.setPDBserial(currentAtomNo++);
            }
        }
        return currentAtomNo;
    }

    private static final void parseChainIdMapping(final Pattern chainIdMappingPattern, final Strands bundle,
            final String line) {
        final Matcher chainIdMappingMatcher = chainIdMappingPattern.matcher(line);
        if (chainIdMappingMatcher.matches()) {
            bundle.addIdMapping(chainIdMappingMatcher.group(CHAIN_ID_FROM_PDB_GROUP_INDEX),
                    chainIdMappingMatcher.group(CHAIN_ID_FROM_CIF_GROUP_INDEX));
            if (chainIdMappingMatcher.groupCount() >= ENTITY_ID_GROUP_INDEX) {
                final String entityIdString = chainIdMappingMatcher.group(ENTITY_ID_GROUP_INDEX);
                if (StringUtils.isNumeric(entityIdString)) {
                    bundle.addEntityId(Integer.parseInt(entityIdString));
                }
            }
        }
    }

    private static final int getModelNo(final Matcher pdbFilenameMatcher) {
        if (pdbFilenameMatcher.groupCount() > MODEL_NO_GROUP_INDEX) {
            final String modelNoString = pdbFilenameMatcher.group(MODEL_NO_GROUP_INDEX);
            return StringUtils.isNotBlank(modelNoString) ? Integer.parseInt(modelNoString) : DEFAULT_MODEL_NO;
        }
        return DEFAULT_MODEL_NO;
    }

    @ToString
    private static final class Strands {

        @Getter
        final String filename;

        final int modelNo;

        final Map<String, String> idMapping;

        final List<Integer> entityIds;

        private Strands(final String filename, final int modelNo) {
            this.filename = filename;
            this.modelNo = modelNo;
            this.idMapping = Maps.newLinkedHashMap();
            this.entityIds = Lists.newArrayList();
        }

        void addIdMapping(final String singleLetterId, final String id) {
            if (!idMapping.containsKey(singleLetterId)) {
                idMapping.put(singleLetterId, id);
            } else {
                LOGGER.warn(String.format("Strand single letter id [%s] is duplicated", singleLetterId));
            }
        }

        void addEntityId(final int entityId) {
            entityIds.add(entityId);
        }

        boolean areEntityIdsDefined() {
            return !CollectionUtils.sizeIsEmpty(entityIds);
        }

        int getMappedChainsNo() {
            return CollectionUtils.size(idMapping);
        }

        String getIdBySingleLetterId(final String singleLetterId) {
            if (idMapping.containsKey(singleLetterId)) {
                return idMapping.get(singleLetterId);
            }
            throw new IllegalArgumentException(String.format("There is no strand with the following id [%s]",
                    singleLetterId));
        }
    }

}

package edu.put.ma.io.writer;

import static edu.put.ma.io.model.ModelInfoImpl.ENTITY_ID_NOT_KNOWN;
import static edu.put.ma.utils.MmcifUtils.FIELD_NAMES_PREFIX;
import static edu.put.ma.utils.MmcifUtils.LABEL_ASYM_ID_FIELD_NAME;
import static edu.put.ma.utils.MmcifUtils.MMCIF_RECORD_LABEL;
import static edu.put.ma.utils.MmcifUtils.ASCII_CODE_OF_CAPITAL_A;
import static edu.put.ma.utils.MmcifUtils.ASCII_CODE_OF_CAPITAL_Z;
import static edu.put.ma.utils.MmcifUtils.ASCII_CODE_OF_LOWER_CASE_A;
import static edu.put.ma.utils.MmcifUtils.ASCII_CODE_OF_LOWER_CASE_Z;
import static edu.put.ma.utils.StringUtils.NEW_LINE;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Structure;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.put.ma.io.model.ModelInfo;
import edu.put.ma.io.model.Structure3d;
import edu.put.ma.structure.StructureExtensionImpl;
import edu.put.ma.utils.MmcifUtils;
import edu.put.ma.utils.PreconditionUtils;
import edu.put.ma.utils.ResidueUtils;

public class MmcifWriter extends CommonWriter {

    private static final int ALPHABET_SIZE = 25;

    private static final String DASH_CHARACTER = "#";

    private static final boolean MULTIPLE_MODEL_STRUCTURE_FLAG = true;

    private static final String MODEL_NO_FIELD_NAME = "pdbx_PDB_model_num";

    private static final String AUTH_ASYM_ID_FIELD_NAME = "auth_asym_id";

    private static final String LABEL_ENTITY_ID_FIELD_NAME = "label_entity_id";

    private static final String INS_CODE_FIELD_NAME = "pdbx_PDB_ins_code";

    @Override
    public void write(final Structure3d structure3d, final String outputFilePath) {
        if (isValid(structure3d.getRawStructure())) {
            write(getStructureString(structure3d), outputFilePath);
        }
    }

    @Override
    protected String getStructureString(final Structure3d structure3d) {
        if (structure3d.nrModels() > 1) {
            return processOfMultipleModelStructure(structure3d);
        }
        final String structureMmcif = structure3d.getRawStructure().toMMCIF();
        return postprocess(structureMmcif, structure3d.getModelInfo(0), !MULTIPLE_MODEL_STRUCTURE_FLAG, 0);
    }

    @Override
    protected String transformOutputFilePath(final String outputFilePath) {
        return transformOutputFilePath(outputFilePath, "cif");
    }

    private static final String processOfMultipleModelStructure(final Structure3d structure3d) {
        final StringBuilder result = new StringBuilder();
        int nextChainIndex = 0;
        for (int modelIndex = 0; modelIndex < structure3d.nrModels(); modelIndex++) {
            Structure modelStructure = structure3d.cloneStructure();
            modelStructure = StructureExtensionImpl
                    .prepareModelOfStructureByIndex(modelStructure, modelIndex);
            final String modelMmcif = modelStructure.toMMCIF();
            PreconditionUtils.checkIfStringIsBlank(modelMmcif,
                    "Structure in format mmCIF is not initialized properly");
            result.append(postprocess(modelMmcif, structure3d.getModelInfo(modelIndex),
                    MULTIPLE_MODEL_STRUCTURE_FLAG, nextChainIndex));
            nextChainIndex += CollectionUtils.size(modelStructure.getModel(0));
        }
        return result.append(DASH_CHARACTER).toString();
    }

    private static final String getChainIdByChainIndex(final int inputChainIndex) {
        int chainIndex = inputChainIndex;
        final StringBuilder chainIdBuilder = new StringBuilder();
        while (chainIndex > ALPHABET_SIZE) {
            int characterAsciiCode = chainIndex % (ALPHABET_SIZE + 1) + ASCII_CODE_OF_CAPITAL_A;
            chainIndex = chainIndex / (ALPHABET_SIZE + 1) - 1;
            chainIdBuilder.append((char) characterAsciiCode);
        }
        chainIdBuilder.append((char) (chainIndex + ASCII_CODE_OF_CAPITAL_A));
        return chainIdBuilder.toString();
    }

    private static final String postprocess(final String structureMmcif, final ModelInfo modelInfo,
            final boolean isMultiModelStructure, final int inputChainIndex) {
        final StringBuilder result = new StringBuilder();
        final String[] mmcifRecords = StringUtils.split(structureMmcif, NEW_LINE);
        final List<String> fieldLabels = Lists.newArrayList();
        String previousAuthAsymId = null;
        String newLabelAsymIdString = null;
        int newEntityId = 1;
        int chainIndex = inputChainIndex;
        for (String mmcifRecord : mmcifRecords) {
            if (ResidueUtils.isCoordinatesRecord(mmcifRecord)) {
                final List<MmcifUtils.Field> fieldsDistribution = MmcifUtils
                        .computeFieldsDistribution(mmcifRecord);
                String updatedMmcifRecord = updateInsCodeAndModelNoWhenNeeded(modelInfo, fieldLabels,
                        mmcifRecord, fieldsDistribution);
                final MmcifUtils.Field authAsymIdField = MmcifUtils.getFieldByName(AUTH_ASYM_ID_FIELD_NAME,
                        fieldLabels, fieldsDistribution);
                final String authAsymIdString = MmcifUtils
                        .getFieldString(updatedMmcifRecord, authAsymIdField);
                if ((StringUtils.isBlank(previousAuthAsymId))
                        || (!StringUtils.equals(previousAuthAsymId, authAsymIdString))) {
                    newEntityId = modelInfo.getEntityIdByStrandIndex(chainIndex);
                    newLabelAsymIdString = getChainIdByChainIndex(chainIndex++);
                    previousAuthAsymId = authAsymIdString;
                }
                updatedMmcifRecord = updateAsymAndEntityId(fieldLabels, newLabelAsymIdString, newEntityId,
                        fieldsDistribution, updatedMmcifRecord);
                result.append(updatedMmcifRecord).append(NEW_LINE);
            } else if (!StringUtils.startsWith(mmcifRecord, DASH_CHARACTER)) {
                if (modelInfo.getModelNo() == 1) {
                    result.append(mmcifRecord).append(NEW_LINE);
                }
                if (StringUtils.startsWith(mmcifRecord, FIELD_NAMES_PREFIX)) {
                    MmcifUtils.addFieldLabel(fieldLabels, mmcifRecord);
                }
            }
        }
        if (!isMultiModelStructure) {
            result.append(DASH_CHARACTER);
        }
        return result.toString();
    }

    private static final String updateAsymAndEntityId(final List<String> fieldLabels,
            String newLabelAsymIdString, int newEntityId, final List<MmcifUtils.Field> fieldsDistribution,
            final String mmcifRecord) {
        String updatedMmcifRecord = updateField(mmcifRecord, fieldLabels, fieldsDistribution,
                LABEL_ASYM_ID_FIELD_NAME, newLabelAsymIdString);
        if (newEntityId != ENTITY_ID_NOT_KNOWN) {
            updatedMmcifRecord = updateField(updatedMmcifRecord, fieldLabels, fieldsDistribution,
                    LABEL_ENTITY_ID_FIELD_NAME, String.valueOf(newEntityId));
        }
        return updatedMmcifRecord;
    }

    private static final String updateInsCodeAndModelNoWhenNeeded(final ModelInfo modelInfo,
            final List<String> fieldLabels, final String mmcifRecord,
            final List<MmcifUtils.Field> fieldsDistribution) {
        String updatedMmcifRecord = updateInsCode(mmcifRecord, fieldLabels, fieldsDistribution);
        if (modelInfo.getModelNo() > 1) {
            updatedMmcifRecord = updateField(updatedMmcifRecord, fieldLabels, fieldsDistribution,
                    MODEL_NO_FIELD_NAME, String.valueOf(modelInfo.getModelNo()));
        }
        return updatedMmcifRecord;
    }

    private static final String updateField(final String mmcifRecord, final List<String> fieldLabels,
            final List<MmcifUtils.Field> fieldsDistribution, final String fieldName, final String newValue) {
        final MmcifUtils.Field field = MmcifUtils.getFieldByName(fieldName, fieldLabels, fieldsDistribution);
        final String currentValue = MmcifUtils.getFieldString(mmcifRecord, field);
        final String updatedMmcifRecord = updateMmcifRecord(mmcifRecord, field, newValue);
        updateFieldsDistribution(fieldLabels, fieldsDistribution, fieldName, currentValue, newValue);
        return updatedMmcifRecord;
    }

    private static final String updateInsCode(final String mmcifRecord, final List<String> fieldLabels,
            final List<MmcifUtils.Field> fieldsDistribution) {
        final MmcifUtils.Field insCodeField = MmcifUtils.getFieldByName(INS_CODE_FIELD_NAME, fieldLabels,
                fieldsDistribution);
        final String insCodeFieldString = MmcifUtils.getFieldString(mmcifRecord, insCodeField);
        if ((!StringUtils.equals("?", insCodeFieldString)) && (StringUtils.isNumeric(insCodeFieldString))) {
            final int insCodeInt = Integer.parseInt(insCodeFieldString);
            if (((insCodeInt >= ASCII_CODE_OF_CAPITAL_A) && (insCodeInt <= ASCII_CODE_OF_CAPITAL_Z))
                    || ((insCodeInt >= ASCII_CODE_OF_LOWER_CASE_A) && (insCodeInt <= ASCII_CODE_OF_LOWER_CASE_Z))) {
                final String newInsCodeFieldString = Character.toString((char) insCodeInt);
                final String updatedMmcifRecord = updateMmcifRecord(mmcifRecord, insCodeField,
                        newInsCodeFieldString);
                updateFieldsDistribution(fieldLabels, fieldsDistribution, INS_CODE_FIELD_NAME,
                        insCodeFieldString, newInsCodeFieldString);
                return updatedMmcifRecord;
            }
        }
        return mmcifRecord;
    }

    private static final void updateFieldsDistribution(final List<String> fieldLabels,
            final List<MmcifUtils.Field> fieldsDistribution, final String fieldName,
            final String previousFieldString, final String nextFieldString) {
        final int previousFieldStringLength = StringUtils.length(previousFieldString);
        final int nextFieldStringLength = StringUtils.length(nextFieldString);
        final int lengthDifference = nextFieldStringLength - previousFieldStringLength;
        if (lengthDifference != 0) {
            final int insCodeFieldIndex = fieldLabels.indexOf(fieldName);
            final int fieldsNo = CollectionUtils.size(fieldLabels);
            for (int fieldIndex = insCodeFieldIndex + 1; fieldIndex < fieldsNo; fieldIndex++) {
                final MmcifUtils.Field currentField = fieldsDistribution.get(fieldIndex);
                currentField.move(lengthDifference);
            }
        }
    }

    private static final String updateMmcifRecord(final String mmcifRecord, final MmcifUtils.Field field,
            final String fieldString) {
        PreconditionUtils.checkIfStringIsBlank(mmcifRecord, MMCIF_RECORD_LABEL);
        Preconditions.checkNotNull(field, "There is no a particular field");
        PreconditionUtils.checkIfStringIsBlank(fieldString, "Expected field value");
        return new StringBuilder(StringUtils.substring(mmcifRecord, 0, field.getStartIndex()))
                .append(fieldString).append(StringUtils.substring(mmcifRecord, field.getEndIndex()))
                .toString();
    }
}

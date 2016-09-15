package edu.put.ma.io.reader;

import static edu.put.ma.utils.MmcifUtils.LABEL_ASYM_ID_FIELD_NAME;
import static edu.put.ma.utils.MmcifUtils.FIELD_NAMES_PREFIX;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.mmcif.MMcifParser;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.put.ma.io.model.ModelInfo;
import edu.put.ma.io.model.ModelInfoImpl;
import edu.put.ma.utils.MmcifUtils;
import edu.put.ma.utils.ResidueUtils;

public class MmcifReader extends CommonReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MmcifReader.class);

    private static final String PDB_MODEL_NUMBER_FIELD_NAME = "pdbx_PDB_model_num";

    private static final String LABEL_ENTITY_ID_FIELD_NAME = "label_entity_id";

    @Override
    protected Structure read(final InputStream inStream) {
        Structure structure = null;
        try {
            MMcifParser parser = new SimpleMMcifParser();
            SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();
            parser.addMMcifConsumer(consumer);
            parser.parse(inStream);
            structure = consumer.getStructure();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return structure;
    }

    @Override
    protected List<ModelInfo> readModelInfo(final List<String> mmcifRecords) {
        final List<ModelInfo> result = Lists.newArrayList();
        final List<String> fieldLabels = Lists.newArrayList();
        int previousModelNo = -1;
        String previousLabelAsymId = null;
        ModelInfo currentModelInfo = null;
        for (String mmcifRecord : mmcifRecords) {
            if (ResidueUtils.isCoordinatesRecord(mmcifRecord)) {
                final List<MmcifUtils.Field> fieldsDistribution = MmcifUtils
                        .computeFieldsDistribution(mmcifRecord);
                final MmcifUtils.Field modelNoField = MmcifUtils.getFieldByName(PDB_MODEL_NUMBER_FIELD_NAME,
                        fieldLabels, fieldsDistribution);
                final int modelNo = Integer.parseInt(MmcifUtils.getFieldString(mmcifRecord, modelNoField));
                if ((previousModelNo == -1) || (modelNo != previousModelNo)) {
                    addModelInfo(result, currentModelInfo);
                    previousLabelAsymId = null;
                    currentModelInfo = new ModelInfoImpl(modelNo);
                    previousModelNo = modelNo;
                }
                final MmcifUtils.Field labelAsymField = MmcifUtils.getFieldByName(LABEL_ASYM_ID_FIELD_NAME,
                        fieldLabels, fieldsDistribution);
                final String labelAsymIdString = MmcifUtils.getFieldString(mmcifRecord, labelAsymField);
                if ((StringUtils.isBlank(previousLabelAsymId))
                        || (!StringUtils.equals(previousLabelAsymId, labelAsymIdString))) {
                    final MmcifUtils.Field labelEntityIdField = MmcifUtils.getFieldByName(
                            LABEL_ENTITY_ID_FIELD_NAME, fieldLabels, fieldsDistribution);
                    final int labelEntityId = Integer.parseInt(MmcifUtils.getFieldString(mmcifRecord,
                            labelEntityIdField));
                    currentModelInfo.addEntityId(labelEntityId);
                    previousLabelAsymId = labelAsymIdString;
                }
            } else if (StringUtils.startsWith(mmcifRecord, FIELD_NAMES_PREFIX)) {
                MmcifUtils.addFieldLabel(fieldLabels, mmcifRecord);
            }
        }
        addModelInfo(result, currentModelInfo);
        return result;
    }

    private static final void addModelInfo(final List<ModelInfo> result, final ModelInfo currentModelInfo) {
        if (currentModelInfo != null) {
            result.add(currentModelInfo);
        }
    }
}

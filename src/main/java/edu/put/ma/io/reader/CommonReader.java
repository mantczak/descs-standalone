package edu.put.ma.io.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.biojava.nbio.structure.DBRef;
import org.biojava.nbio.structure.SSBond;
import org.biojava.nbio.structure.Site;
import org.biojava.nbio.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import edu.put.ma.archiver.Archiver;
import edu.put.ma.archiver.ArchiverFactory;
import edu.put.ma.io.model.ModelInfo;
import edu.put.ma.io.model.PdbBundle;
import edu.put.ma.io.model.PdbBundleImpl;
import edu.put.ma.io.model.Structure3d;
import edu.put.ma.io.model.Structure3dFactory;
import edu.put.ma.utils.PreconditionUtils;

public abstract class CommonReader implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonReader.class);

    @Override
    public Structure3d read(final String inputFilePath) {
        final File file = getFile(inputFilePath);
        final Structure3d structure3d = (ArchiverFactory.isArchive(file)) ? processBundleArchive(file)
                : processSingleFile(file);
        filterAtomicDataAndHeaderOnly(structure3d.getRawStructure());
        return structure3d;
    }

    protected abstract Structure read(InputStream inStream);

    protected abstract List<ModelInfo> readModelInfo(List<String> records);

    private Structure3d processBundleArchive(final File bundleArchive) {
        Structure3d structure3d = null;
        final File tempDir = Files.createTempDir();
        try {
            final Archiver archiver = ArchiverFactory.getArchiver(bundleArchive);
            archiver.extract(bundleArchive, tempDir);
            final PdbBundle bundle = new PdbBundleImpl(this, tempDir);
            final Structure structure = bundle.getStructure();
            PdbBundleImpl.renumerateAtoms(structure);
            structure3d = Structure3dFactory.construct(structure, bundle.getModelInfos());
        } finally {
            edu.put.ma.utils.FileUtils.deleteDirectory(tempDir);
        }
        return structure3d;
    }

    private Structure3d processSingleFile(final File file) {
        final Structure structure = readRawStructure(file);
        final List<ModelInfo> modelInfos = readModelInfo(file);
        Preconditions.checkNotNull(
                structure,
                String.format("Input 3D Structure [%s] is invalid",
                        FilenameUtils.getBaseName(file.getAbsolutePath())));
        PreconditionUtils.checkIfListIsEmpty(modelInfos, "Models information cannot be empty");
        return Structure3dFactory.construct(structure, modelInfos);
    }

    private Structure readRawStructure(final File file) {
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            return read(inStream);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inStream);
        }
        throw new IllegalArgumentException(String.format("Input file [%s] not found", file.getAbsolutePath()));
    }

    private List<ModelInfo> readModelInfo(final File inputFile) {
        try {
            final List<String> records = FileUtils.readLines(inputFile);
            return readModelInfo(records);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        throw new IllegalArgumentException(String.format("Inappropriate format of input file [%s]",
                inputFile.getAbsolutePath()));
    }

    private static final File getFile(final String inputFilePath) {
        PreconditionUtils.checkIfStringIsBlank(inputFilePath, "Input file path");
        final File file = FileUtils.getFile(inputFilePath);
        PreconditionUtils.checkIfFileExistsAndIsNotADirectory(file, "Input");
        return file;
    }

    private static final void filterAtomicDataAndHeaderOnly(final Structure structure) {
        Preconditions.checkNotNull(structure, "Structure is not initialized properly");
        if (!CollectionUtils.sizeIsEmpty(structure.getConnections())) {
            final List<Map<String, Integer>> connections = edu.put.ma.utils.CollectionUtils.emptyList();
            structure.setConnections(connections);
        }
        if (!CollectionUtils.sizeIsEmpty(structure.getDBRefs())) {
            final List<DBRef> dbrefs = edu.put.ma.utils.CollectionUtils.emptyList();
            structure.setDBRefs(dbrefs);
        }
        if (!CollectionUtils.sizeIsEmpty(structure.getSites())) {
            final List<Site> sites = edu.put.ma.utils.CollectionUtils.emptyList();
            structure.setSites(sites);
        }
        if (!CollectionUtils.sizeIsEmpty(structure.getSSBonds())) {
            final List<SSBond> ssbonds = edu.put.ma.utils.CollectionUtils.emptyList();
            structure.setSSBonds(ssbonds);
        }
    }
}

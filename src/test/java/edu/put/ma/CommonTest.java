package edu.put.ma;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class CommonTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected App app;

    @Before
    public void setUp() {
        app = new App();
    }

    protected static final File getFile(final Class<?> clazz, final String... options) {
        final StringBuilder path = new StringBuilder();
        final int optionsCount = ArrayUtils.getLength(options);
        for (int optionIndex = 0; optionIndex < optionsCount; optionIndex++) {
            path.append(options[optionIndex]);
            if (optionIndex < optionsCount - 1) {
                path.append("/");
            }
        }
        return FileUtils.toFile(clazz.getResource(path.toString()));
    }

}

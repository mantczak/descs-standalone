package edu.put.ma.descs;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import edu.put.ma.descs.DescriptorsBuilderImpl;

public class DescriptorBuilderTest {
    
    @Test
    public void testFindAndRemoveRedundantElementCenters() throws Exception {
        final List<Integer> elementCenters = new ArrayList<Integer>(Arrays.asList(new Integer[] { 15, 17, 19,
                20, 21, 23 }));
        final int originResidueIndex = 20;
        final int elementSize = 5;
        final List<Integer> expectedResult = new ArrayList<Integer>(
                Arrays.asList(new Integer[] { 15, 20, 23 }));
        DescriptorsBuilderImpl.removeRedundantElementCenterIndexes(originResidueIndex, elementCenters,
                elementSize);
        assertTrue(expectedResult.equals(elementCenters));
    }
}

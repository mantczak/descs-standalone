package edu.put.ma.descs.algorithms;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class HungarianMethodTest {

    @Test
    public void testHungarianMethod() throws Exception {
        final int[][] expectedResult = new int[][] { { 2, 0 }, { 1, 1 }, { 0, 2 }, { 3, 3 } };
        final double[][] costs = new double[][] { { 82.0, 83.0, 69.0, 92.0 }, { 77.0, 37.0, 49.0, 92.0 },
                { 11.0, 69.0, 5.0, 86.0 }, { 8.0, 9.0, 98.0, 23.0 } };
        final HungarianMethod hungarianMethod = new HungarianMethodImpl();
        final int[][] result = hungarianMethod.execute(costs);
        assertArrayEquals(result, expectedResult);
    }
}

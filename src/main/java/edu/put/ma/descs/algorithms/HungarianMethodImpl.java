package edu.put.ma.descs.algorithms;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import edu.put.ma.utils.ArrayUtils;
import edu.put.ma.utils.PreconditionUtils;

/**
 * @see <a href="http://shawntoneil.com/index.php/items/code/java@Hungarian@Hungarian.java">An application of
 * the Hungarian method to solve the minimum cost assignment problem</a>
 */
public class HungarianMethodImpl implements HungarianMethod {

    private static final int EXPECTED_LOCATION_SIZE = 2;

    private int elementsNo;

    private boolean[][] primes;

    private boolean[][] stars;

    private boolean[] rowsCovered;

    private boolean[] colsCovered;

    private double[][] costs;

    @Override
    public int[][] execute(final double[][] costs) {
        init(costs);
        subtractRowColMins();
        findStars();
        resetCovered();
        coverStarredZeroCols();
        while (org.apache.commons.lang3.ArrayUtils.contains(colsCovered, false)) {
            int[] primedLocation = primeUncoveredZero();
            if (primedLocation[0] == -1) {
                minUncoveredRowsCols();
                primedLocation = primeUncoveredZero();
            }
            final int primedRowIndex = primedLocation[0];
            final int starColIndex = org.apache.commons.lang3.ArrayUtils.indexOf(stars[primedRowIndex], true);
            if (starColIndex != -1) {
                rowsCovered[primedRowIndex] = true;
                colsCovered[starColIndex] = false;
            } else {
                augmentPathStartingAtPrime(primedLocation);
                resetCovered();
                resetPrimes();
                coverStarredZeroCols();
            }
        }
        return starsToAssignments(elementsNo, stars);
    }

    private void init(final double[][] costs) {
        PreconditionUtils.checkEqualityOfMatrixBothDimensions(costs, "costs");
        elementsNo = org.apache.commons.lang3.ArrayUtils.getLength(costs);
        this.costs = ArrayUtils.clone(costs);
        primes = new boolean[elementsNo][elementsNo];
        stars = new boolean[elementsNo][elementsNo];
        rowsCovered = new boolean[elementsNo];
        colsCovered = new boolean[elementsNo];
    }

    private void resetPrimes() {
        for (int rowIndex = 0; rowIndex < elementsNo; rowIndex++) {
            Arrays.fill(primes[rowIndex], false);
        }
    }

    private void resetCovered() {
        Arrays.fill(rowsCovered, false);
        Arrays.fill(colsCovered, false);
    }

    private void findStars() {
        final boolean[] rowStars = new boolean[elementsNo];
        final boolean[] colStars = new boolean[elementsNo];
        for (int rowIndex = 0; rowIndex < elementsNo; rowIndex++) {
            for (int colIndex = 0; colIndex < elementsNo; colIndex++) {
                if ((Double.compare(costs[rowIndex][colIndex], 0.0) == 0) && !rowStars[rowIndex]
                        && !colStars[colIndex]) {
                    stars[rowIndex][colIndex] = true;
                    rowStars[rowIndex] = true;
                    colStars[colIndex] = true;
                    break;
                }
            }
        }
    }

    private double getMinUncoveredValue() {
        double minUncoveredValue = Double.MAX_VALUE;
        for (int rowIndex = 0; rowIndex < elementsNo; rowIndex++) {
            if (!rowsCovered[rowIndex]) {
                minUncoveredValue = Math.min(minUncoveredValue, getMinUncoveredValueInRow(rowIndex));
            }
        }
        return minUncoveredValue;
    }

    private double getMinUncoveredValueInRow(int rowIndex) {
        double minUncoveredValue = Double.MAX_VALUE;
        for (int colIndex = 0; colIndex < elementsNo; colIndex++) {
            if ((!colsCovered[colIndex])
                    && (Double.compare(costs[rowIndex][colIndex], minUncoveredValue) < 0)) {
                minUncoveredValue = costs[rowIndex][colIndex];
            }
        }
        return minUncoveredValue;
    }

    private void minUncoveredRowsCols() {
        final double minUncoveredValue = getMinUncoveredValue();
        updateCosts(elementsNo, rowsCovered, true, minUncoveredValue, costs);
        updateCosts(elementsNo, colsCovered, false, minUncoveredValue, costs);
    }

    private int[] primeUncoveredZero() {
        for (int rowIndex = 0; rowIndex < elementsNo; rowIndex++) {
            if (!rowsCovered[rowIndex]) {
                final int[] location = getRowLocation(rowIndex);
                if (!Arrays.equals(location, org.apache.commons.lang3.ArrayUtils.EMPTY_INT_ARRAY)) {
                    return location;
                }
            }
        }
        return new int[] { -1, -1 };
    }

    private int[] getRowLocation(int rowIndex) {
        for (int colIndex = 0; colIndex < elementsNo; colIndex++) {
            if ((!colsCovered[colIndex]) && (Double.compare(costs[rowIndex][colIndex], 0.0) == 0)) {
                primes[rowIndex][colIndex] = true;
                return new int[] { rowIndex, colIndex };
            }
        }
        return org.apache.commons.lang3.ArrayUtils.EMPTY_INT_ARRAY;
    }

    private void augmentPathStartingAtPrime(final int[] location) {
        final int twoTimesElementsNo = elementsNo << 1;
        final List<List<Integer>> primeLocations = Lists.newArrayListWithExpectedSize(twoTimesElementsNo);
        final List<List<Integer>> starLocations = Lists.newArrayListWithExpectedSize(twoTimesElementsNo);
        primeLocations.add(Ints.asList(location));
        int currentRowIndex = location[0];
        int currentColIndex = location[1];
        while (true) {
            int starRowIndex = findStarRowInCol(elementsNo, currentColIndex, stars);
            if (starRowIndex == -1) {
                break;
            }
            starLocations.add(Ints.asList(new int[] { starRowIndex, currentColIndex }));
            currentRowIndex = starRowIndex;
            int primeColIndex = org.apache.commons.lang3.ArrayUtils.indexOf(primes[currentRowIndex], true);
            primeLocations.add(Ints.asList(new int[] { currentRowIndex, primeColIndex }));
            currentColIndex = primeColIndex;
        }
        updateLocations(starLocations, false, stars);
        updateLocations(primeLocations, true, stars);
    }

    private void coverStarredZeroCols() {
        for (int colIndex = 0; colIndex < elementsNo; colIndex++) {
            colsCovered[colIndex] = false;
            for (int rowIndex = 0; rowIndex < elementsNo; rowIndex++) {
                if (stars[rowIndex][colIndex]) {
                    colsCovered[colIndex] = true;
                    break;
                }
            }
        }
    }

    private void subtractRowColMins() {
        subtractMinFromCosts(elementsNo, costs, true);
        subtractMinFromCosts(elementsNo, costs, false);
    }

    private static final int[][] starsToAssignments(final int elementsNo, final boolean[][] stars) {
        final int[][] toRet = new int[elementsNo][];
        for (int colIndex = 0; colIndex < elementsNo; colIndex++) {
            toRet[colIndex] = new int[] { findStarRowInCol(elementsNo, colIndex, stars), colIndex };
        }
        return toRet;
    }

    private static final int findStarRowInCol(final int elementsNo, final int colIndex,
            final boolean[][] stars) {
        for (int rowIndex = 0; rowIndex < elementsNo; rowIndex++) {
            if (stars[rowIndex][colIndex]) {
                return rowIndex;
            }
        }
        return -1;
    }

    private static final void updateLocations(final List<List<Integer>> locations, final boolean value,
            final boolean[][] stars) {
        for (List<Integer> location : locations) {
            final int locationSize = CollectionUtils.size(location);
            if (locationSize == EXPECTED_LOCATION_SIZE) {
                final int rowIndex = location.get(0).intValue();
                final int colIndex = location.get(1).intValue();
                stars[rowIndex][colIndex] = value;
            }
        }
    }

    private static final void updateCosts(final int elementsNo, final boolean[] covered,
            final boolean expectedValue, final double minUncoveredValue, final double[][] costs) {
        for (int rowIndex = 0; rowIndex < elementsNo; rowIndex++) {
            if (covered[rowIndex] == expectedValue) {
                updateRowCosts(elementsNo, expectedValue, minUncoveredValue, costs, rowIndex);
            }
        }
    }

    private static final void updateRowCosts(final int elementsNo, final boolean expectedValue,
            final double minUncoveredValue, final double[][] costs, final int rowIndex) {
        for (int colIndex = 0; colIndex < elementsNo; colIndex++) {
            if (expectedValue) {
                costs[rowIndex][colIndex] = costs[rowIndex][colIndex] + minUncoveredValue;
            } else {
                costs[colIndex][rowIndex] = costs[colIndex][rowIndex] - minUncoveredValue;
            }
        }
    }

    private static final void subtractMinFromCosts(final int elementsNo, final double[][] costs,
            final boolean isRow) {
        for (int firstDimensionIndex = 0; firstDimensionIndex < elementsNo; firstDimensionIndex++) {
            final double min = getMin(elementsNo, costs, firstDimensionIndex, isRow);
            for (int secondDimensionIndex = 0; secondDimensionIndex < elementsNo; secondDimensionIndex++) {
                if (isRow) {
                    costs[firstDimensionIndex][secondDimensionIndex] = costs[firstDimensionIndex][secondDimensionIndex]
                            - min;
                } else {
                    costs[secondDimensionIndex][firstDimensionIndex] = costs[secondDimensionIndex][firstDimensionIndex]
                            - min;
                }
            }
        }
    }

    private static final double getMin(final int elementsNo, final double[][] costs,
            final int firstDimensionIndex, final boolean isRow) {
        double min = Double.MAX_VALUE;
        for (int secondDimensionIndex = 0; secondDimensionIndex < elementsNo; secondDimensionIndex++) {
            if (isRow) {
                if (costs[firstDimensionIndex][secondDimensionIndex] < min) {
                    min = costs[firstDimensionIndex][secondDimensionIndex];
                }
            } else {
                if (costs[secondDimensionIndex][firstDimensionIndex] < min) {
                    min = costs[secondDimensionIndex][firstDimensionIndex];
                }
            }
        }
        return min;
    }
}

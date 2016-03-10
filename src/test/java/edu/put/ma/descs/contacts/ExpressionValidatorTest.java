package edu.put.ma.descs.contacts;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.put.ma.model.MoleculeType;

public class ExpressionValidatorTest {
    @Test
    public void testInproperExpressionFormat1() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("2.0",
                MoleculeType.PROTEIN);
        assertFalse(expressionValidator.isValid());
    }

    @Test
    public void testInproperExpressionFormat2() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("AND(2.0, 2.0)",
                MoleculeType.PROTEIN);
        assertFalse(expressionValidator.isValid());
    }

    @Test
    public void testInproperExpressionFormat3() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("OR(DIST:AB, 2.0)",
                MoleculeType.PROTEIN);
        assertFalse(expressionValidator.isValid());
    }

    @Test
    public void testInproperExpressionFormat4() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("NOT(DIST:CA;AB)",
                MoleculeType.PROTEIN);
        assertFalse(expressionValidator.isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInproperExpressionFormat5() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("DIST:CA;CA<6",
                MoleculeType.RNA);
    }

    @Test
    public void testInproperExpressionFormat6() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("(2.0)",
                MoleculeType.PROTEIN);
        assertFalse(expressionValidator.isValid());
    }

    @Test
    public void testInproperExpressionFormat7() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("(DIST:CA;CA)",
                MoleculeType.PROTEIN);
        assertFalse(expressionValidator.isValid());
    }

    @Test
    public void testInproperExpressionFormat8() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("DISTANCE:C1'<=6",
                MoleculeType.PROTEIN);
        assertFalse(expressionValidator.isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInproperExpressionFormat9() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("DISTANCE:CQ>6",
                MoleculeType.PROTEIN);
    }

    @Test
    public void testInproperExpressionFormat10() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "NOT(DIST:CA;CA,DIST:CB;CB)", MoleculeType.PROTEIN);
        assertFalse(expressionValidator.isValid());
    }

    @Test
    public void testInproperExpressionFormat11() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("AND(DIST:CA;CA)",
                MoleculeType.PROTEIN);
        assertFalse(expressionValidator.isValid());
    }

    @Test
    public void testInproperExpressionFormat12() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("OR(DISTANCE:CB)",
                MoleculeType.PROTEIN);
        assertFalse(expressionValidator.isValid());
    }

    @Test
    public void testSimpleExpression1() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("DIST:CA;CA<6",
                MoleculeType.PROTEIN);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testSimpleExpression2() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("DISTANCE:C1'>=7.0",
                MoleculeType.RNA);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testSimpleExpression3() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl("5=6",
                MoleculeType.PROTEIN);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression1() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "AND(DISTANCE:C1'<7.0, DIST:BSGC<=8.0)", MoleculeType.RNA);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression2() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "NOT(DISTANCE:C1';O5'<7.0)", MoleculeType.RNA);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression3() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "AND(NOT(DISTANCE:C1';O5'<7.0), (DISTANCE:C1';O5'<7.0))", MoleculeType.RNA);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression4() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "AND(AND(NOT(DISTANCE:C1';O5'<7.0), (DISTANCE:C1';O5'<7.0)), AND(DISTANCE:C1';O5'<7.0, NOT(DISTANCE:C1';O5'<7.0)))",
                MoleculeType.RNA);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression5() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "AND(AND(NOT(DISTANCE:C1';O5'<7.0), (DISTANCE:C1';O5'<7.0)), AND((DISTANCE:C1';O5'<7.0), NOT(DISTANCE:C1';O5'<7.0)))",
                MoleculeType.RNA);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression6() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "NOT(OR((DISTANCE:SCGC <= 3.5), AND((DIST:SCGC;SCGC <= DISTANCE:CA,CA - 1.5 + DISTANCE:CB,CB); (DISTANCE-SCGC;SCGC <= 5.0))))",
                MoleculeType.PROTEIN);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression7() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "NOT(OR((DISTANCE:SCGC <= 3.5), AND((DIST:SCGC;SCGC <= DISTANCE:CA,CA - (1.5 + DISTANCE:CB,CB)); (DISTANCE-SCGC;SCGC <= 5.0))))",
                MoleculeType.PROTEIN);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression8() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "NOT(OR((DISTANCE:SCGC <= 3.5), AND((DIST:SCGC;SCGC <= (DISTANCE:CA,CA * 2) - (1.5 + DISTANCE:CB,CB)); (DISTANCE-SCGC;SCGC <= 5.0))))",
                MoleculeType.PROTEIN);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression9() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "NOT(OR((DISTANCE:SCGC <= 3.5), AND((DIST:SCGC;SCGC <= (DISTANCE:CA,CA * (2)) - (1.5 + (DISTANCE:CB,CB))); (DISTANCE-SCGC;SCGC <= 5.0))))",
                MoleculeType.PROTEIN);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression10() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "NOT(OR((DISTANCE:SCGC <= 3.5), AND((DIST:SCGC;SCGC <= ((DISTANCE:CA,CA) * (2)) - ((1.5) + (DISTANCE:CB,CB))); (DISTANCE-SCGC;SCGC <= 5.0))))",
                MoleculeType.PROTEIN);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression11() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "(NOT(DISTANCE:C1';O5'<7.0))", MoleculeType.RNA);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression12() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "NOT((OR((DISTANCE:SCGC <= 3.5), AND((DIST:SCGC;SCGC <= ((DISTANCE:CA,CA) * (2)) - ((1.5) + (DISTANCE:CB,CB))); (DISTANCE-SCGC;SCGC <= 5.0)))))",
                MoleculeType.PROTEIN);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression13() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "AND((NOT(DISTANCE:C1';O5'<(5.0))), (OR(DISTANCE:C1';O5'<6.0, (DIST:BSGC<9.0))))",
                MoleculeType.RNA);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression14() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "AND((NOT((DISTANCE:C1';O5')<(5))), (OR(((DISTANCE:C1';O5')<(6)), ((DIST:BSGC)<(9)))))",
                MoleculeType.RNA);
        assertTrue(expressionValidator.isValid());
    }

    @Test
    public void testComplexExpression15() throws Exception {
        final ExpressionValidator expressionValidator = new ExpressionValidatorImpl(
                "OR(DISTANCE:BSGC <= 10, OR(AND(DISTANCE:BSGC <= DISTANCE:RBGC + 3.5, DISTANCE:RBGC <= 8), AND(DISTANCE:RBGC <= DISTANCE:BBGC + 5, DISTANCE:BBGC <= 7.0)))",
                MoleculeType.RNA);
        assertTrue(expressionValidator.isValid());
    }
}

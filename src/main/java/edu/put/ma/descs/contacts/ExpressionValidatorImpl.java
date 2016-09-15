package edu.put.ma.descs.contacts;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import edu.put.ma.model.AtomNamesPair;
import edu.put.ma.model.AtomNamesPairImpl;
import edu.put.ma.model.MoleculeType;
import edu.put.ma.utils.CollectionUtils;
import edu.put.ma.model.ResiduesDictionary;

public class ExpressionValidatorImpl implements ExpressionValidator {

    private static final int SECOND_COMPONENT_GROUP_INDEX = 2;

    private static final int FIRST_COMPONENT_GROUP_INDEX = 1;

    private static final int ALPHABET_SIZE = 26;

    @Getter
    private String inContactResiduesExpressionString;

    @Getter
    private boolean valid;

    private Map<String, String> components;

    private List<String> atomNames;

    private Map<String, AtomNamesPair> distances;

    public ExpressionValidatorImpl(final String inContactResiduesExpressionString,
            final MoleculeType moleculeType) {
        setInContactResiduesExpressionString(inContactResiduesExpressionString, moleculeType);
    }

    @Override
    public void setInContactResiduesExpressionString(final String inContactResiduesExpressionString,
            final MoleculeType moleculeType) {
        String unifiedInContactResiduesExpressionString = unify(
                StringUtils.deleteWhitespace(StringUtils.upperCase(inContactResiduesExpressionString)),
                moleculeType);
        this.valid = isSyntaxValid(unifiedInContactResiduesExpressionString, moleculeType)
                && ResiduesDictionary.considersAtom(atomNames, moleculeType);
        unifiedInContactResiduesExpressionString = unifyAndOrFunctions(unifiedInContactResiduesExpressionString);
        this.inContactResiduesExpressionString = getExpression(unifiedInContactResiduesExpressionString,
                moleculeType);
        initDistances(moleculeType);
    }

    @Override
    public String getAtomNamePairsString() {
        final int atomNamePairsCount = org.apache.commons.collections4.CollectionUtils.size(distances
                .values());
        final StringBuilder sb = new StringBuilder();
        int pairIndex = 0;
        for (AtomNamesPair pair : distances.values()) {
            sb.append(pair.toString());
            if (pairIndex++ < atomNamePairsCount - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public Set<String> getVariableNames() {
        return Collections.unmodifiableSet(components.keySet());
    }

    @Override
    public AtomNamesPair getDistance(final String variableName) {
        if (!distances.containsKey(variableName)) {
            throw new IllegalArgumentException(String.format("Unknown variable name %s", variableName));
        }
        return distances.get(variableName);
    }

    public static final Pattern getAtomNamesPattern(final MoleculeType moleculeType) {
        return getAtomNamesPattern(moleculeType, Patterns.PROTEIN_ATOM_NAMES_PATTERN,
                Patterns.RNA_ATOM_NAMES_PATTERN);
    }

    public static final Pattern getVirtualAtomNamesPattern(final MoleculeType moleculeType) {
        return getAtomNamesPattern(moleculeType, Patterns.PROTEIN_VIRTUAL_ATOM_NAMES_PATTERN,
                Patterns.RNA_VIRTUAL_ATOM_NAMES_PATTERN);
    }

    private void prepareComponents() {
        components = CollectionUtils.prepareMap(components);
    }

    private void prepareAtomNames() {
        atomNames = CollectionUtils.prepareList(atomNames);
    }

    private void prepareDistances() {
        distances = CollectionUtils.prepareMap(distances);
    }

    private void initDistances(final MoleculeType moleculeType) {
        prepareDistances();
        final Pattern distancePattern = (moleculeType == MoleculeType.PROTEIN) ? Patterns.UNIFIED_PROTEIN_DISTANCE_PATTERN
                : Patterns.UNIFIED_RNA_DISTANCE_PATTERN;
        for (Map.Entry<String, String> entry : components.entrySet()) {
            final Matcher distanceMatcher = distancePattern.matcher(entry.getValue());
            if (distanceMatcher.matches()) {
                addDistance(entry.getKey(), distanceMatcher.group(FIRST_COMPONENT_GROUP_INDEX),
                        distanceMatcher.group(SECOND_COMPONENT_GROUP_INDEX));
            }
        }
    }

    private String getExpression(final String inContactResiduesExpressionString,
            final MoleculeType moleculeType) {
        prepareComponents();
        final Pattern distancePattern = (moleculeType == MoleculeType.PROTEIN) ? Patterns.UNIFIED_PROTEIN_DISTANCE_PATTERN
                : Patterns.UNIFIED_RNA_DISTANCE_PATTERN;
        return generalAnalysis(inContactResiduesExpressionString, distancePattern, components,
                ComponentPrefixes.DISTANCES);
    }

    private String unify(final String inContactResiduesExpressionString, final MoleculeType moleculeType) {
        String unifiedInContactResiduesExpressionString = simpleReplacements(inContactResiduesExpressionString);
        unifiedInContactResiduesExpressionString = unifyDistances(moleculeType,
                unifiedInContactResiduesExpressionString);
        return unifiedInContactResiduesExpressionString;
    }

    private String simpleReplacements(final String inContactResiduesExpressionString) {
        String unifiedInContactResiduesExpressionString = inContactResiduesExpressionString;
        unifiedInContactResiduesExpressionString = StringUtils.replace(
                unifiedInContactResiduesExpressionString, "==", "=");
        unifiedInContactResiduesExpressionString = StringUtils.replace(
                unifiedInContactResiduesExpressionString, "=<", "<=");
        unifiedInContactResiduesExpressionString = StringUtils.replace(
                unifiedInContactResiduesExpressionString, "=>", ">=");
        return unifiedInContactResiduesExpressionString;
    }

    private void addAtomName(final String atomName, final MoleculeType moleculeType) {
        final Pattern virtualAtomNamesPattern = getVirtualAtomNamesPattern(moleculeType);
        final Matcher virtualAtomNamesMatcher = virtualAtomNamesPattern.matcher(atomName);
        if ((!virtualAtomNamesMatcher.matches()) && (!atomNames.contains(atomName))) {
            atomNames.add(atomName);
        }
    }

    private void addDistance(final String variableName, final String firstComponent,
            final String secondComponent) {
        if (!distances.containsKey(variableName)) {
            distances.put(variableName, new AtomNamesPairImpl(firstComponent, secondComponent));
        }
    }

    private String unifyDistances(final MoleculeType moleculeType,
            final String unifiedInContactResiduesExpressionString) {
        prepareAtomNames();
        final Pattern distancePattern = (moleculeType == MoleculeType.PROTEIN) ? Patterns.GENERAL_PROTEIN_DISTANCE_PATTERN
                : Patterns.GENERAL_RNA_DISTANCE_PATTERN;
        int startIndex = 0;
        final Matcher distanceMatcher = distancePattern.matcher(unifiedInContactResiduesExpressionString);
        final StringBuilder updatedInContactResiduesExpressionStringBuilder = new StringBuilder();
        boolean atLeastOneExchange = false;
        while (distanceMatcher.find()) {
            final int groupsCount = distanceMatcher.groupCount();
            if (!((Patterns.DISTANCE_PREFIX.equals(distanceMatcher.group(1)))
                    && (":".equals(distanceMatcher.group(2))) && ("-".equals(distanceMatcher.group(4))))) {
                final String unifiedDistanceString = getUnifiedDistanceString(moleculeType, distanceMatcher,
                        groupsCount);
                if (StringUtils.isNotBlank(unifiedDistanceString)) {
                    final String prefix = StringUtils.substring(unifiedInContactResiduesExpressionString,
                            startIndex, distanceMatcher.start());
                    updatedInContactResiduesExpressionStringBuilder.append(prefix).append(
                            unifiedDistanceString);
                    atLeastOneExchange = true;
                }
            }
            startIndex = distanceMatcher.end();
        }
        if (atLeastOneExchange) {
            final String postfix = StringUtils.substring(unifiedInContactResiduesExpressionString,
                    startIndex, StringUtils.length(unifiedInContactResiduesExpressionString));
            updatedInContactResiduesExpressionStringBuilder.append(postfix);
            return updatedInContactResiduesExpressionStringBuilder.toString();
        }
        return unifiedInContactResiduesExpressionString;
    }

    private String getUnifiedDistanceString(final MoleculeType moleculeType, final Matcher distanceMatcher,
            final int groupsCount) {
        String unifiedDistanceString = null;
        if (StringUtils.isNotBlank(distanceMatcher.group(groupsCount))) {
            final String firstComponent = distanceMatcher.group(groupsCount - 3);
            final String secondComponent = distanceMatcher.group(groupsCount);
            unifiedDistanceString = new StringBuilder(Patterns.DISTANCE_PREFIX).append(":")
                    .append(firstComponent).append(";").append(secondComponent).toString();
            addAtomName(firstComponent, moleculeType);
            addAtomName(secondComponent, moleculeType);
        } else if (StringUtils.isEmpty(distanceMatcher.group(groupsCount - 2))) {
            final String component = distanceMatcher.group(groupsCount - 3);
            unifiedDistanceString = new StringBuilder(Patterns.DISTANCE_PREFIX).append(":").append(component)
                    .append(";").append(component).toString();
            addAtomName(component, moleculeType);
        }
        return unifiedDistanceString;
    }

    private String unifyAndOrFunctions(final String inContactResiduesExpressionString) {
        String unifiedInContactResiduesExpressionString = new String(inContactResiduesExpressionString);
        final Pattern logicalFunctionsKeyPattern = Pattern.compile("U[A-Z]+");
        for (Map.Entry<String, String> entry : components.entrySet()) {
            Matcher keyMatcher = logicalFunctionsKeyPattern.matcher(entry.getKey());
            if (keyMatcher.matches()) {
                Matcher valueMatcher = Patterns.AND_OR_PATTERN.matcher(entry.getValue());
                if ((valueMatcher.matches()) && (entry.getValue().indexOf(";") >= 0)) {
                    final String[] result = expandFunction(entry.getValue());
                    unifiedInContactResiduesExpressionString = StringUtils.replace(
                            unifiedInContactResiduesExpressionString, result[0], result[1]);
                }
            }
        }
        return unifiedInContactResiduesExpressionString;
    }

    private String[] expandFunction(final String andOrFunctionString) {
        final String[] result = new String[] { new String(andOrFunctionString),
                new String(StringUtils.replace(andOrFunctionString, ";", ",")) };
        Matcher keysMatcher = Patterns.COMPONENT_KEYS_PATTERN.matcher(result[0]);
        while (keysMatcher.find()) {
            final String componentKey = keysMatcher.group();
            if (components.containsKey(componentKey)) {
                for (int i = 0; i < ArrayUtils.getLength(result); i++) {
                    result[i] = StringUtils.replace(result[i], componentKey, components.get(componentKey));
                }
                keysMatcher = Patterns.COMPONENT_KEYS_PATTERN.matcher(result[0]);
            }
        }
        return result;
    }

    private boolean isSyntaxValid(final String inContactResiduesExpressionString,
            final MoleculeType moleculeType) {
        prepareComponents();
        final Pattern distancePattern = (moleculeType == MoleculeType.PROTEIN) ? Patterns.UNIFIED_PROTEIN_DISTANCE_PATTERN
                : Patterns.UNIFIED_RNA_DISTANCE_PATTERN;
        String inputString = generalAnalysis(inContactResiduesExpressionString, distancePattern, components,
                ComponentPrefixes.DISTANCES);
        inputString = generalAnalysis(inputString, Patterns.NUMBER_PATTERN, components,
                ComponentPrefixes.NUMBERS);
        inputString = generalAnalysis(inputString, Patterns.COMPLEX_EXPRESSION_PATTERN, components,
                ComponentPrefixes.COMPLEX_EXPRESSIONS);
        inputString = generalAnalysis(inputString, Patterns.COMPARISON_PATTERN, components,
                ComponentPrefixes.COMPARISONS);
        inputString = analyseLogicalFuctions(inputString, components);
        return Pattern.compile("W[A-Z]+|V[A-Z]+|U[A-Z]+").matcher(inputString).matches();
    }

    private static final Pattern getAtomNamesPattern(final MoleculeType moleculeType,
            final Pattern proteinPattern, final Pattern rnaPattern) {
        return (moleculeType == MoleculeType.PROTEIN) ? proteinPattern : rnaPattern;
    }

    private static final String getAsciiId(final int count) {
        final StringBuilder result = new StringBuilder();
        int currentCount = count;
        while (currentCount >= ALPHABET_SIZE) {
            final int rest = currentCount % ALPHABET_SIZE;
            result.insert(0, (char) ('A' + rest));
            currentCount /= ALPHABET_SIZE;
        }
        result.insert(0, (char) ('A' + currentCount));
        return result.toString();
    }

    private static final String updateStringOnce(final String input, final int start, final int end,
            final String replacement) {
        final StringBuilder updatedString = new StringBuilder();
        updatedString.append(StringUtils.substring(input, 0, start)).append(replacement)
                .append(StringUtils.substring(input, end));
        return updatedString.toString();
    }

    private static final String introduceOccurence(final String inputString,
            final Map<String, String> components, final char singleLetterCode, final String patternString,
            final int count) {
        final String currentKey = singleLetterCode + getAsciiId(count);
        components.put(currentKey, patternString);
        return StringUtils.replace(inputString, patternString, currentKey);
    }

    private static final String generalAnalysis(final String inputString, final Pattern pattern,
            final Map<String, String> components, final ComponentPrefixes keyPrefix) {
        final Pattern skipBracketsPattern = Pattern.compile(new StringBuilder("\\((")
                .append(keyPrefix.getSingleLetterCode()).append("[A-Z]+)\\)").toString());
        String localInputString = new String(inputString);
        Matcher matcher = pattern.matcher(localInputString);
        Matcher skipBracketsMatcher = skipBracketsPattern.matcher(localInputString);
        int count = 0;
        boolean found = true;
        while (found) {
            found = false;
            if (matcher.find()) {
                found = true;
                localInputString = introduceOccurence(localInputString, components,
                        keyPrefix.getSingleLetterCode(), matcher.group(), count);
                count++;
            } else if (skipBracketsMatcher.find()) {
                found = true;
                if (occurenceCanBeIgnored(localInputString, skipBracketsMatcher)) {
                    continue;
                }
                final String skipBracketsString = skipBracketsMatcher.group();
                final int bracketsStartIndex = skipBracketsMatcher.start();
                localInputString = updateStringOnce(localInputString, bracketsStartIndex,
                        skipBracketsMatcher.end(), StringUtils.substring(skipBracketsString, 1,
                                StringUtils.length(skipBracketsString) - 1));
            }
            if (found) {
                matcher = pattern.matcher(localInputString);
                skipBracketsMatcher = skipBracketsPattern.matcher(localInputString);
            }
        }
        return localInputString;
    }

    private static final boolean occurenceCanBeIgnored(final String inputString,
            final Matcher skipBracketsMatcher) {
        final int bracketsStartIndex = skipBracketsMatcher.start();
        final String prefix = StringUtils.substring(inputString, bracketsStartIndex - 2, bracketsStartIndex);
        return ("OR".equals(prefix)) || ("OT".equals(prefix));
    }

    private static final OperationsType setPatternStringAndReturnOperationType(final Matcher andOrMatcher,
            final Matcher notMatcher, final Matcher skipBracketsMatcher,
            final StringBuilder patternStringBuilder) {
        if (andOrMatcher.find()) {
            patternStringBuilder.append(andOrMatcher.group());
            return OperationsType.FUNCTION;
        } else if (notMatcher.find()) {
            patternStringBuilder.append(notMatcher.group());
            return OperationsType.FUNCTION;
        } else if (skipBracketsMatcher.find()) {
            patternStringBuilder.append(skipBracketsMatcher.group());
            return OperationsType.BRACKETS;
        }
        return OperationsType.NONE;
    }

    private static final String analyseLogicalFuctions(final String inputString,
            final Map<String, String> components) {
        final Pattern skipBracketsPattern = Pattern.compile("\\(("
                + ComponentPrefixes.LOGICAL_FUNCTIONS.getSingleLetterCode() + "[A-Z]+)\\)");
        String localInputString = new String(inputString);
        Matcher andOrMatcher = Patterns.AND_OR_PATTERN.matcher(localInputString);
        Matcher notMatcher = Patterns.NOT_PATTERN.matcher(localInputString);
        Matcher skipBracketsMatcher = skipBracketsPattern.matcher(localInputString);
        int count = 0;
        OperationsType currentOperationType = OperationsType.FUNCTION;
        while (currentOperationType != OperationsType.NONE) {
            final StringBuilder patternStringBuilder = new StringBuilder();
            currentOperationType = setPatternStringAndReturnOperationType(andOrMatcher, notMatcher,
                    skipBracketsMatcher, patternStringBuilder);
            final String patternString = patternStringBuilder.toString();
            if (OperationsType.FUNCTION == currentOperationType) {
                localInputString = introduceOccurence(localInputString, components,
                        ComponentPrefixes.LOGICAL_FUNCTIONS.getSingleLetterCode(), patternString, count);
                count++;
            } else if (OperationsType.BRACKETS == currentOperationType) {
                if (occurenceCanBeIgnored(localInputString, skipBracketsMatcher)) {
                    continue;
                }
                localInputString = updateStringOnce(localInputString, skipBracketsMatcher.start(),
                        skipBracketsMatcher.end(),
                        StringUtils.substring(patternString, 1, StringUtils.length(patternString) - 1));
            }
            if (currentOperationType != OperationsType.NONE) {
                andOrMatcher = Patterns.AND_OR_PATTERN.matcher(localInputString);
                notMatcher = Patterns.NOT_PATTERN.matcher(localInputString);
                skipBracketsMatcher = skipBracketsPattern.matcher(localInputString);
            }
        }
        return localInputString;
    }

    private enum OperationsType {
        NONE, FUNCTION, BRACKETS;
    }

    private static final class Patterns {

        private static final String DISTANCE_PREFIX = "DISTANCE";

        private static final Pattern GENERAL_PROTEIN_DISTANCE_PATTERN = Pattern
                .compile("(DISTANCE|DIST)([:-]|\\.)(BBGC|SCGC|CBX|VCB|BBC|SCC|[A-Z0-9]{1,3})(([;,]|\\.)(BBGC|SCGC|CBX|VCB|BBC|SCC|[A-Z0-9]{1,3}))?");

        private static final Pattern GENERAL_RNA_DISTANCE_PATTERN = Pattern
                .compile("(DISTANCE|DIST)([:-]|\\.)(BBGC|RBGC|BSGC|BBC|RBC|BSC|[A-Z0-9\'*`]{1,3})(([;,]|\\.)(BBGC|RBGC|BSGC|BBC|RBC|BSC|[A-Z0-9\'*`]{1,3}))?");

        private static final Pattern UNIFIED_PROTEIN_DISTANCE_PATTERN = Pattern
                .compile("DISTANCE:(BBGC|SCGC|CBX|VCB|BBC|SCC|[A-Z0-9]{1,3});(BBGC|SCGC|CBX|VCB|BBC|SCC|[A-Z0-9]{1,3})");

        private static final Pattern UNIFIED_RNA_DISTANCE_PATTERN = Pattern
                .compile("DISTANCE:(BBGC|RBGC|BSGC|BBC|RBC|BSC|[A-Z0-9\'*`]{1,3});(BBGC|RBGC|BSGC|BBC|RBC|BSC|[A-Z0-9\'*`]{1,3})");

        private static final Pattern PROTEIN_VIRTUAL_ATOM_NAMES_PATTERN = Pattern
                .compile("BBGC|SCGC|CBX|VCB|BBC|SCC");

        private static final Pattern RNA_VIRTUAL_ATOM_NAMES_PATTERN = Pattern
                .compile("BBGC|RBGC|BSGC|BBC|RBC|BSC");

        private static final Pattern PROTEIN_ATOM_NAMES_PATTERN = Pattern
                .compile("(BBGC|SCGC|CBX|VCB|BBC|SCC|[A-Z0-9]{1,3})");

        private static final Pattern RNA_ATOM_NAMES_PATTERN = Pattern
                .compile("(BBGC|RBGC|BSGC|BBC|RBC|BSC|[A-Z0-9\'*`]{1,3})");

        private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");

        private static final Pattern COMPLEX_EXPRESSION_PATTERN = Pattern
                .compile("(Z[A-Z]+|Y[A-Z]+|X[A-Z]+)(([-+*/%])(Z[A-Z]+|Y[A-Z]+|X[A-Z]+))+");

        private static final Pattern COMPARISON_PATTERN = Pattern
                .compile("(Z[A-Z]+|Y[A-Z]+|X[A-Z]+)(<=|>=|<|>|=)(Z[A-Z]+|Y[A-Z]+|X[A-Z]+)");

        private static final Pattern AND_OR_PATTERN = Pattern
                .compile("(AND|OR)\\((W[A-Z]+|V[A-Z]+|U[A-Z]+)([,;])(W[A-Z]+|V[A-Z]+|U[A-Z]+)\\)");

        private static final Pattern NOT_PATTERN = Pattern.compile("NOT\\((W[A-Z]+|V[A-Z]+|U[A-Z]+)\\)");

        private static final Pattern COMPONENT_KEYS_PATTERN = Pattern
                .compile("Z[A-Z]+|Y[A-Z]+|X[A-Z]+|W[A-Z]+|V[A-Z]+|U[A-Z]+");

        private Patterns() {
            // hidden constructor
        }
    }
}

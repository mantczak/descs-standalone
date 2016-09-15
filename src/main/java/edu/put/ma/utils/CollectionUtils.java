package edu.put.ma.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Arrays;

import org.biojava.nbio.structure.Group;

public final class CollectionUtils {

    private CollectionUtils() {
        // hidden constructor
    }

    public static final <E> List<E> prepareList(final List<E> list) {
        if (list == null) {
            return Lists.newArrayList();
        } else if (!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(list)) {
            list.clear();
        }
        return list;
    }

    public static final <K, V> Map<K, V> prepareMap(final Map<K, V> map) {
        if (map == null) {
            return Maps.newHashMap();
        } else if (!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(map)) {
            map.clear();
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static final <T> List<T> emptyList() {
        return (List<T>) Collections.emptyList();
    }

    public static final <K, V> Map<K, V> emptyMap() {
        return Collections.<K, V> emptyMap();
    }

    public static final <T> List<T> constructEmptyListWithCapacity(final List<T> list) {
        Preconditions.checkNotNull(list);
        return Lists.newArrayListWithCapacity(org.apache.commons.collections4.CollectionUtils.size(list));
    }

    public static final <T> T[] toArray(final List<T> list, final T[] array) {
        Preconditions.checkNotNull(list);
        Preconditions.checkNotNull(array);
        return list.toArray(array);
    }

    public static final <T> List<T> transformCollectionIntoList(final Collection<T> collection) {
        Preconditions.checkNotNull(collection);
        return Collections.list(Collections.enumeration(collection));
    }

    public static final <T> List<T> extendAndReturnAsNewObject(final List<T> currentElements,
            final List<T> newElements) {
        final int currentElementsCount = org.apache.commons.collections4.CollectionUtils
                .size(currentElements);
        final int newElementsCount = org.apache.commons.collections4.CollectionUtils.size(newElements);
        final List<T> extendedElements = Lists.newArrayListWithCapacity(currentElementsCount
                + newElementsCount);
        extend(extendedElements, currentElements);
        extend(extendedElements, newElements);
        return extendedElements;
    }

    public static final <T> void init(final List<T> currentElements, final List<T> newElements) {
        final List<T> elements = prepareList(currentElements);
        extend(elements, newElements);
    }

    public static final <T> void extend(final List<T> currentElements, final List<T> newElements) {
        if (!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(newElements)) {
            org.apache.commons.collections4.CollectionUtils.addAll(currentElements, newElements);
        }
    }

    public static final <T> void extendFromSpecificPosition(final List<T> currentElements,
            final List<T> newElements, final int position) {
        if (!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(newElements)) {
            final int currentElementsSize = org.apache.commons.collections4.CollectionUtils
                    .size(currentElements);
            if (position < currentElementsSize) {
                currentElements.addAll(position, newElements);
            } else if (position == currentElementsSize) {
                extend(currentElements, newElements);
            } else {
                PreconditionUtils.checkIfIndexInRange(position, 0,
                        org.apache.commons.collections4.CollectionUtils.size(currentElements),
                        "Specific position");
            }
        }
    }

    public static final <T> void setFromSpecificPosition(final List<T> currentElements,
            final List<T> newElements, final int position) {
        if (!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(newElements)) {
            PreconditionUtils.checkIfIndexInRange(position, 0,
                    org.apache.commons.collections4.CollectionUtils.size(currentElements),
                    "Specific position");
            final int newElementsSize = org.apache.commons.collections4.CollectionUtils.size(newElements);
            remove(currentElements, position, newElementsSize);
            extendFromSpecificPosition(currentElements, newElements, position);
        }
    }

    public static final <T> void remove(final List<T> currentElements, final int position, final int size) {
        if (org.apache.commons.collections4.CollectionUtils.size(currentElements) >= position + size) {
            for (int index = position + size - 1; index >= position; index--) {
                currentElements.remove(index);
            }
        }
    }

    public static final <T> void remove(final List<T> currentElements, final List<T> remove) {
        if ((!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(currentElements))
                && (!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(remove))) {
            final List<T> previousElements = (List<T>) org.apache.commons.collections4.CollectionUtils
                    .removeAll(currentElements, remove);
            currentElements.clear();
            extend(currentElements, previousElements);
        }
    }

    public static final <T> List<T> identifyNewElements(final List<T> currentElements,
            final List<T> newElements) {
        if ((!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(currentElements))
                && (!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(newElements))) {
            final Collection<T> residuesInIntersection = org.apache.commons.collections4.CollectionUtils
                    .intersection(currentElements, newElements);
            if (!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(residuesInIntersection)) {
                return transformCollectionIntoList(org.apache.commons.collections4.CollectionUtils.removeAll(
                        newElements, residuesInIntersection));
            }
            return newElements;
        }
        return Collections.emptyList();
    }

    public static final <T> boolean equalStringRepresentationsOfLists(final List<T> firstElements,
            final List<T> secondElements) {
        if ((!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(firstElements))
                && (!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(secondElements))) {
            return Arrays.toString(firstElements.toArray()).equals(Arrays.toString(secondElements.toArray()));
        }
        return false;
    }

    public static final <T extends Comparable<T>> List<Integer> sortAndReturnOrdering(final List<T> elements) {
        final List<Integer> ordering = Lists
                .newArrayListWithCapacity(org.apache.commons.collections4.CollectionUtils.size(elements));
        Collections.sort(elements, new Comparator<T>() {
            public int compare(final T o1, final T o2) {
                final int comparisonResult = o1.compareTo(o2);
                ordering.add(comparisonResult);
                return comparisonResult;
            }
        });
        return ordering;
    }

    public static final <T extends Comparable<T>> void sort(final List<T> elements) {
        Collections.sort(elements, new Comparator<T>() {
            public int compare(final T o1, final T o2) {
                return o1.compareTo(o2);
            }
        });
    }

    public static final void sortResidues(final List<Group> residues) {
        Collections.sort(residues, new Comparator<Group>() {
            public int compare(final Group o1, final Group o2) {
                return o1.getResidueNumber().compareTo(o2.getResidueNumber());
            }
        });
    }

    public static final <T> void sortAccordingToOrdering(final List<T> elements, final List<Integer> ordering) {
        Collections.sort(elements, new Comparator<T>() {
            private int index;

            @Override
            public int compare(final T o1, final T o2) {
                return ordering.get(index++);
            }
        });
    }
}

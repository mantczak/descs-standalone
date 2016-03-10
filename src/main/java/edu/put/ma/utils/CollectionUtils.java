package edu.put.ma.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
        if ((!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(currentElements))
                && (!org.apache.commons.collections4.CollectionUtils.sizeIsEmpty(newElements))) {
            PreconditionUtils.checkIfIndexInRange(position, 0,
                    org.apache.commons.collections4.CollectionUtils.size(currentElements),
                    "Specific position");
            currentElements.addAll(position, newElements);
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
}

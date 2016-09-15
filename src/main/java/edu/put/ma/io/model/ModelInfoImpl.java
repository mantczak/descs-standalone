package edu.put.ma.io.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.ImmutableSet;

@RequiredArgsConstructor
public class ModelInfoImpl implements ModelInfo {

    public static final int ENTITY_ID_NOT_KNOWN = Integer.MIN_VALUE;

    @Getter
    private final int modelNo;

    private List<Integer> entityIds;

    @Getter
    private boolean entityIdsConsidered;

    public ModelInfoImpl(final int modelNo, final List<Integer> entityIds) {
        this(modelNo);
        setEntityIds(entityIds);
    }

    @Override
    public void addEntityId(final int entityId) {
        if (entityIds == null) {
            entityIds = edu.put.ma.utils.CollectionUtils.prepareList(entityIds);
        }
        entityIds.add(entityId);
        setEntityIdsConsidered();
    }

    @Override
    public List<Integer> getEntityIds() {
        return Collections.unmodifiableList(entityIds);
    }

    @Override
    public int getEntityIdByStrandIndex(final int index) {
        if ((index >= 0) && (index < CollectionUtils.size(entityIds))) {
            return entityIds.get(index);
        }
        return ENTITY_ID_NOT_KNOWN;
    }

    @Override
    public void setEntityIds(final List<Integer> entityIds) {
        if (!CollectionUtils.sizeIsEmpty(entityIds)) {
            this.entityIds = edu.put.ma.utils.CollectionUtils.prepareList(this.entityIds);
            CollectionUtils.addAll(this.entityIds, entityIds);
            setEntityIdsConsidered();
        } else {
            this.entityIdsConsidered = false;
        }
    }

    private void setEntityIdsConsidered() {
        final Set<Integer> uniqueEntityIds = ImmutableSet.copyOf(this.entityIds);
        this.entityIdsConsidered = CollectionUtils.size(uniqueEntityIds) != 1;
    }

}

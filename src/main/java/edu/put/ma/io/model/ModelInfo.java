package edu.put.ma.io.model;

import java.util.List;

public interface ModelInfo {

    int getModelNo();

    void addEntityId(int entityId);

    List<Integer> getEntityIds();

    int getEntityIdByStrandIndex(int index);

    void setEntityIds(List<Integer> entityIds);

    boolean isEntityIdsConsidered();
}

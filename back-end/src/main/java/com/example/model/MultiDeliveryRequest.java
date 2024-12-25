package com.example.model;

import java.util.List;

public class MultiDeliveryRequest {
    private long warehouseId;
    private List<Long> pickupPoints;
    private List<Long> dropoffPoints;

    // Constructeurs
    public MultiDeliveryRequest() {}

    public MultiDeliveryRequest(long warehouseId, List<Long> pickupPoints, List<Long> dropoffPoints) {
        this.warehouseId = warehouseId;
        this.pickupPoints = pickupPoints;
        this.dropoffPoints = dropoffPoints;
    }

    // Getters et Setters
    public long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public List<Long> getPickupPoints() {
        return pickupPoints;
    }

    public void setPickupPoints(List<Long> pickupPoints) {
        this.pickupPoints = pickupPoints;
    }

    public List<Long> getDropoffPoints() {
        return dropoffPoints;
    }

    public void setDropoffPoints(List<Long> dropoffPoints) {
        this.dropoffPoints = dropoffPoints;
    }
}

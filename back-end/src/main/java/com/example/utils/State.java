package com.example.utils;

import java.util.*;

public class State implements Cloneable {
    public long currentPosition;
    public List<Long> path;
    public Set<Long> unvisitedPickups;
    public Map<Long, Long> activeDeliveries;
    public Map<Long, Double> pickupTimes;
    public double elapsedTime;

    // Durations for pickups and deliveries in seconds (default values)
    public double pickupDuration = 0.0;  // Default: 0 seconds
    public double deliveryDuration = 0.0;  // Default: 0 seconds

    public State(long currentPosition, List<Long> path, Set<Long> unvisitedPickups, Map<Long, Long> activeDeliveries, double elapsedTime) {
        this.currentPosition = currentPosition;
        this.path = path;
        this.unvisitedPickups = unvisitedPickups;
        this.activeDeliveries = activeDeliveries;
        this.pickupTimes = new HashMap<>();
        this.elapsedTime = elapsedTime;
    }

    @Override
    public State clone() {
        try {
            State clone = (State) super.clone();
            clone.path = new ArrayList<>(this.path);
            clone.unvisitedPickups = new HashSet<>(this.unvisitedPickups);
            clone.activeDeliveries = new HashMap<>(this.activeDeliveries);
            clone.pickupTimes = new HashMap<>(this.pickupTimes);
            clone.pickupDuration = this.pickupDuration;
            clone.deliveryDuration = this.deliveryDuration;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning failed", e);
        }
    }
}
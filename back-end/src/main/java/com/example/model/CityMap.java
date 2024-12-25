package com.example.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "city_maps") // Nom de la table dans la base de données
public class CityMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Clé primaire auto-incrémentée
    @Column(name = "id")
    private Long id;

    // Liste des intersections liées à cette carte
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "city_map_id") // Clé étrangère dans la table des intersections
    private List<Intersection> intersections = new ArrayList<>();

    // Liste des segments de route liés à cette carte
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "city_map_id") // Clé étrangère dans la table des segments de route
    private List<RoadSegment> roadSegments = new ArrayList<>();

    // Constructeur par défaut requis par JPA
    public CityMap() {}

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Intersection> getIntersections() {
        return intersections;
    }

    public void setIntersections(List<Intersection> intersections) {
        this.intersections = intersections;
    }

    public List<RoadSegment> getRoadSegments() {
        return roadSegments;
    }

    public void setRoadSegments(List<RoadSegment> roadSegments) {
        this.roadSegments = roadSegments;
    }

    @Override
    public String toString() {
        return "CityMap{" +
                "id=" + id +
                ", intersections=" + intersections +
                ", roadSegments=" + roadSegments +
                '}';
    }
}

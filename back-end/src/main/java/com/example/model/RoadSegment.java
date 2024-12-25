package com.example.model;

import jakarta.persistence.*;

// Annotation pour indiquer que cette classe est une entité persistable
@Entity
@Table(name = "road_segments") // Nom de la table dans la base de données
public class RoadSegment {

    // Clé primaire avec génération automatique
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Nom de la colonne pour l'ID unique
    private long id;

    // Clé étrangère ou relation pour l'origine
    @Column(name = "origin", nullable = false)
    private long origin;

    // Clé étrangère ou relation pour la destination
    @Column(name = "destination", nullable = false)
    private long destination;

    // Nom de la rue
    @Column(name = "street_name", nullable = false, length = 100)
    private String streetName;

    // Longueur du segment de route
    @Column(name = "length", nullable = false)
    private double length;

    // Constructeur par défaut requis par JPA
    public RoadSegment() {}

    // Constructeur avec arguments pour usage pratique
    public RoadSegment(long origin, long destination, String streetName, double length) {
        this.origin = origin;
        this.destination = destination;
        this.streetName = streetName;
        this.length = length;
    }

    // Getters et setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrigin() {
        return origin;
    }

    public void setOrigin(long origin) {
        this.origin = origin;
    }

    public long getDestination() {
        return destination;
    }

    public void setDestination(long destination) {
        this.destination = destination;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "RoadSegment{" +
                "id=" + id +
                ", origin=" + origin +
                ", destination=" + destination +
                ", streetName='" + streetName + '\'' +
                ", length=" + length +
                '}';
    }
}

package com.example.service;

import com.example.model.*;
import com.example.repository.CityMapRepository;
import com.example.repository.IntersectionRepository;
import com.example.repository.TourRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CityMapService {

    private final CityMapRepository cityMapRepository;
    private final TourRepository tourRepository;
    private final IntersectionRepository intersectionRepository;

    // Constructor injection for the repository
    public CityMapService(CityMapRepository cityMapRepository, TourRepository tourRepository, IntersectionRepository intersectionRepository) {
        this.cityMapRepository = cityMapRepository;
        this.tourRepository = tourRepository;
        this.intersectionRepository = intersectionRepository;
    }

    // Load a CityMap from an XML file and save it to the database
    public CityMap loadFromXML(String filePath) {
        CityMap cityMap = new CityMap();

        try {
            System.out.println("Parsing XML file: " + filePath);

            File xmlFile = new File(filePath);
            if (!xmlFile.exists()) {
                throw new FileNotFoundException("File not found: " + filePath);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

            // Parse intersections (noeuds)
            NodeList nodeList = doc.getElementsByTagName("noeud");
            System.out.println("Number of noeuds: " + nodeList.getLength());

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element nodeElement = (Element) nodeList.item(i);
                System.out.println("Parsing noeud #" + i);
                long id = Long.parseLong(nodeElement.getAttribute("id"));
                double latitude = Double.parseDouble(nodeElement.getAttribute("latitude"));
                double longitude = Double.parseDouble(nodeElement.getAttribute("longitude"));

                Intersection intersection = new Intersection(id, latitude, longitude);
                cityMap.getIntersections().add(intersection); // Add to CityMap
            }

            // Parse road segments (troncons)
            NodeList tronconList = doc.getElementsByTagName("troncon");
            System.out.println("Number of troncons: " + tronconList.getLength());
            for (int i = 0; i < tronconList.getLength(); i++) {
                Element tronconElement = (Element) tronconList.item(i);
                System.out.println("Parsing troncon #" + i);
                long origin = Long.parseLong(tronconElement.getAttribute("origine"));
                long destination = Long.parseLong(tronconElement.getAttribute("destination"));
                double length = Double.parseDouble(tronconElement.getAttribute("longueur"));
                String streetName = tronconElement.getAttribute("nomRue");

                RoadSegment roadSegment = new RoadSegment(origin, destination, streetName, length);
                cityMap.getRoadSegments().add(roadSegment); // Add to CityMap
            }

            // Save the CityMap to the database
            cityMap = cityMapRepository.save(cityMap);
            System.out.println("CityMap saved to the database with ID: " + cityMap.getId());

        } catch (Exception e) {
            System.err.println("Error parsing XML file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error parsing XML file", e);
        }

        return cityMap;
    }

    // Load a CityMap from XML content and save it to the database
    public CityMap loadFromXMLContent(String xmlContent) {
        CityMap cityMap = new CityMap();

        try {
            System.out.println("Parsing XML content...");

            // Convertir le contenu XML en Document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
            doc.getDocumentElement().normalize();

            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

            // Parse intersections (noeuds)
            NodeList nodeList = doc.getElementsByTagName("noeud");
            System.out.println("Number of noeuds: " + nodeList.getLength());

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element nodeElement = (Element) nodeList.item(i);
                System.out.println("Parsing noeud #" + i);
                long id = Long.parseLong(nodeElement.getAttribute("id"));
                double latitude = Double.parseDouble(nodeElement.getAttribute("latitude"));
                double longitude = Double.parseDouble(nodeElement.getAttribute("longitude"));

                Intersection intersection;

                // Vérifiez si l'intersection existe déjà dans la base de données
                if (intersectionRepository.existsById(id)) {
                    intersection = intersectionRepository.findById(id).orElseThrow();
                } else {
                    intersection = new Intersection(id, latitude, longitude);
                    intersection = intersectionRepository.save(intersection); // Persistez seulement si elle n'existe pas
                }

                // Ajoutez l'intersection à CityMap
                if (!cityMap.getIntersections().contains(intersection)) {
                    cityMap.getIntersections().add(intersection);
                }
            }

            // Parse road segments (troncons)
            NodeList tronconList = doc.getElementsByTagName("troncon");
            System.out.println("Number of troncons: " + tronconList.getLength());
            for (int i = 0; i < tronconList.getLength(); i++) {
                Element tronconElement = (Element) tronconList.item(i);
                System.out.println("Parsing troncon #" + i);
                long origin = Long.parseLong(tronconElement.getAttribute("origine"));
                long destination = Long.parseLong(tronconElement.getAttribute("destination"));
                double length = Double.parseDouble(tronconElement.getAttribute("longueur"));
                String streetName = tronconElement.getAttribute("nomRue");

                RoadSegment roadSegment = new RoadSegment(origin, destination, streetName, length);
                cityMap.getRoadSegments().add(roadSegment); // Add to CityMap
            }


            // Save the CityMap to the database
            cityMap = cityMapRepository.save(cityMap);
            System.out.println("CityMap saved to the database with ID: " + cityMap.getId());

        } catch (Exception e) {
            System.err.println("Error parsing XML content: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error parsing XML content", e);
        }

        return cityMap;
    }


    public CityMap getCityMapById(long id) {
        return cityMapRepository.findById(id).orElse(null);
    }

    public double[] findLatLongFromId(long id) {
        Intersection intersection = intersectionRepository.findById(id).orElse(null);
        assert intersection != null;
        return new double[]{intersection.getLatitude(), intersection.getLongitude()};
    }


    public Intersection findIntersectionById(Long id) {
        return intersectionRepository.findById(id).orElse(null);
    }

    public Tour addTour(Tour tour, Long warehouseId) {
        // Récupérer l'Intersection à partir de l'ID
        Intersection warehouse = intersectionRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse with ID " + warehouseId + " not found"));

        // Associer l'entrepôt au Tour
        tour.setWarehouse(warehouse);

        // Vérifie que le Courier est également initialisé
        if (tour.getCourier() == null) {
            throw new IllegalArgumentException("Courier must not be null");
        }

        // Sauvegarder le Tour
        return tourRepository.save(tour);
    }


    public List<DeliveryRequest> createDeliveryRequests(List<Long> pickupIds, List<Long> dropoffIds) {
        if (pickupIds.size() != dropoffIds.size()) {
            throw new IllegalArgumentException("Pickup and dropoff lists must have the same size.");
        }

        List<DeliveryRequest> deliveryRequests = new ArrayList<>();

        for (int i = 0; i < pickupIds.size(); i++) {
            Long pickupId = pickupIds.get(i);
            Long dropoffId = dropoffIds.get(i);

            // Récupérer les intersections correspondantes
            Intersection pickup = intersectionRepository.findById(pickupId)
                    .orElseThrow(() -> new IllegalArgumentException("Pickup ID not found: " + pickupId));

            Intersection dropoff = intersectionRepository.findById(dropoffId)
                    .orElseThrow(() -> new IllegalArgumentException("Dropoff ID not found: " + dropoffId));

            // Créer une nouvelle DeliveryRequest
            DeliveryRequest deliveryRequest = new DeliveryRequest();
            deliveryRequest.setPickup(pickup);
            deliveryRequest.setDelivery(dropoff);

            // Ajouter à la liste
            deliveryRequests.add(deliveryRequest);
        }

        return deliveryRequests;
    }

    public Tour createTourFromXML(String filePath) {
        try {
            // Charger le fichier XML
            File xmlFile = new File(filePath);
            if (!xmlFile.exists()) {
                throw new FileNotFoundException("XML file not found: " + filePath);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Récupérer l'entrepôt
            Element entrepotElement = (Element) doc.getElementsByTagName("entrepot").item(0);
            if (entrepotElement == null) {
                throw new IllegalArgumentException("Entrepôt not found in XML file.");
            }

            Long warehouseId = Long.parseLong(entrepotElement.getAttribute("adresse"));
            Intersection warehouse = intersectionRepository.findById(warehouseId)
                    .orElseThrow(() -> new IllegalArgumentException("Warehouse intersection not found: " + warehouseId));

            // Créer une nouvelle instance de Tour
            Tour tour = new Tour();
            tour.setWarehouse(warehouse);

            // Parcourir les livraisons
            NodeList livraisonNodes = doc.getElementsByTagName("livraison");
            List<DeliveryRequest> deliveryRequests = new ArrayList<>();
            for (int i = 0; i < livraisonNodes.getLength(); i++) {
                Element livraisonElement = (Element) livraisonNodes.item(i);

                Long pickupId = Long.parseLong(livraisonElement.getAttribute("adresseEnlevement"));
                Long deliveryId = Long.parseLong(livraisonElement.getAttribute("adresseLivraison"));
                Long pickupDuration = Long.parseLong(livraisonElement.getAttribute("dureeEnlevement"));
                Long deliveryDuration = Long.parseLong(livraisonElement.getAttribute("dureeLivraison"));

                Intersection pickup = intersectionRepository.findById(pickupId)
                        .orElseThrow(() -> new IllegalArgumentException("Pickup intersection not found: " + pickupId));
                Intersection delivery = intersectionRepository.findById(deliveryId)
                        .orElseThrow(() -> new IllegalArgumentException("Delivery intersection not found: " + deliveryId));

                DeliveryRequest deliveryRequest = new DeliveryRequest();
                deliveryRequest.setPickup(pickup);
                deliveryRequest.setDelivery(delivery);
                deliveryRequest.setPickupDuration(pickupDuration);
                deliveryRequest.setDeliveryDuration(deliveryDuration);

                deliveryRequests.add(deliveryRequest);
            }

            // Associer les DeliveryRequests au Tour
            tour.setDeliveryRequests(deliveryRequests);

            return tour;

        } catch (Exception e) {
            System.err.println("Error parsing XML file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating Tour from XML file", e);
        }
    }

    public Tour createTourFromXMLContent(String fileContent) {
        try {
            // Charger le contenu XML à partir de la chaîne
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            // Récupérer l'entrepôt
            Element entrepotElement = (Element) doc.getElementsByTagName("entrepot").item(0);
            if (entrepotElement == null) {
                throw new IllegalArgumentException("Entrepôt not found in XML content.");
            }

            Long warehouseId = Long.parseLong(entrepotElement.getAttribute("adresse"));
            Intersection warehouse = intersectionRepository.findById(warehouseId)
                    .orElseThrow(() -> new IllegalArgumentException("Warehouse intersection not found: " + warehouseId));

            // Créer une nouvelle instance de Tour
            Tour tour = new Tour();
            tour.setWarehouse(warehouse);

            // Parcourir les livraisons
            NodeList livraisonNodes = doc.getElementsByTagName("livraison");
            List<DeliveryRequest> deliveryRequests = new ArrayList<>();
            for (int i = 0; i < livraisonNodes.getLength(); i++) {
                Element livraisonElement = (Element) livraisonNodes.item(i);

                Long pickupId = Long.parseLong(livraisonElement.getAttribute("adresseEnlevement"));
                Long deliveryId = Long.parseLong(livraisonElement.getAttribute("adresseLivraison"));

                Intersection pickup = intersectionRepository.findById(pickupId)
                        .orElseThrow(() -> new IllegalArgumentException("Pickup intersection not found: " + pickupId));
                Intersection delivery = intersectionRepository.findById(deliveryId)
                        .orElseThrow(() -> new IllegalArgumentException("Delivery intersection not found: " + deliveryId));

                DeliveryRequest deliveryRequest = new DeliveryRequest();
                deliveryRequest.setPickup(pickup);
                deliveryRequest.setDelivery(delivery);

                deliveryRequests.add(deliveryRequest);
            }

            // Associer les DeliveryRequests au Tour
            tour.setDeliveryRequests(deliveryRequests);

            return tour;

        } catch (Exception e) {
            System.err.println("Error parsing XML content: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating Tour from XML content", e);
        }
    }




}



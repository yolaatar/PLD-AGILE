package com.example.controller;

import com.example.model.*;
import com.example.service.CityMapService;
import com.example.service.DataService;
import com.example.service.PathFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/city-map")
public class CityMapController {

    @Autowired
    private final CityMapService cityMapService;
    private final DataService dataService;

    private CityMap loadedCityMap;

    public CityMapController(CityMapService cityMapService, DataService dataService) {
        this.cityMapService = cityMapService;
        this.dataService = dataService;
    }

    /**
     * Endpoint pour charger une CityMap à partir d'un fichier XML.
     *
     * @return La CityMap ou une erreur en cas de problème
     */
    @CrossOrigin(origins = "http://localhost:5173") // Allow requests from your frontend
    @GetMapping("/loadmap")
    public ResponseEntity<?> getCityMap() {
        try {
            // Load the city map from the XML file
            if ((loadedCityMap = cityMapService.getCityMapById(1)) == null) {
                loadedCityMap = cityMapService.loadFromXML("src/main/resources/fichiersXMLPickupDelivery/petitPlan.xml");
            }
            // Return the loaded city map as JSON
            return ResponseEntity.ok(loadedCityMap);
        } catch (Exception e) {
            // Return an error message in case of failure
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error loading city map from file: " + e.getMessage());
        }
    }

    @PostMapping("/loadmap-content")
    public ResponseEntity<?> loadCityMap(@RequestBody String xmlContent) {
        try {
            // Charger la carte depuis le contenu XML
            loadedCityMap = cityMapService.loadFromXMLContent(xmlContent);

            // Retourner la carte chargée en tant que réponse JSON
            return ResponseEntity.ok(loadedCityMap);
        } catch (Exception e) {
            // Retourner un message d'erreur en cas d'échec
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error loading city map from XML content: " + e.getMessage());
        }
    }


    @PostMapping("/optimize-sequence")
    public ResponseEntity<?> optimizeAndPersistTour(@RequestBody Map<String, Object> requestData) {
        try {
            // Étape 1 : Vérification et extraction des données
            if (!requestData.containsKey("start") || !requestData.containsKey("pickups") || !requestData.containsKey("dropoffs") || !requestData.containsKey("courier")) {
                return ResponseEntity.badRequest().body("Invalid request data: missing required fields.");
            }

            Long startId = ((Number) requestData.get("start")).longValue();
            int courierId = ((Number) requestData.get("courier")).intValue();
            @SuppressWarnings("unchecked")
            List<Object> rawPickups = (List<Object>) requestData.get("pickups");
            List<Long> pickupIds = rawPickups.stream()
                    .map(pickup -> ((Number) pickup).longValue())
                    .toList();

            @SuppressWarnings("unchecked")
            List<Object> rawDropoffs = (List<Object>) requestData.get("dropoffs");
            List<Long> dropoffIds = rawDropoffs.stream()
                    .map(dropoff -> ((Number) dropoff).longValue())
                    .toList();

            // Validation des données
            if (pickupIds.size() != dropoffIds.size()) {
                return ResponseEntity.badRequest().body("Pickup and dropoff lists must have the same size.");
            }

            // Étape 2 : Appeler l'algorithme pour optimiser la route
            List<Long> optimizedPath = PathFinder.greedyOptimizeDeliverySequenceWithPath(
                    loadedCityMap,
                    startId,
                    pickupIds,
                    dropoffIds
            );

            if (optimizedPath == null || optimizedPath.isEmpty()) {
                return ResponseEntity.badRequest().body("Failed to optimize the delivery sequence.");
            }

            // Étape 3 : Persister le Tour avec les DeliveryRequests
            Tour tour = new Tour();
            Intersection warehouse = cityMapService.findIntersectionById(startId) ;
            tour.setWarehouse(warehouse);
            System.out.println("le start id est : " + startId);
            System.out.println("la warehouse est : " + warehouse);
            Optional<Courier> courier = dataService.getCourierById(courierId);
            tour.setCourier(courier.orElse(null));

            // Créer les DeliveryRequests à partir des IDs de pickup et dropoff
            List<DeliveryRequest> deliveryRequests = cityMapService.createDeliveryRequests(pickupIds, dropoffIds);
            tour.setDeliveryRequests(deliveryRequests);

            // Persister le Tour
            cityMapService.addTour(tour, startId);
            System.out.println("Tour validé et persistant avec succès.");

            // Étape 4 : Conversion des IDs en coordonnées lat/lng
            List<double[]> coordinates = optimizedPath.stream()
                    .map(id -> {
                        double[] latLng = cityMapService.findLatLongFromId(id);
                        if (latLng == null) {
                            throw new IllegalArgumentException("ID not found in map: " + id);
                        }
                        return latLng;
                    })
                    .toList();

            // Retourner les coordonnées optimisées au front-end
            return ResponseEntity.ok(coordinates);

        } catch (Exception e) {
            // Gestion des erreurs
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error optimizing and persisting the tour: " + e.getMessage());
        }
    }

    @GetMapping("/import/load-tour-from-xml")
    public ResponseEntity<?> loadTourFromXML(@RequestParam("filePath") String filePath) {
        try {
            // Étape 1 : Créer le Tour à partir du fichier XML
            Tour tour = cityMapService.createTourFromXML("src/main/resources/fichiersXMLPickupDelivery/" + filePath);

            // Étape 2 : Récupérer les données nécessaires pour la réponse
            Long startId = tour.getWarehouse().getId();
            List<Long> pickupIds = tour.getDeliveryRequests().stream()
                    .map(request -> request.getPickup().getId())
                    .toList();
            List<Long> dropoffIds = tour.getDeliveryRequests().stream()
                    .map(request -> request.getDelivery().getId())
                    .toList();

            // Étape 3 : Appeler l'algorithme pour optimiser la route
            List<Long> optimizedPath = PathFinder.greedyOptimizeDeliverySequenceWithPath(
                    loadedCityMap,
                    startId,
                    pickupIds,
                    dropoffIds
            );

            if (optimizedPath == null || optimizedPath.isEmpty()) {
                return ResponseEntity.badRequest().body("Failed to optimize the delivery sequence.");
            }

            // Étape 4 : Conversion des IDs en coordonnées lat/lng
            List<double[]> coordinates = optimizedPath.stream()
                    .map(id -> {
                        double[] latLng = cityMapService.findLatLongFromId(id);
                        if (latLng == null) {
                            throw new IllegalArgumentException("ID not found in map: " + id);
                        }
                        return latLng;
                    })
                    .toList();

            Courier courier = tour.getCourier(); // Récupérer le coursier

            // Étape 5 : Préparer la réponse finale pour le front-end
            Map<String, Object> response = new HashMap<>();
            response.put("warehouse", Map.of(
                    "id", startId,
                    "coordinates", cityMapService.findLatLongFromId(startId)
            ));
            response.put("pickups", pickupIds.stream()
                    .map(id -> Map.of(
                            "id", id,
                            "coordinates", cityMapService.findLatLongFromId(id)
                    ))
                    .toList());
            response.put("dropoffs", dropoffIds.stream()
                    .map(id -> Map.of(
                            "id", id,
                            "coordinates", cityMapService.findLatLongFromId(id)
                    ))
                    .toList());
            response.put("route", coordinates);
            response.put("courier", courier != null ? Map.of(
                    "id", courier.getId(),
                    "name", courier.getName()
            ) : null); // Inclure le coursier si disponible

            // Retourner la réponse formatée
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating tour from XML: " + e.getMessage());
        }
    }

    @PostMapping("/import/load-tour-from-xml-content")
    public ResponseEntity<?> loadTourFromXMLContent(@RequestBody String fileContent) {
        try {
            // Étape 1 : Créer le Tour à partir du contenu XML
            Tour tour = cityMapService.createTourFromXMLContent(fileContent);

            // Étape 2 : Récupérer les données nécessaires pour la réponse
            Long startId = tour.getWarehouse().getId();
            List<Long> pickupIds = new ArrayList<>();
            List<Long> dropoffIds = new ArrayList<>();
            List<Double> pickupDurations = new ArrayList<>();
            List<Double> deliveryDurations = new ArrayList<>();
            for (DeliveryRequest request : tour.getDeliveryRequests()) {
                pickupIds.add(request.getPickup().getId());
                dropoffIds.add(request.getDelivery().getId());
                pickupDurations.add(request.getPickupDuration()); // Already in seconds
                deliveryDurations.add(request.getDeliveryDuration()); // Already in seconds
            }
            // Étape 3 : Appeler l'algorithme pour optimiser la route
            List<Long> optimizedPath = PathFinder.greedyOptimizeDeliverySequenceWithPath(
                    loadedCityMap,
                    startId,
                    pickupIds,
                    dropoffIds,
                    pickupDurations,
                    deliveryDurations
            );

            if (optimizedPath == null || optimizedPath.isEmpty()) {
                return ResponseEntity.badRequest().body("Failed to optimize the delivery sequence.");
            }

            // Étape 4 : Conversion des IDs en coordonnées lat/lng
            List<double[]> coordinates = optimizedPath.stream()
                    .map(id -> {
                        double[] latLng = cityMapService.findLatLongFromId(id);
                        if (latLng == null) {
                            throw new IllegalArgumentException("ID not found in map: " + id);
                        }
                        return latLng;
                    })
                    .toList();

            Courier courier = tour.getCourier(); // Récupérer le coursier

            // Étape 5 : Préparer la réponse finale pour le front-end
            Map<String, Object> response = new HashMap<>();
            response.put("warehouse", Map.of(
                    "id", startId,
                    "coordinates", cityMapService.findLatLongFromId(startId)
            ));
            response.put("pickups", pickupIds.stream()
                    .map(id -> Map.of(
                            "id", id,
                            "coordinates", cityMapService.findLatLongFromId(id)
                    ))
                    .toList());
            response.put("dropoffs", dropoffIds.stream()
                    .map(id -> Map.of(
                            "id", id,
                            "coordinates", cityMapService.findLatLongFromId(id)
                    ))
                    .toList());
            response.put("route", coordinates);
            response.put("courier", courier != null ? Map.of(
                    "id", courier.getId(),
                    "name", courier.getName()
            ) : null); // Inclure le coursier si disponible

            // Retourner la réponse formatée
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating tour from XML content: " + e.getMessage());
        }
    }


}

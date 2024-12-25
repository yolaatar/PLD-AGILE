import React, { useEffect, useState } from "react";
import DeliveryPlanner from "./components/DeliveryPlanner";
import PlaceholderMap from "./components/PlaceholderMap";
import { fetchMap, sendRequestToBackend } from "./api/Services";
import { fetchIntersections } from "./api/Services";
import { generateUniqueId } from "./utils/utils";

import "./styles/App.css";
import "leaflet/dist/leaflet.css";

function App() {
	const [tours, setTours] = useState([]);
	const [currentTour, setCurrentTour] = useState({
		id: null,
		courier: null,
		warehouse: null,
		requests: [],
	});
	const [selectionStep, setSelectionStep] = useState(null);
	const [intersections, setIntersections] = useState([]);
	const [route, setRoute] = useState([]);

	useEffect(() => {
		const fetchData = async () => {
			await fetchIntersections(setIntersections);
		};

		fetchData();
	}, []);

	const handleFileUpload = async (event) => {
		const file = event.target.files[0]; // Récupérer le fichier sélectionné
		if (!file) return;

		try {
			const response = await importXMLFile(file); // Envoyer le fichier au back-end
			console.log("Processed data from XML:", response);
			// Mettre à jour l'état ou effectuer une action avec les données reçues
		} catch (error) {
			console.error("Failed to process XML file:", error);
		}
	};

	const handleFechMap = async (fileContent) => {
		try {
			const response = await fetchMap(fileContent);
			console.log("Processed map data from XML:", response);
			setIntersections(response.intersections || []);
		} catch (error) {
			console.error("Failed to process XML file:", error);
		}
	};	

	const handleNodeClick = async (node) => {
		if (!selectionStep) return;

		setCurrentTour((prevTour) => {
			const updatedTour = { ...prevTour };

			if (selectionStep === "courier") {
				alert(
					"Select a courier before choosing a warehouse, pickup or delivery location."
				);
				return prevTour;
			} else if (selectionStep === "warehouse") {
				updatedTour.warehouse = node;
				setSelectionStep("pickup");
			} else if (selectionStep === "pickup") {
				updatedTour.requests = [
					...updatedTour.requests,
					{
						id: generateUniqueId(),
						pickup: node,
						delivery: null,
					},
				];
				setSelectionStep("delivery");
			} else if (selectionStep === "delivery") {
				if (updatedTour.requests.length === 0) {
					alert(
						"Please add a pickup request before selecting a delivery location."
					);
					return prevTour;
				}

				updatedTour.requests[updatedTour.requests.length - 1].delivery = node;
				setSelectionStep("pickup");
			}

			return updatedTour;
		});
	};

	

	const startNewTour = () => {
		setSelectionStep("courier");
		setCurrentTour({
			id: generateUniqueId(),
			courier: null,
			warehouse: null,
			requests: [],
		});
	};

	const finalizeTour = async () => {
		if (
			currentTour.courier &&
			currentTour.warehouse &&
			currentTour.requests.length > 0 &&
			currentTour.requests.every((req) => req.pickup && req.delivery)
		) {
			try {
				const payload = {
					start: currentTour.warehouse.id,
					pickups: currentTour.requests.map((req) => req.pickup.id),
					dropoffs: currentTour.requests.map((req) => req.delivery.id),
					courier: currentTour.courier.id, // Inclure l'ID du livreur
				};
				
				const response = await sendRequestToBackend(payload);

				const parsedRoute = response.map(([lat, lng]) => ({ lat, lng }));

				const updatedCurrentTour = { ...currentTour, route: parsedRoute };

				setTours([...tours, updatedCurrentTour]);

				setRoute(parsedRoute);

				setCurrentTour({
					id: null,
					courier: null,
					warehouse: null,
					requests: [],
				});
				setSelectionStep(null);
			} catch (error) {
				alert("An error occurred while finalizing the tour. Please try again.");
				setCurrentTour({
					id: null,
					courier: null,
					warehouse: null,
					requests: [],
				});
				setSelectionStep(null);
			}
		} else {
			alert(
				"The tour is not complete. Make sure you have selected a courier, a warehouse, and at least one complete request."
			);
			setCurrentTour({
				id: null,
				courier: null,
				warehouse: null,
				requests: [],
			});
			setSelectionStep(null);
		}
	};

	const finalizeEditedTour = async (
		editedTour,
		setEditedTour,
		setEditingTourId
	) => {
		if (!editedTour) return;
	
		// Vérifiez que la tournée éditée est complète
		if (
			editedTour.courier &&
			editedTour.warehouse &&
			editedTour.requests.length > 0 &&
			editedTour.requests.every((req) => req.pickup && req.delivery)
		) {
			try {
				// Préparer le payload avec le champ `courier`
				const payload = {
					start: editedTour.warehouse.id,
					pickups: editedTour.requests.map((req) => req.pickup.id),
					dropoffs: editedTour.requests.map((req) => req.delivery.id),
					courier: editedTour.courier.id, // Assurez-vous que le champ `courier` est inclus
				};
	
				console.log("Payload sent to backend for editing:", payload);
	
				// Envoyer la requête au backend
				const response = await sendRequestToBackend(payload);
	
				// Parser la réponse pour extraire la route
				const parsedRoute = response.map(([lat, lng]) => ({ lat, lng }));
	
				// Mettre à jour la tournée avec la nouvelle route
				const updatedTourWithRoute = { ...editedTour, route: parsedRoute };
	
				// Mettre à jour la liste des tournées en remplaçant la tournée éditée
				setTours(
					tours.map((tour) =>
						tour.id === editedTour.id ? updatedTourWithRoute : tour
					)
				);
	
				// Mettre à jour la route affichée sur la carte
				setRoute(parsedRoute);
	
				// Réinitialiser les états liés à l'édition
				setEditingTourId(null);
				setEditedTour(null);
			} catch (error) {
				console.error("Error updating the edited tour:", error);
				alert("The tour update failed. Please try again.");
			}
		} else {
			alert(
				"The edited tour is not complete. Please make sure to select a courier, a warehouse, and at least one complete request."
			);
		}
	};
	

	return (
		<div className="App">
			<div className="map-section">
				<PlaceholderMap
					intersections={intersections}
					tours={tours}
					onNodeClick={handleNodeClick}
					selectionStep={selectionStep}
					currentTour={currentTour}
					route={route}
				/>
			</div>
			<div className="planner-section">
				<DeliveryPlanner
					startNewTour={startNewTour}
					setTours={setTours} // Passez setTours comme prop ici
					tours={tours}
					setCurrentTour={setCurrentTour}
					selectionStep={selectionStep}
					setSelectionStep={setSelectionStep}
					finalizeTour={finalizeTour}
					finalizeEditedTour={finalizeEditedTour}
					setRoute={setRoute}
					handleFechMap={handleFechMap}
				/>
			</div>
		</div>
	);
}

export default App;
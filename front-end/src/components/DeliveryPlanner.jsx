import React, { useState, useEffect, useRef } from "react";
import { fetchMap, getCouriers, uploadXMLContent } from "../api/Services";
import { importXMLFile } from "../api/Services";
import {
	FaWarehouse,
	FaTruckPickup,
	FaShippingFast,
	FaPlus,
	FaUser,
} from "react-icons/fa";
import { RxCross2 } from "react-icons/rx";
import { generateDeliveryXML } from "../utils/utils";
import { HiOutlinePencil, HiOutlineArrowLeft } from "react-icons/hi";
import { MdOutlineCancel } from "react-icons/md";
import { generateUniqueId } from "../utils/utils";


const DeliveryPlanner = ({
	startNewTour,
	tours,
	setTours,
	setCurrentTour,
	selectionStep,
	setSelectionStep,
	finalizeTour,
	finalizeEditedTour,
	setRoute,
	handleFechMap,
}) => {
	const fileInputRef = useRef(null); // Référence pour l'input file
	const [editingTourId, setEditingTourId] = useState(null);
	const [editedTour, setEditedTour] = useState(null);

	// Fonction pour ouvrir la boîte de dialogue fichier
	const handleImportClick = () => {
		fileInputRef.current.click();
	};

	const [selectedCourier, setSelectedCourier] = useState(null);
	const [couriers, setCouriers] = useState([]);

	useEffect(() => {
		const fetchCouriers = async () => {
			try {
				const data = await getCouriers(); // Appel à l'API
				setCouriers(data); // On garde uniquement les noms
			} catch (err) {
				setError("Failed to load couriers. Please try again.");
				console.error("Error fetching couriers:", err);
			}
		};
		fetchCouriers();
	}, []);

	const handleTourClick = (tour) => {
		if (tour.route) {
			console.log("Tour clicked:", tour);
			setRoute(tour.route);
		} else {
			console.warn("This tour does not have a route.");
		}
	};

	const exportToursToXML = () => {
		const xmlContent = generateDeliveryXML(tours);
		const blob = new Blob([xmlContent], { type: "application/xml" });
		const link = document.createElement("a");
		link.href = URL.createObjectURL(blob);
		link.download = "export_tours.xml";
		link.click();
	};


	const handleImportedTour = async (fileContent) => {
		try {
			// Appeler la fonction pour importer le fichier XML
			const response = await uploadXMLContent(fileContent);

			console.log("Backend response for imported tour:", response);

			// Extraire les données du backend
			const { warehouse, pickups, dropoffs, courier, route } = response;

			// Vérifier que les données retournées sont valides
			if (!warehouse || !pickups || !dropoffs || !route) {
				throw new Error("Invalid data received from backend for imported tour.");
			}

			// Formater les données pour qu'elles soient conformes à `PlaceholderMap`
			const formattedWarehouse = {
				id: warehouse.id,
				latitude: warehouse.coordinates[0],
				longitude: warehouse.coordinates[1],
			};

			const formattedPickups = pickups.map((pickup) => ({
				id: pickup.id,
				latitude: pickup.coordinates[0],
				longitude: pickup.coordinates[1],
			}));

			const formattedDropoffs = dropoffs.map((dropoff) => ({
				id: dropoff.id,
				latitude: dropoff.coordinates[0],
				longitude: dropoff.coordinates[1],
			}));

			const formattedRoute = route.map(([lat, lng]) => ({
				lat,
				lng,
			}));

			// Définir le coursier par défaut
			const defaultCourier = { id: 1, name: "Importé par XML" };

			// Si aucun coursier n'est fourni, utilisez le coursier par défaut
			const formattedCourier = courier
				? { id: courier.id, name: courier.name }
				: defaultCourier;


			// Construire un objet de tournée à partir des données formatées
			const importedTour = {
				courier: formattedCourier, // Ajouter le coursier
				warehouse: formattedWarehouse,
				requests: formattedPickups.map((pickup, index) => ({
					id: generateUniqueId(), // Générer un ID unique
					pickup,
					delivery: formattedDropoffs[index],
				})),
				route: formattedRoute,
			};

			console.log("Constructed imported tour:", importedTour);

			// Ajouter la tournée importée à la liste des tournées
			setTours((prevTours) => [...prevTours, importedTour]);

			// Mettre à jour la route pour qu'elle soit affichée sur la carte
			setRoute(formattedRoute);
		} catch (error) {
			console.error("Error handling imported tour:", error);
			alert("An error occurred while importing the tour. Please check the file and try again.");
		}
	};


	const cancelTour = () => {
		// Reset the current tour and selection step
		setCurrentTour({
			id: null,
			courier: null,
			warehouse: null,
			requests: [],
		});
		setSelectionStep(null); // Reset to show the main sidebar
	};

	const getStepMessage = () => {
		if (selectionStep === "courier")
			return "Please select a courier for the new tour";
		if (selectionStep === "warehouse")
			return "Click on the map to select the warehouse for the new tour.";
		if (selectionStep === "pickup")
			return "Click on the map to select the pickup location for the new request.";
		if (selectionStep === "delivery")
			return "Click on the map to select the delivery location for the new request.";
		return "Press the button to start creating a new tour.";
	};

	const startNewTourWithCourier = () => {
		setSelectedCourier(null);
		setSelectionStep("courier");
		startNewTour();
	};

	const handleCourierSelect = (courier) => {
		setSelectedCourier(courier);
		setCurrentTour((prevTour) => ({
			...prevTour,
			courier,
			requests: [], // Si vous souhaitez réinitialiser les requêtes lors de la sélection d'un nouveau courier
		}));
		setSelectionStep("warehouse");
	};

	const endTour = () => {
		finalizeTour();
	};

	const handleEditTour = (tourId) => {
		const tourToEdit = tours.find((tour) => tour.id === tourId);
		if (tourToEdit) {
			setEditingTourId(tourId);
			// Créer une copie profonde de la tournée pour éviter les mutations directes
			const tourCopy = JSON.parse(JSON.stringify(tourToEdit));
			setEditedTour(tourCopy);
		} else {
			console.error("Tour not found:", tourId);
		}
	};

	const handleDeleteDelivery = (requestId) => {
		if (!editedTour) return;

		if (!window.confirm("Are you sure you want to delete this delivery?"))
			return;

		const updatedRequests = editedTour.requests.filter(
			(req) => req.id !== requestId
		);

		setEditedTour({
			...editedTour,
			requests: updatedRequests,
		});
	};

	const deleteTourById = (tours, tourId) => {
		if (!Array.isArray(tours)) {
			console.error("Expected an array but got:", tours);
			return [];
		}
		// Filter out the tour with the specified ID
		return tours.filter((tour) => tour.id !== tourId);
	};
	
	

	const deleteTourWithFewestRequestsById = (tours, tourId) => {
		const filteredTours = tours.filter((tour) => tour.id === tourId);

		if (filteredTours.length <= 1) {
			// If only one or no tour with the given ID, return tours without changes
			return tours.filter((tour) => tour.id !== tourId);
		}

		// Find the tour with the minimum number of requests
		const tourToDelete = filteredTours.reduce((minTour, currentTour) =>
			currentTour.requests.length < minTour.requests.length ? currentTour : minTour
		);

		// Return the tours without the tour with fewer requests
		return tours.filter((tour) => tour !== tourToDelete);
	};

	const handleAddDelivery = (tourId) => {
		if (!editedTour || editedTour.id !== tourId) {
			console.error("No tour is being edited or invalid tour ID.");
			return;
		}

		// Log the edited tour info for debugging
		console.log("Tour Info:", {
			id: editedTour.id,
			courier: editedTour.courier,
			warehouse: editedTour.warehouse,
			requests: editedTour.requests.map((req) => ({
				id: req.id,
				pickup: req.pickup,
				delivery: req.delivery,
			})),
		});

		// Set the selection step to "pickup" and assign the current tour to the edited tour
		setSelectionStep("pickup");
		setCurrentTour(editedTour);

	};

	const handleCancelEditing = () => {
		setEditingTourId(null);
		setEditedTour(null);
	};

	const handleValidateEditing = (
		editedTour,
		setEditedTour,
		setEditingTourId
	) => {
		finalizeEditedTour(editedTour, setEditedTour, setEditingTourId);
	};

	{/* Références pour les inputs */ }
	const fileInputRefXML = useRef(null); // Référence pour Import XML
	const fileInputRefMap = useRef(null); // Référence pour Import Map

	return (
		<div
			style={{
				padding: "1rem",
				color: "#000000",
				maxWidth: "600px",
				margin: "0 auto",
			}}
		>
			{/* Barre de navigation avec icônes */}
			<div
				style={{
					display: "flex",
					justifyContent: "center",
					alignItems: "center",
					backgroundColor: "#f0f0f0",
					padding: "1rem",
					borderRadius: "0.75rem",
					marginBottom: "1rem",
					gap: "1rem",
				}}
			>
				<button
					onClick={selectionStep === null ? startNewTourWithCourier : null}
					style={{
						backgroundColor: "#336659",
						color: "white",
						padding: "0.5rem 0.5rem",
						fontSize: "0.750rem",
						border: "none",
						borderRadius: "0.5rem",
						cursor: "pointer",
						display: "flex",
						alignItems: "center",
						gap: "0.5rem",
					}}
				>
					{selectionStep === null ? (
						<>
							<FaPlus /> New Tour
						</>
					) : (
						"Current Tour n°" + tours.length
					)}
				</button>

				{/* Bouton Import XML */}
				{selectionStep == null && (
					<>
						<button
							onClick={() => fileInputRefXML.current.click()} // Appelle la bonne référence
							style={{
								backgroundColor: "#2196F3",
								color: "white",
								padding: "0.5rem 0.5rem",
								fontSize: "0.750rem",
								border: "none",
								borderRadius: "0.5rem",
								cursor: "pointer",
								display: "flex",
								alignItems: "center",
							}}
						>
							Import XML
						</button>
						<input
							type="file"
							accept=".xml"
							ref={fileInputRefXML} // Référence unique pour Import XML
							style={{ display: "none" }}
							onChange={async (e) => {
								const file = e.target.files[0];
								if (file) {
									try {
										const fileReader = new FileReader();
										fileReader.onload = async (event) => {
											const fileContent = event.target.result;
											console.log("XML Content Loaded:", fileContent);

											handleImportedTour(fileContent); // Fonction associée à l'import XML
										};
										fileReader.readAsText(file);
									} catch (error) {
										console.error("Error reading XML file:", error);
										alert("Failed to read the XML file. Please try again.");
									}
								}
							}}
						/>
					</>
				)}

				{/* Bouton Import Map XML */}
				{selectionStep == null && (
					<>
						<button
							onClick={() => fileInputRefMap.current.click()} // Appelle la bonne référence
							style={{
								backgroundColor: "#4CAF50",
								color: "white",
								padding: "0.5rem 0.5rem",
								fontSize: "0.750rem",
								border: "none",
								borderRadius: "0.5rem",
								cursor: "pointer",
								display: "flex",
								alignItems: "center",
							}}
						>
							Import Map
						</button>
						<input
							type="file"
							accept=".xml"
							ref={fileInputRefMap} // Référence unique pour Import Map
							style={{ display: "none" }}
							onChange={async (e) => {
								const file = e.target.files[0];
								if (file) {
									try {
										const fileReader = new FileReader();
										fileReader.onload = async (event) => {
											const fileContent = event.target.result;
											console.log("Map Content Loaded:", fileContent);

											handleFechMap(fileContent); // Fonction associée à l'import Map
										};
										fileReader.readAsText(file);
									} catch (error) {
										console.error("Error reading Map file:", error);
										alert("Failed to read the Map file. Please try again.");
									}
								}
							}}
						/>
					</>
				)}


				{selectionStep == "pickup" && (
					<button
						onClick={endTour}
						style={{
							backgroundColor: "#f88e55",
							color: "white",
							padding: "0.5rem 0.5rem",
							fontSize: "0.750rem",
							border: "none",
							borderRadius: "0.5rem",
							cursor: "pointer",
							display: "flex",
							alignItems: "center",
							gap: "0.5rem",
						}}
					>
						<RxCross2 />
						End tour
					</button>
				)}

				{selectionStep !== null && (
					<button
						onClick={cancelTour}
						style={{
							backgroundColor: "#800020",
							color: "white",
							padding: "0.5rem 0.5rem",
							fontSize: "0.750rem",
							border: "none",
							borderRadius: "0.5rem",
							cursor: "pointer",
							display: "flex",
							alignItems: "center",
							gap: "0.5rem",
						}}
					>
						<RxCross2 />
						Cancel Tour
					</button>
				)}
			</div>

			{/* Message d'instruction */}
			<p
				style={{ textAlign: "center", fontSize: "1rem", marginBottom: "1rem" }}
			>
				{getStepMessage()}
			</p>

			{/* Sélection du livreur */}
			{selectionStep === "courier" && (
				<div
					style={{
						display: "flex",
						flexDirection: "column",
						gap: "0.5rem",
						marginBottom: "1rem",
					}}
				>
					<h3>Select a Courier:</h3>
					{couriers.map((courier, index) => (
						<button
							key={courier.id}
							onClick={() => handleCourierSelect(courier)} // Passe l'objet complet
							style={{
								padding: "0.5rem 1rem",
								backgroundColor:
									selectedCourier?.id === courier.id ? "#4CAF50" : "#f0f0f0",
								color: selectedCourier?.id === courier.id ? "white" : "black",
								border: "1px solid #ccc",
								borderRadius: "0.5rem",
								cursor: "pointer",
							}}
						>
							{courier.name}
						</button>
					))}

				</div>

			)}

			{/* Liste des tours */}
			<ul style={{ listStyleType: "none", padding: 0 }}>
				{tours.map((tour, indexTour) => (
					<li
						key={tour.id}
						onClick={() => handleTourClick(tour)}
						className="tour-item"
						style={{
							borderRadius: "0.75rem",
							border: "2px solid #000000",
							padding: "1rem",
							marginBottom: "0.5rem",
							backgroundColor: "#f9f9f9",
							fontSize: "0.9rem",
							transition: "transform 0.2s",
							cursor: "pointer",
						}}
					>
						<div
							style={{
								display: "flex",
								alignItems: "center",
								justifyContent: "space-between",
								padding: "0.25rem",
							}}
						>
							<strong>Tour n° : {indexTour + 1}</strong>
							<div
								style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}
							>
								<HiOutlinePencil
									style={{ fontSize: "1.5rem", cursor: "pointer" }}
									onClick={(e) => {
										e.stopPropagation();
										handleEditTour(tour.id);
									}}
								/>
								{editingTourId === tour.id && editedTour && (
									<HiOutlineArrowLeft
										onClick={(e) => {
											e.preventDefault();
											handleCancelEditing();
										}}
										style={{
											top: "0.5rem",
											right: "0.5rem",
											cursor: "pointer",
											color: "#FF9800", // Orange color
											fontSize: "1.5rem",
										}}
										title="Cancel Editing"
									/>
									
								)}
								{editingTourId === tour.id && editedTour && (
										<MdOutlineCancel
											onClick={(e) => {
												e.preventDefault();
												setTours(deleteTourById(tour,tour.id));
												setEditingTourId(null);
												setEditedTour(null);
												setRoute([]);
											}}
											style={{
												top: "0.5rem",
												right: "0.5rem",
												cursor: "pointer",
												color: "#800020",
												fontSize: "1.5rem",
											}}
											title="Supprimer le tour"
										/>
									)}
							</div>
						</div>

						<div style={{ padding: "0.25rem" }}>
							<FaUser size={15} color="#9C27B0" title="Courier" />
							<strong>Courier:</strong> {tour.courier.name}
						</div>
						<div style={{ padding: "0.25rem" }}>
							<FaWarehouse size={15} color="#4CAF50" title="Warehouse" />
							<strong>Warehouse:</strong> ({tour.warehouse.latitude},{" "}
							{tour.warehouse.longitude})
						</div>

						{/* Liste des requêtes */}
						<ul
							style={{
								listStyleType: "none",
								paddingLeft: "1rem",
								marginTop: "0.5rem",
							}}

							
						>
							{(editingTourId === tour.id
								? editedTour.requests
								: tour.requests
							).map((req, indexReq) => (
								<li
									key={req.id}
									style={{
										position: "relative", // Positionnement relatif pour le conteneur
										borderRadius: "0.75rem",
										border: "2px solid #000000",
										padding: "1rem",
										marginBottom: "0.5rem",
										backgroundColor: "#f9f9f9",
										fontSize: "0.9rem",
									}}
								>
									{editingTourId === tour.id && editedTour && (
										<MdOutlineCancel
											onClick={(e) => {
												e.preventDefault();
												handleDeleteDelivery(req.id);
											}}
											style={{
												position: "absolute",
												top: "0.5rem",
												right: "0.5rem",
												cursor: "pointer",
												color: "#800020",
												fontSize: "1.5rem",
											}}
											title="Supprimer la livraison"
										/>
									)}
									<div style={{ padding: "0.25rem" }}>
										<FaTruckPickup size={15} color="#FF9800" title="Pickup" />
										<strong>Pickup:</strong> ({req.pickup.latitude},{" "}
										{req.pickup.longitude})
									</div>
									<div style={{ padding: "0.25rem" }}>
										<FaShippingFast
											size={15}
											color="#2196F3"
											title="Delivery"
										/>
										<strong>Delivery:</strong> ({req.delivery.latitude},{" "}
										{req.delivery.longitude})
									</div>
								</li>
							))}
						</ul>

						{editingTourId === tour.id && editedTour && (
							<div
								style={{
									display: "flex",
									justifyContent: "center",
									gap: "1rem",
									marginTop: "1rem",
								}}
							>
								<button
									onClick={(e) => {
										e.stopPropagation();
										handleAddDelivery(tour.id); // Call the handler to add a delivery
										const updatedTours = deleteTourWithFewestRequestsById(tours, tour.id);
										setTours(updatedTours);
										setEditingTourId(null);
										setEditedTour(null);
										
									}}
									style={{
										backgroundColor: "#2196F3",
										color: "white",
										padding: "0.3rem 0.8rem",
										fontSize: "0.9rem",
										border: "none",
										borderRadius: "0.5rem",
										cursor: "pointer",
									}}
								>
									Add Delivery
								</button>
							</div>
						)}

						{editingTourId === tour.id && editedTour && (
							<div
								style={{
									display: "flex",
									justifyContent: "center",
									gap: "1rem",
									marginTop: "1rem",
								}}
							>
								<button
									onClick={(e) => {
										e.stopPropagation();
										handleValidateEditing(
											editedTour,
											setEditedTour,
											setEditingTourId
										);
									}}
									style={{
										backgroundColor: "#336659",
										color: "white",
										padding: "0.3rem 0.8rem",
										fontSize: "0.9rem",
										border: "none",
										borderRadius: "0.5rem",
										cursor: "pointer",
									}}
								>
									Validate Editing
								</button>
							</div>
						)}

						{/* Bouton d'export XML */}
						<button
							onClick={() => {
								const xmlContent = generateDeliveryXML(tour);
								const blob = new Blob([xmlContent], {
									type: "application/xml",
								});
								const link = document.createElement("a");
								link.href = URL.createObjectURL(blob);
								link.download = `tour_${indexTour + 1}.xml`;
								link.click();
							}}
							style={{
								backgroundColor: "#4CAF50",
								color: "white",
								padding: "0.3rem 0.8rem",
								fontSize: "0.9rem",
								border: "none",
								borderRadius: "0.5rem",
								cursor: "pointer",
								display: "block",
								marginTop: "1rem",
								marginLeft: "auto",
								marginRight: "auto",
							}}
						>
							Export this tour as an XML file
						</button>
					</li>
				))}
			</ul>
		</div>
	);
};

export default DeliveryPlanner;
import axios from "axios";

const API_URL = "http://localhost:8080";

const apiClient = axios.create({
	baseURL: API_URL,
	headers: {
		"Content-Type": "application/json",
	},
});

// Envoyer une requête de livraison
export async function sendRequestToBackend(payload) {
	console.log("Payload to backend:", payload);

	try {
		const response = await apiClient.post(
			`/city-map/optimize-sequence`,
			payload
		);
		console.log("Response from backend:", response.data);
		return response.data;
	} catch (error) {
		console.error(
			"Error sending request to backend:",
			error.response?.data || error.message
		);
		throw error;
	}
}

// Récupérer les intersections
export async function fetchIntersections(setIntersections) {
	try {
		const response = await fetch("http://localhost:8080/city-map/loadmap");
		if (!response.ok) {
			throw new Error("Failed to load city map");
		}
		const data = await response.json();
		setIntersections(data.intersections || []); // Set intersections
	} catch (error) {
		console.error("Error fetching city map:", error);
		setIntersections([]); // Fallback to empty list
	}
}

//Récupérer la map
export async function fetchMap(fileContent) {
    try {
        const response = await apiClient.post(`/city-map/loadmap-content`, fileContent, {
            headers: {
                "Content-Type": "application/xml",
            },
        });
        return response.data;
    } catch (error) {
        console.error("Error fetching map:", error.response?.data || error.message);
        throw error;
    }
}

export async function importXMLFile(filePath) {
	try {
		// Envoie le nom du fichier via une requête GET
		const response = await apiClient.get(`city-map/import/load-tour-from-xml`, {
			params: { filePath }, // Ajouter le paramètre filePath dans l'URL
		});
		console.log("File successfully sent to backend:", response.data);
		return response.data;
	} catch (error) {
		console.error(
			"Error sending file name:",
			error.response?.data || error.message
		);
		throw error;
	}
}

// Récupérer les coursiers
export async function getCouriers() {
    try {
        const response = await apiClient.get(`/data/get/allcourier`);
        return response.data; 
    } catch (error) {
        console.error("Error fetching couriers:", error.response?.data || error.message);
        throw error; 
    }
}

export async function uploadXMLContent(xmlContent) {
    try {
        const response = await apiClient.post(`city-map/import/load-tour-from-xml-content`, xmlContent, {
            headers: {
                "Content-Type": "application/xml",
            },
        });
        console.log("XML content successfully sent to backend:", response.data);
        return response.data;
    } catch (error) {
        console.error(
            "Error uploading XML content:",
            error.response?.data || error.message
        );
        throw error;
    }
}

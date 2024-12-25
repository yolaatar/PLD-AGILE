// utils.js
export function generateDeliveryXML(tour) {
	const xmlHeader = `<?xml version="1.0" encoding="UTF-8"?>`;
	const entrepot = `<entrepot adresse="${tour.warehouse.id}" heureDepart="08:00"/>`;

	const livraisons = tour.requests
		.map(
			(req) =>
				`<livraison adresseEnlevement="${req.pickup.id}" adresseLivraison="${req.delivery.id}"/>`
		)
		.join("\n");

	return `${xmlHeader}\n<demandeDeLivraisons>\n${entrepot}\n${livraisons}\n</demandeDeLivraisons>`;
}

export const generateUniqueId = () => {
	return `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
};

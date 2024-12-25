# Application de Planification de Livraisons

## Présentation
Cette application full stack vise à optimiser les tournées de livraison en tenant compte de diverses contraintes (temps de livraison, temps de ramassage, etc.). Elle combine une interface React + Vite et un backend robuste en Spring Boot, soutenu par une base de données MySQL.

## Fonctionnalités Réalisées
### Gestion des couriers et des demandes
- Création et modification des demandes de livraison (points de ramassage et de dépôt).
- Validation dynamique des contraintes : chaque point de ramassage doit précéder son point de dépôt.
- Gestion multi-couriers avec ajustements en temps réel selon les contraintes définies.

### Optimisation des tournées
- Calcul automatique des itinéraires optimaux, intégrant les contraintes :
  - Temps maximum entre ramassage et livraison.
  - Durées spécifiques pour le ramassage et la livraison.
- Affichage des trajets optimisés directement sur une carte interactive.
- Support pour plusieurs couriers avec des contraintes temporelles ajustables.

### Chargement et affichage des cartes
- Import de cartes depuis des fichiers XML.
- Visualisation interactive des intersections, rues, entrepôts, et trajets optimisés.

### Sauvegarde et restauration
- Enregistrement des tournées complètes dans une base de données ou un fichier XML.
- Restauration des données et rechargement des cartes.
- Gestion des erreurs de sauvegarde avec retour utilisateur.

## Guide d'Installation et d'Exécution
### Prérequis
- Node.js pour le frontend.
- Java 21+ pour le backend.
- MySQL pour la base de données.
- Maven pour gérer les dépendances backend.

### Étapes d'installation
#### Recommendations 
- Utilisez les IDEs suivantes pour un lancement/configuration plus facile de l’application : 
- Visual Studio Code pour le frontend.
- IntelliJ IDEA pour le backend.

#### Frontend
1. Naviguez dans le dossier du frontend :
   cd frontend
2. Installez les dépendances :
   npm install
3. Lancez le serveur de développement :
   npm run dev

#### Backend
1. Naviguez dans le dossier du backend :
   cd backend
2. Connectez-vous à votre interface MySQL (comme MySQL Workbench, phpMyAdmin ou en ligne de commande) et exécutez la commande suivante :
   CREATE DATABASE agile;
3. Configurez les accès à la base de données dans `resources/application.yaml` 
et changez les identifiants selon vos paramètres MYSQL :
   spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agile
    username: root # Remplacez par votre nom d'utilisateur MySQL
    password: root # Remplacez par votre mot de passe MySQL
  jpa:
    hibernate:
      ddl-auto: update # Peut être `create`, `update` ou `validate` selon vos besoins
4. Assurez-vous que Maven est installé sur votre système. Exécutez la commande suivante pour télécharger les dépendances :
   mvn clean install
5.1 Lancez le backend avec Maven :
   mvn spring-boot:run
5.2 Lancez le backend avec IntelliJ : 
    Trouvez le fichier src/main/java/com/example/DemoApplication.java et cliquez sur le bouton run intégré à IntelliJ pour executer le fichier

### Accès à l'application
- Ouvrez votre navigateur et accédez au frontend : http://localhost:5173
- Les appels backend sont disponibles via http://localhost:8080
- Vous pouvez tester les différents appels API depuis POSTMAN avec le lien suivant : 
https://app.getpostman.com/join-team?invite_code=f8400535894f0678612b691e3c55b1977a37490b52c535025db0cb0e354d2931&target_code=275f783ba686e6d0d821ae3eec5fb4c9

## Contributeurs
- Développement Frontend : React + Vite.
- Développement Backend : Java Spring Boot.
- Gestion de la base de données : MySQL.

## Améliorations Futures
- Ajout du suivi en temps réel des livraisons.
- Support pour des entrepôts multiples et des couriers supplémentaires.
- Intégration avec des cartes en temps réel via des services tiers (Google Maps, etc.).

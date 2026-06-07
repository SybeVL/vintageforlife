# CODEBASE_INDEX.md

Index van alle belangrijke bestanden in de VintageForLife Routeplanner.

---

## UI Layer

### `app/src/main/java/com/vintage4life/routeplanner/ui/RoutePlannerActivity.kt`
**Verantwoordelijkheid:**
Entry point van de app. Vraagt locatiepermissies, initialiseert de ViewModel met de Mapbox-token en start de Compose UI.

**Afhankelijkheden:**
- `RoutePlannerViewModel`
- `RoutePlannerScreen`
- Android `ActivityResultContracts`

---

### `app/src/main/java/com/vintage4life/routeplanner/ui/RoutePlannerScreen.kt`
**Verantwoordelijkheid:**
Volledige scherm-UI: Mapbox-kaart met GPS-puck, bottom panel met invoervelden, stoplijst, optimalisatieknoppen en routesamenvatting. Delegeert alle logica naar de ViewModel.

**Afhankelijkheden:**
- `RoutePlannerViewModel`
- `MapAnnotations`
- `OptimizationCriteria`
- `Location`
- Android `Geocoder`
- Mapbox Compose SDK

---

### `app/src/main/java/com/vintage4life/routeplanner/ui/MapAnnotations.kt`
**Verantwoordelijkheid:**
Tekent de routelijn (GeoJSON LineLayer) en genummerde pins (PointAnnotationManager) op de Mapbox-kaart. Geïsoleerd van de UI voor herbruikbaarheid en testbaarheid.

**Afhankelijkheden:**
- Mapbox Maps SDK (`MapView`, `PointAnnotationManager`)
- `Location`

---

## ViewModel Layer

### `app/src/main/java/com/vintage4life/routeplanner/viewmodel/RoutePlannerViewModel.kt`
**Verantwoordelijkheid:**
Beheert alle UI-state via `StateFlow`. Coördineert route-optimalisatie (via `RoutePlannerService`) en het ophalen van weggeometrie (via `MapboxDirectionsClient`).

**Afhankelijkheden:**
- `RoutePlannerService`
- `MapboxDirectionsClient`
- `Location`, `Route`, `OptimizationCriteria`

**State:**
- `stopList` — lijst van toegevoegde stops
- `routeResult` — berekende geoptimaliseerde route
- `routeGeometry` — wegcoördinaten van Mapbox Directions
- `isLoading` — loading-indicator tijdens berekening
- `errorMessage` — éénmalige foutmelding

---

## Service Layer

### `app/src/main/java/com/vintage4life/routeplanner/service/RoutePlannerService.kt`
**Verantwoordelijkheid:**
Orchestreert de TSP-optimalisatie: bouwt de afstandsmatrix, draait NearestNeighbor + TwoOpt, en berekent totale afstand en reistijd.

**Afhankelijkheden:**
- `HaversineCalculator`
- `NearestNeighborAlgorithm`
- `TwoOptAlgorithm`
- `DistanceMatrix`
- `Location`, `Route`, `OptimizationCriteria`

---

## Algorithm Layer

### `app/src/main/java/com/vintage4life/routeplanner/algorithm/TSPAlgorithm.kt`
**Verantwoordelijkheid:**
Strategy interface voor TSP-algoritmen. Stelt implementaties in staat uitwisselbaar te zijn.

---

### `app/src/main/java/com/vintage4life/routeplanner/algorithm/NearestNeighborAlgorithm.kt`
**Verantwoordelijkheid:**
Greedy constructie-heuristiek. Berekent een eerste geldige route door altijd de dichtstbijzijnde onbezochte stop te kiezen. Complexiteit: O(n²).

**Afhankelijkheden:**
- `TSPAlgorithm`, `DistanceMatrix`

---

### `app/src/main/java/com/vintage4life/routeplanner/algorithm/TwoOptAlgorithm.kt`
**Verantwoordelijkheid:**
Lokale verbeteringsalgoritme. Verbetert een bestaande route door segment-omkeringen die de totale afstand verkleinen. Complexiteit: O(n²) per iteratie.

**Afhankelijkheden:**
- `TSPAlgorithm`, `DistanceMatrix`

---

## Distance Layer

### `app/src/main/java/com/vintage4life/routeplanner/distance/DistanceCalculator.kt`
**Verantwoordelijkheid:**
Strategy interface voor afstandsberekeningen tussen twee `Location`-objecten.

---

### `app/src/main/java/com/vintage4life/routeplanner/distance/HaversineCalculator.kt`
**Verantwoordelijkheid:**
Berekent de orthodromische (luchtvogel) afstand tussen twee GPS-coördinaten via de Haversine-formule. Retourneert kilometers.

**Afhankelijkheden:**
- `DistanceCalculator`, `Location`

---

### `app/src/main/java/com/vintage4life/routeplanner/distance/DistanceMatrix.kt`
**Verantwoordelijkheid:**
Vooraf berekende n×n symmetrische afstandsmatrix. Bouwkosten O(n²), opzoekkosten O(1).

**Afhankelijkheden:**
- `DistanceCalculator`, `Location`

---

### `app/src/main/java/com/vintage4life/routeplanner/distance/MapboxDirectionsClient.kt`
**Verantwoordelijkheid:**
Haalt echte weggeometrie op via de Mapbox Directions REST API. Retourneert een lijst van [lon, lat]-coördinatenparen. Blokkerende HTTP-aanroep; altijd aanroepen vanuit een achtergrondthread.

**Afhankelijkheden:**
- `Location`
- Mapbox Directions API (REST)

---

## Model Layer

### `app/src/main/java/com/vintage4life/routeplanner/model/Location.kt`
**Verantwoordelijkheid:**
Datamodel voor een geografische stop: id, naam, adres, latitude, longitude.

---

### `app/src/main/java/com/vintage4life/routeplanner/model/Route.kt`
**Verantwoordelijkheid:**
Datamodel voor een berekende route: geordende stops, totale afstand (km), geschatte reistijd (min), gebruikte criteria.

---

### `app/src/main/java/com/vintage4life/routeplanner/model/OptimizationCriteria.kt`
**Verantwoordelijkheid:**
Enum voor optimalisatiecriteria: `DISTANCE`, `TIME`, `SUSTAINABILITY`.

---

### `app/src/main/java/com/vintage4life/routeplanner/model/User.kt`
**Verantwoordelijkheid:**
Basisklasse voor gebruikers met id, naam, e-mail en rol.

---

### `app/src/main/java/com/vintage4life/routeplanner/model/Role.kt`
**Verantwoordelijkheid:**
Enum voor gebruikersrollen: `CHAUFFEUR`, `ADMINISTRATOR`.

---

### `app/src/main/java/com/vintage4life/routeplanner/model/Chauffeur.kt`
**Verantwoordelijkheid:**
Subklasse van `User` voor chauffeurs, met rijbewijsnummer.

---

### `app/src/main/java/com/vintage4life/routeplanner/model/Administrator.kt`
**Verantwoordelijkheid:**
Subklasse van `User` voor beheerders.

---

### `app/src/main/java/com/vintage4life/routeplanner/model/RouteAssignment.kt`
**Verantwoordelijkheid:**
Koppelt een route aan een chauffeur met bezorgdatum en status.

---

### `app/src/main/java/com/vintage4life/routeplanner/model/AssignmentStatus.kt`
**Verantwoordelijkheid:**
Enum voor toewijzingsstatus: `PENDING`, `ACTIVE`, `COMPLETED`, `CANCELLED`.

---

## Repository Layer

### `app/src/main/java/com/vintage4life/routeplanner/repository/RouteRepository.kt`
**Verantwoordelijkheid:**
In-memory opslag van berekende routes. Vervangbaar door Room-database voor productie.

---

## Resources

### `app/src/main/res/values/strings.xml`
Bevat `app_name` en `mapbox_access_token` (public token; de echte waarde staat in `local.properties`).

### `app/src/main/AndroidManifest.xml`
Declareert `RoutePlannerActivity`, internetpermissie en locatiepermissies.

### `local.properties` *(niet in git)*
Bevat `MAPBOX_ACCESS_TOKEN` (public) en `MAPBOX_DOWNLOADS_TOKEN` (secret).

# FUNCTIONALITY_MAP.md

Overzicht van alle hoofdfunctionaliteiten van de VintageForLife Routeplanner app.

---

### 1. Locatiepermissies

**Beschrijving:**
Vraagt bij het opstarten toestemming aan de gebruiker voor GPS-toegang (fine en coarse location).

**Bestanden:**
- `ui/RoutePlannerActivity.kt`

**Packages:**
- `com.vintage4life.routeplanner.ui`

**Android API:**
- `ActivityResultContracts.RequestMultiplePermissions`
- `Manifest.permission.ACCESS_FINE_LOCATION`
- `Manifest.permission.ACCESS_COARSE_LOCATION`

---

### 2. Live GPS-tracking op de kaart

**Beschrijving:**
Toont de huidige locatie van de gebruiker als een rijdende puck op de kaart.
Centreert de kaart automatisch op de eerste GPS-fix.

**Bestanden:**
- `ui/RoutePlannerScreen.kt`

**Packages:**
- `com.vintage4life.routeplanner.ui`

**Mapbox plugins:**
- `locationcomponent` (`createDefault2DPuck`, `OnIndicatorPositionChangedListener`)
- `PuckBearing.COURSE`

---

### 3. Kaart klikken → adres invullen

**Beschrijving:**
Tikt de gebruiker op de kaart, dan wordt het adres van die locatie via reverse-geocoding
automatisch ingevuld in het adresveld.

**Bestanden:**
- `ui/RoutePlannerScreen.kt`

**Packages:**
- `com.vintage4life.routeplanner.ui`

**Gebruikt:**
- Android `Geocoder` (reverse geocoding via `getFromLocation`)
- Mapbox `onMapClickListener`

---

### 4. Live adreslabel op huidige positie

**Beschrijving:**
Toont een tekstlabel met het huidige adres boven de GPS-puck.
Verdwijnt zodra een route actief is.

**Bestanden:**
- `ui/RoutePlannerScreen.kt`

**Packages:**
- `com.vintage4life.routeplanner.ui`

**Gebruikt:**
- Android `Geocoder`
- Mapbox `PointAnnotationManager` (aparte manager voor het adreslabel)

---

### 5. Stop toevoegen via adres

**Beschrijving:**
De gebruiker vult een naam (optioneel) en adres in. Het adres wordt via geocoding
omgezet naar coördinaten en als stop aan de lijst toegevoegd.

**Bestanden:**
- `ui/RoutePlannerScreen.kt`
- `viewmodel/RoutePlannerViewModel.kt`
- `model/Location.kt`

**Packages:**
- `com.vintage4life.routeplanner.ui`
- `com.vintage4life.routeplanner.viewmodel`
- `com.vintage4life.routeplanner.model`

**Gebruikt:**
- Android `Geocoder` (`getFromLocationName`)

---

### 6. Stop verwijderen

**Beschrijving:**
Verwijdert een stop uit de lijst op basis van index. Reset de actieve route.

**Bestanden:**
- `ui/RoutePlannerScreen.kt`
- `viewmodel/RoutePlannerViewModel.kt`

---

### 7. Route-optimalisatie (TSP)

**Beschrijving:**
Berekent de kortste/snelste volgorde van stops via het Travelling Salesman Problem.

**Flow:**
1. Bouw een afstandsmatrix via `HaversineCalculator` — O(n²)
2. Bereken een initiële route via `NearestNeighborAlgorithm` — O(n²)
3. Verbeter de route via `TwoOptAlgorithm` — O(n²–n³)
4. Zet de indices terug naar `Location`-objecten

**Bestanden:**
- `service/RoutePlannerService.kt`
- `algorithm/NearestNeighborAlgorithm.kt`
- `algorithm/TwoOptAlgorithm.kt`
- `algorithm/TSPAlgorithm.kt`
- `distance/DistanceMatrix.kt`
- `distance/HaversineCalculator.kt`
- `model/Location.kt`
- `model/Route.kt`

**Packages:**
- `com.vintage4life.routeplanner.service`
- `com.vintage4life.routeplanner.algorithm`
- `com.vintage4life.routeplanner.distance`

**Optimalisatiecriteria:**
- `DISTANCE` — kortste afstand (km)
- `TIME` — snelste reistijd (min, schatting op 50 km/u)
- `SUSTAINABILITY` — zelfde als distance in huidige implementatie (uitbreidbaar)

---

### 8. Weggeometrie ophalen (Mapbox Directions)

**Beschrijving:**
Na route-optimalisatie worden de echte wegcoördinaten opgehaald via de Mapbox Directions API.
Wordt gebruikt om een nauwkeurige routelijn op de kaart te tekenen.

**Bestanden:**
- `distance/MapboxDirectionsClient.kt`
- `viewmodel/RoutePlannerViewModel.kt`

**Packages:**
- `com.vintage4life.routeplanner.distance`

**API:**
- `GET https://api.mapbox.com/directions/v5/mapbox/driving/{coords}?geometries=geojson`

---

### 9. Route tekenen op de kaart

**Beschrijving:**
Tekent een groene routelijn en genummerde rode pins op de kaart na route-optimalisatie.
Gebruikt echte weggeometrie wanneer beschikbaar; valt terug op rechte lijnen.

**Bestanden:**
- `ui/MapAnnotations.kt`
- `ui/RoutePlannerScreen.kt`

**Packages:**
- `com.vintage4life.routeplanner.ui`

**Mapbox:**
- `GeoJsonSource`, `LineLayer` voor de routelijn
- `PointAnnotationManager` voor de genummerde pins

---

### 10. Optimalisatiecriterium kiezen

**Beschrijving:**
De gebruiker kiest via FilterChips welk criterium (afstand, tijd, CO₂) gebruikt wordt
voor de route-optimalisatie.

**Bestanden:**
- `ui/RoutePlannerScreen.kt`
- `model/OptimizationCriteria.kt`

---

### 11. Route wissen

**Beschrijving:**
Reset alle stops, de actieve route, de weggeometrie en eventuele foutmeldingen.

**Bestanden:**
- `ui/RoutePlannerScreen.kt`
- `viewmodel/RoutePlannerViewModel.kt`

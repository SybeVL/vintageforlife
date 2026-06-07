# FUNCTIONAL_PARITY_REPORT.md

Validatierapport van de UML-conformiteitsrefactor van de VintageForLife Routeplanner.

---

## 1. Behouden functionaliteiten

Alle onderstaande functionaliteiten zijn ongewijzigd behouden na de refactor.

| Functionaliteit | Status |
|---|---|
| Locatiepermissies aanvragen bij opstarten | ✅ Behouden |
| Live GPS-tracking met puck op kaart | ✅ Behouden |
| Kaart aantikken → adres automatisch invullen | ✅ Behouden |
| Live adreslabel op huidige GPS-positie | ✅ Behouden |
| Stop toevoegen via Android Geocoder | ✅ Behouden |
| Stop verwijderen uit lijst | ✅ Behouden |
| TSP-optimalisatie (NearestNeighbor + TwoOpt) | ✅ Behouden |
| Weggeometrie ophalen via Mapbox Directions API | ✅ Behouden |
| Routelijn tekenen op kaart (GeoJSON LineLayer) | ✅ Behouden |
| Genummerde pins per stop op kaart | ✅ Behouden |
| Optimalisatiecriterium kiezen (Distance/Time/CO₂) | ✅ Behouden |
| Loading-indicator tijdens berekening | ✅ Behouden |
| Alles wissen (stops + route + kaart) | ✅ Behouden |
| Routesamenvatting tonen (km + min) | ✅ Behouden |
| Foutmeldingen tonen bij validatiefouten | ✅ Behouden |

---

## 2. Verplaatste componenten

| Component | Oude locatie/signatuur | Nieuwe locatie/signatuur | Reden |
|---|---|---|---|
| `TSPAlgorithm.solve()` | `solve(DistanceMatrix): List<Int>` | `solve(List<Location>): Route` | UML-conform: interface werkt op domeinniveau |
| `NearestNeighborAlgorithm.solve()` | `solve(DistanceMatrix): List<Int>` | `solve(List<Location>): Route` | Aangepast aan nieuwe interface |
| `TwoOptAlgorithm.solve()` | `solve(DistanceMatrix): List<Int>` | `solve(List<Location>): Route` | Aangepast aan nieuwe interface |
| `TwoOptAlgorithm.reverseSegment()` | `reverseSegment(route, from, to)` | `reverse(route, from, to)` | Hernoemd conform UML |
| `RoutePlannerService.optimizeRoute()` | `optimizeRoute(stops, criteria)` | `planRoute(stops, criteria)` | Hernoemd conform UML |
| `RoutePlannerViewModel.solveRoute()` | `solveRoute(criteria)` | `planRoute(criteria)` | Hernoemd conform UML |
| `RoutePlannerViewModel.addStop()` | `addStop(name, address, lat, lon)` | `addStop(location: Location)` | Conform UML: accepteert Location-object |
| `RoutePlannerViewModel.stopList` | `stopList: StateFlow<List<Location>>` | `stops: StateFlow<List<Location>>` | Hernoemd conform UML |
| `RoutePlannerViewModel.routeResult` | `routeResult: StateFlow<Route?>` | `route: StateFlow<Route?>` | Hernoemd conform UML |
| `RoutePlannerViewModel.errorMessage` | `errorMessage: StateFlow<String?>` | `error: StateFlow<String?>` | Hernoemd conform UML |
| `Route.stops` | `stops: List<Location>` | `locations: List<Location>` | Hernoemd conform UML |
| `Route.totalDistanceKm` | `totalDistanceKm: Double` | `totalDistance: Double` | Hernoemd conform UML |
| `User` | `open class User` | `abstract class User` | Conform UML: abstract |

---

## 3. Nieuwe componenten

### `Location.distanceTo()`
- **Doel:** Afstandsberekening direct op het model, conform UML
- **Waarom noodzakelijk:** UML schrijft deze methode expliciet voor op `Location`
- **Impact:** Geen — addatieve methode, geen gedragsverandering

### `DistanceMatrix.getDistance()` en `getSize()`
- **Doel:** Methode-aliassen conform UML-naamgeving
- **Waarom noodzakelijk:** UML toont `getDistance(i,j)` en `getSize()` expliciet
- **Impact:** Geen — wrappers om bestaande `distance()` en `size()` methoden

### `DistanceMatrix.build()` (companion object)
- **Doel:** Statische factory methode conform UML
- **Waarom noodzakelijk:** UML toont `build(List<Location>)` als methode op DistanceMatrix
- **Impact:** Geen — addatieve factory, bestaande constructor blijft intact

### `RouteAssignment.complete()` en `cancel()`
- **Doel:** Status-transitiemethoden conform UML
- **Waarom noodzakelijk:** UML schrijft `complete()` en `cancel()` expliciet voor
- **Impact:** Geen — addatieve methoden op een ongebruikt model

### `RoutePlannerService.setAlgorithm()`
- **Doel:** Runtime-uitwisseling van TSP-algoritme conform UML
- **Waarom noodzakelijk:** UML toont `setAlgorithm(TSPAlgorithm)` expliciet
- **Impact:** Geen — addatieve methode

### `Administrator.assignRoute()` en `createRoute()`
- **Doel:** Beheerdermethoden conform UML
- **Waarom noodzakelijk:** UML toont beide methoden expliciet op `Administrator`
- **Impact:** Geen — addatieve methoden op een nog niet in de UI gebruikte klasse

### `Chauffeur.vehicleId`
- **Doel:** Voertuig-ID conform UML
- **Waarom noodzakelijk:** UML toont `vehicleId: String` op `Chauffeur`
- **Impact:** Geen — optioneel veld met default lege string

### `NearestNeighborAlgorithm.solveIndices()` en `buildRoute()`
- **Doel:** Interne hulpmethoden voor hergebruik door `TwoOptAlgorithm`
- **Waarom noodzakelijk:** `TwoOptAlgorithm` hergebruikt de greedy-indices zonder dubbele matrixopbouw
- **Impact:** Geen — implementatiedetail; niet zichtbaar via de `TSPAlgorithm`-interface

---

## 4. UML-conformiteit per module

| UML-component | Geïmplementeerde klasse | Status | Opmerkingen |
|---|---|---|---|
| `<<interface>> TSPAlgorithm` | `algorithm/TSPAlgorithm.kt` | ✅ Volledig conform | Signatuur `solve(List<Location>): Route` |
| `NearestNeighborAlgorithm` | `algorithm/NearestNeighborAlgorithm.kt` | ✅ Volledig conform | `solve()` + `buildRoute()` |
| `TwoOptAlgorithm` | `algorithm/TwoOptAlgorithm.kt` | ✅ Volledig conform | `solve()` + `reverse()` + NearestNeighbor-dep. |
| `<<enum>> OptimizationCriteria` | `model/OptimizationCriteria.kt` | ✅ Volledig conform | DISTANCE, TIME, SUSTAINABILITY |
| `RoutePlannerService` | `service/RoutePlannerService.kt` | ✅ Volledig conform | `planRoute()` + `setAlgorithm()` + beide velden |
| `<<interface>> DistanceCalculator` | `distance/DistanceCalculator.kt` | ✅ Volledig conform | `calculate(a,b): double` |
| `HaversineCalculator` | `distance/HaversineCalculator.kt` | ✅ Volledig conform | `calculate(a,b): double` |
| `DistanceMatrix` | `distance/DistanceMatrix.kt` | ✅ Volledig conform | `getDistance()`, `getSize()`, `build()` toegevoegd |
| `Location` | `model/Location.kt` | ⚠️ Gedeeltelijk conform | Zie afwijking §5.1 |
| `Route` | `model/Route.kt` | ✅ Volledig conform | `locations`, `totalDistance`, `addLocation()`, `getTotalDistance()` |
| `RouteAssignment` | `model/RouteAssignment.kt` | ✅ Volledig conform | `complete()` + `cancel()` |
| `<<enum>> AssignmentStatus` | `model/AssignmentStatus.kt` | ✅ Volledig conform | Alle 4 waarden |
| `<<abstract>> User` | `model/User.kt` | ✅ Volledig conform | abstract + `getRole()` |
| `Role` | `model/Role.kt` | ⚠️ Gedeeltelijk conform | Zie afwijking §5.2 |
| `Chauffeur` | `model/Chauffeur.kt` | ✅ Volledig conform | `licenseNumber` + `vehicleId` |
| `Administrator` | `model/Administrator.kt` | ✅ Volledig conform | `assignRoute()` + `createRoute()` |
| `RoutePlannerViewModel` | `viewmodel/RoutePlannerViewModel.kt` | ✅ Volledig conform | `stops`, `route`, `error`, `addStop(location)`, `removeStop(location)`, `planRoute()` |

---

## 5. Afwijkingen van UML

### 5.1 `Location.x, y` vs `latitude, longitude`
- **UML:** `- x, y: double`
- **Implementatie:** `latitude: Double`, `longitude: Double`
- **Reden:** De Android `Geocoder` en Mapbox SDK leveren coördinaten in WGS-84 formaat als latitude/longitude. Hernoemen naar x/y zou alle GPS- en kaartfunctionaliteit breken.
- **Functionele impact:** Geen — semantisch equivalent; latitude = y, longitude = x
- **Beslissing:** Behouden met documentatie

### 5.2 `Role` als `enum` vs klasse met `role: String`
- **UML:** `Role { + role: String }` (toont een klasse met string-veld)
- **Implementatie:** `enum class Role { CHAUFFEUR, ADMINISTRATOR }`
- **Reden:** Een enum biedt type-veiligheid, exhaustive when-expressies en is idiomatisch Kotlin. Een klasse met string-veld vereist runtime-validatie en is foutgevoelig.
- **Functionele impact:** Geen
- **Beslissing:** Enum behouden conform best practices

### 5.3 `RoutePlannerViewModel.planRoute()` parameter
- **UML:** `planRoute(List<Location>)` — neemt een expliciet lijst mee
- **Implementatie:** `planRoute(criteria: OptimizationCriteria)` — gebruikt intern `_stops`
- **Reden:** De ViewModel beheert de stoplijst al intern als state. Een extra lijstparameter zou leiden tot inconsistentie tussen de interne state en de meegegeven lijst. De `criteria` parameter is noodzakelijk voor de bestaande optimalisatiecriterium-keuze in de UI.
- **Functionele impact:** Geen
- **Beslissing:** Intern `_stops` gebruiken; criteria-parameter toegevoegd

### 5.4 Extra velden in `RoutePlannerViewModel`
- **UML:** toont `stops`, `route`, `error`
- **Implementatie:** ook `routeGeometry` en `isLoading`
- **Reden:** `routeGeometry` is noodzakelijk voor de kaartvisualisatie (Mapbox lijnlaag). `isLoading` is noodzakelijk voor de loading-indicator in de UI.
- **Functionele impact:** Geen nieuwe functionaliteit — ondersteunende state voor bestaand gedrag

### 5.5 Extra velden in `Route`
- **UML:** toont `locations` en `totalDistance`
- **Implementatie:** ook `estimatedTimeMin` en `criteria`
- **Reden:** `estimatedTimeMin` toont de geschatte reistijd in de UI. `criteria` slaat op welk criterium gebruikt is.
- **Functionele impact:** Geen nieuwe functionaliteit

---

## 6. Succescriteria-evaluatie

| Criterium | Status |
|---|---|
| Applicatie compileert | ✅ |
| Bestaande functionaliteit behouden | ✅ |
| UML-structuur gevolgd | ✅ (met gedocumenteerde afwijkingen) |
| Code modulair opgezet | ✅ |
| Geen nieuwe functionaliteit zonder noodzaak | ✅ |
| Alle wijzigingen gedocumenteerd | ✅ |

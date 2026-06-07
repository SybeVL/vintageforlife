# ARCHITECTURE.md

Beschrijving van de architectuur van de VintageForLife Routeplanner.

---

## Overzicht

De app volgt een **Clean Architecture**-aanpak gecombineerd met **feature-based package-indeling**.
Alle code zit momenteel in één `:app` Gradle-module, maar is intern opgesplitst in duidelijk afgebakende lagen.

```
:app
 └── com.vintage4life.routeplanner
      ├── algorithm/       — TSP-algoritmen (pure Kotlin, geen Android-afhankelijkheden)
      ├── distance/        — Afstandsberekening + Mapbox Directions client
      ├── model/           — Domeinmodellen (data classes, enums)
      ├── repository/      — Data-toegangslaag (in-memory, uitbreidbaar naar Room)
      ├── service/         — Businesslogica (route-orchestratie)
      ├── ui/              — Presentatielaag (Activity, Composables, MapAnnotations)
      └── viewmodel/       — UI-state management (ViewModel + StateFlow)
```

---

## Lagen

### Presentatielaag (`ui/`)
- **`RoutePlannerActivity`** — Entry point, permissies, ViewModel-initialisatie
- **`RoutePlannerScreen`** — Compose UI: kaart + bottom panel
- **`MapAnnotations`** — Kaarttekenlogica (pins, routelijn) geïsoleerd van Compose

**Regels:**
- Geen directe businesslogica
- Communiceert uitsluitend via ViewModel
- Android/Mapbox SDK afhankelijkheden zijn geïsoleerd in deze laag

### ViewModel-laag (`viewmodel/`)
- **`RoutePlannerViewModel`** — Beheert UI-state, coördineert Service en DirectionsClient

**Regels:**
- Geen UI-imports
- Alle state via `StateFlow`
- Achtergrondwerk via `viewModelScope` + `Dispatchers.IO`

### Servicelaag (`service/`)
- **`RoutePlannerService`** — Orchestreert TSP-optimalisatie

**Regels:**
- Geen Android-afhankelijkheden
- Pure Kotlin, testbaar zonder emulator

### Algoritmeslaag (`algorithm/`)
- **`TSPAlgorithm`** — Strategy interface
- **`NearestNeighborAlgorithm`** — Greedy constructie O(n²)
- **`TwoOptAlgorithm`** — Lokale verbetering O(n²–n³)

**Regels:**
- Pure Kotlin, geen externe afhankelijkheden
- Uitwisselbaar via `TSPAlgorithm` interface

### Afstandslaag (`distance/`)
- **`DistanceCalculator`** — Strategy interface
- **`HaversineCalculator`** — Luchtvogel-afstand (km)
- **`DistanceMatrix`** — O(1) opzoektabel
- **`MapboxDirectionsClient`** — REST-aanroepen naar Mapbox Directions

### Modellaag (`model/`)
- Pure data classes en enums
- Geen logica, geen afhankelijkheden

### Repository-laag (`repository/`)
- **`RouteRepository`** — In-memory opslag
- Interface-klaar voor Room-database migratie

---

## Dataflow

```
Gebruiker
   │
   ▼
RoutePlannerScreen (Compose)
   │  collectAsState()
   ▼
RoutePlannerViewModel
   │  solveRoute()
   ├──► RoutePlannerService.optimizeRoute()
   │         │
   │         ├──► DistanceMatrix (HaversineCalculator)
   │         ├──► NearestNeighborAlgorithm.solve()
   │         └──► TwoOptAlgorithm.improve()
   │
   └──► MapboxDirectionsClient.fetchRouteGeometry()
              │
              └──► Mapbox Directions REST API
```

---

## Dependency Injection

Momenteel via constructor-injectie met default-parameters (geen DI-framework).
Uitbreidbaar naar Hilt wanneer de app groeit.

---

## Toekomstige module-indeling (aanbeveling)

Wanneer de app uitgroeit met chauffeur- en adminbeheer:

```
:app
:core:domain          — model/, algorithm/, distance/
:core:data            — repository/, MapboxDirectionsClient
:core:ui              — gedeelde Compose-componenten
:feature:routeplanner — service/, viewmodel/, ui/
:feature:chauffeurs   — toekomstig chauffeursbeheer
:feature:assignments  — toekomstige routetoewijzingen
```

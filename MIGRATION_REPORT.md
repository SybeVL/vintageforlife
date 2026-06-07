# MIGRATION_REPORT.md

Verslag van de refactor van de VintageForLife Routeplanner PoC naar een professionele modulaire architectuur.

---

## Oude structuur (routeplanner_old_2.zip)

**Package:** `com.example.javaplanner`

```
app/src/main/java/com/example/javaplanner/
├── MainActivity.kt          — Activity + volledige UI + kaartlogica (monolithisch)
├── RouteViewModel.kt        — ViewModel met Mapbox-initialisatie en route-solving
├── MapAnnotations.kt        — Kaarttekenlogica
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

**Problemen met de oude structuur:**
| Probleem | Details |
|---|---|
| God-klasse `MainActivity` | UI, kaartlogica, geocoding en permissieafhandeling in één bestand (330+ regels) |
| Verkeerde package-naam | `com.example.javaplanner` i.p.v. `com.vintage4life.routeplanner` |
| Geen scheiding van verantwoordelijkheden | Business- en presentatielogica door elkaar |
| `OptimisationMode` vs `OptimizationCriteria` | Inconsistente naamgeving |
| `Location` met getters | `getName()`, `getLat()`, `getLon()` i.p.v. Kotlin properties |
| `Route` met getters | `getLocations()` i.p.v. `stops` property |
| Geen repository-laag | Geen persistentie-abstractie |
| Geen model voor gebruikers | Chauffeur/Beheerder niet gemodelleerd |

---

## Nieuwe structuur (na refactor)

**Package:** `com.vintage4life.routeplanner`

```
app/src/main/java/com/vintage4life/routeplanner/
├── algorithm/
│   ├── TSPAlgorithm.kt                 — Strategy interface
│   ├── NearestNeighborAlgorithm.kt     — Greedy constructie
│   └── TwoOptAlgorithm.kt              — Lokale verbetering
├── distance/
│   ├── DistanceCalculator.kt           — Strategy interface
│   ├── HaversineCalculator.kt          — Luchtvogel-afstand
│   ├── DistanceMatrix.kt               — O(1) afstandstabel
│   └── MapboxDirectionsClient.kt       — Weggeometrie via REST API
├── model/
│   ├── Location.kt                     — Geografische stop (data class)
│   ├── Route.kt                        — Berekende route (data class)
│   ├── OptimizationCriteria.kt         — DISTANCE / TIME / SUSTAINABILITY
│   ├── User.kt                         — Basisgebruiker
│   ├── Role.kt                         — CHAUFFEUR / ADMINISTRATOR
│   ├── Chauffeur.kt                    — Chauffeur-gebruiker
│   ├── Administrator.kt                — Beheerder-gebruiker
│   ├── RouteAssignment.kt              — Route–chauffeur-koppeling
│   └── AssignmentStatus.kt             — PENDING / ACTIVE / COMPLETED / CANCELLED
├── repository/
│   └── RouteRepository.kt              — In-memory routeopslag
├── service/
│   └── RoutePlannerService.kt          — TSP-orchestratie
├── ui/
│   ├── RoutePlannerActivity.kt         — Permissies + ViewModel-init
│   ├── RoutePlannerScreen.kt           — Volledige Compose UI
│   └── MapAnnotations.kt              — Kaarttekenlogica
└── viewmodel/
    └── RoutePlannerViewModel.kt        — UI-state + coördinatie
```

---

## Verplaatste bestanden

| Oud bestand | Nieuw bestand | Wijziging |
|---|---|---|
| `MainActivity.kt` | `ui/RoutePlannerActivity.kt` | Gesplitst: alleen permissies + init |
| `MainActivity.kt` | `ui/RoutePlannerScreen.kt` | UI-logica geëxtraheerd naar Composable |
| `RouteViewModel.kt` | `viewmodel/RoutePlannerViewModel.kt` | Hernoemd, `routeGeometry` toegevoegd |
| `MapAnnotations.kt` | `ui/MapAnnotations.kt` | Package gecorrigeerd, Kotlin properties |

---

## Nieuwe bestanden

| Bestand | Reden |
|---|---|
| `algorithm/TSPAlgorithm.kt` | Strategy interface voor uitwisselbaarheid |
| `algorithm/NearestNeighborAlgorithm.kt` | Geëxtraheerde algoritmelaag |
| `algorithm/TwoOptAlgorithm.kt` | Geëxtraheerde algoritmelaag |
| `distance/DistanceCalculator.kt` | Strategy interface |
| `distance/HaversineCalculator.kt` | Pure afstandsberekening |
| `distance/DistanceMatrix.kt` | O(1) opzoektabel |
| `distance/MapboxDirectionsClient.kt` | Weggeometrie-integratie (hernoemd vanuit oud project) |
| `model/Location.kt` | Kotlin data class (vervangt oude getters) |
| `model/Route.kt` | Kotlin data class (vervangt oude getters) |
| `model/OptimizationCriteria.kt` | Hernoemd van `OptimisationMode` |
| `model/User.kt` | Nieuw — gebruikersmodel |
| `model/Role.kt` | Nieuw — rolsysteem |
| `model/Chauffeur.kt` | Nieuw — chauffeurmodel |
| `model/Administrator.kt` | Nieuw — beheerdermodel |
| `model/RouteAssignment.kt` | Nieuw — routetoewijzingsmodel |
| `model/AssignmentStatus.kt` | Nieuw — statustracking |
| `repository/RouteRepository.kt` | Nieuw — data-toegangslaag |
| `service/RoutePlannerService.kt` | Geëxtraheerde servicelaag |

---

## Behouden functionaliteit

| Functionaliteit | Status |
|---|---|
| GPS-tracking met puck op kaart | ✅ Behouden |
| Kaart klikken → adres invullen | ✅ Behouden |
| Live adreslabel op huidige positie | ✅ Behouden |
| Stop toevoegen via geocoding | ✅ Behouden |
| Stop verwijderen | ✅ Behouden |
| TSP-optimalisatie (NearestNeighbor + TwoOpt) | ✅ Behouden |
| Weggeometrie via Mapbox Directions | ✅ Behouden |
| Routelijn tekenen op kaart | ✅ Behouden |
| Genummerde pins per stop | ✅ Behouden |
| Optimalisatiecriterium kiezen | ✅ Behouden |
| Loading-indicator tijdens berekening | ✅ Behouden |
| Route wissen | ✅ Behouden |
| Routesamenvatting (km + min) | ✅ Behouden |

---

## Openstaande verbeterpunten

| Punt | Prioriteit | Beschrijving |
|---|---|---|
| Dependency Injection (Hilt) | Medium | Vervang constructor-injectie door Hilt voor betere testbaarheid |
| Room-database | Medium | Vervang `RouteRepository` in-memory opslag door Room |
| Chauffeur/Admin UI | Laag | Schermen voor gebruikersbeheer en routetoewijzing |
| Unit tests | Hoog | Tests voor `RoutePlannerService`, algoritmen en `DistanceMatrix` |
| Foutafhandeling geocoder | Medium | Toon melding als adres niet gevonden kan worden |
| Mapbox Geocoding API | Laag | Vervang Android `Geocoder` door Mapbox Search API voor consistentere resultaten |
| `SUSTAINABILITY`-criterium | Laag | Huidige implementatie is gelijk aan `DISTANCE`; uitbreiden met CO₂-factor |

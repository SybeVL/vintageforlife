package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

/**
 * Berekent de reistijd tussen twee locaties op basis van afstand en
 * een realistisch snelheidsmodel voor stedelijke bezorging in Nederland.
 *
 * Retourneert uren (zodat de TSP-algoritmen de goedkoopste route vinden).
 * De RoutePlannerService vermenigvuldigt met 60 voor de weergave in minuten.
 *
 * Snelheidsmodel:
 *  - < 2 km  : 30 km/h  (stadscentrum, veel stoplichten)
 *  - 2–15 km : 50 km/h  (stedelijk/ringweg)
 *  - > 15 km : 80 km/h  (snelweg/provinciale weg)
 *
 * Dit geeft het algoritme een zinvolle reden om korte hops samen te voegen
 * en langere segmenten te prefereren, wat een andere volgorde oplevert dan
 * puur op afstand optimaliseren.
 */
class TimeCalculator(
    private val baseCalculator: DistanceCalculator = HaversineCalculator()
) : DistanceCalculator {

    override fun calculate(from: Location, to: Location): Double {
        val distanceKm = baseCalculator.calculate(from, to)

        val speedKmh = when {
            distanceKm < 2.0  -> 30.0
            distanceKm < 15.0 -> 50.0
            else              -> 80.0
        }

        return distanceKm / speedKmh  // uren
    }
}

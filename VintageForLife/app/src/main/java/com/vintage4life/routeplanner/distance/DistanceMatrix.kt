package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

/**
 * Vooraf berekende n×n symmetrische afstandsmatrix.
 * Bouwkosten: O(n²). Opzoekkosten: O(1).
 *
 * Conform UML: heeft [getDistance(i,j)], [getSize()], en statische [build()] factory.
 *
 * @param locations   Geordende lijst van stops
 * @param calculator  Afstandsstrategie voor de bouw van de matrix
 */
class DistanceMatrix(
    private val locations: List<Location>,
    calculator: DistanceCalculator
) {
    private val n = locations.size
    private val matrix: Array<DoubleArray> = Array(n) { DoubleArray(n) }

    init {
        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val d = calculator.calculate(locations[i], locations[j])
                matrix[i][j] = d
                matrix[j][i] = d  // symmetrisch
            }
        }
    }

    /** Retourneert de afstand tussen stop [i] en stop [j] in km. */
    fun distance(i: Int, j: Int): Double = matrix[i][j]

    /** Conform UML: getDistance(i, j): double */
    fun getDistance(i: Int, j: Int): Double = distance(i, j)

    /** Aantal stops in de matrix. */
    fun size(): Int = n

    /** Conform UML: getSize(): int */
    fun getSize(): Int = n

    /** Retourneert de Location op index [i]. */
    fun locationAt(i: Int): Location = locations[i]

    companion object {
        /**
         * Factory methode conform UML: build(List<Location>).
         * Gebruikt HaversineCalculator als standaard.
         */
        fun build(locations: List<Location>): DistanceMatrix =
            DistanceMatrix(locations, com.vintage4life.routeplanner.distance.HaversineCalculator())
    }
}

package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

/**
 * Pre-computed n×n symmetric distance matrix.
 * Build cost: O(n²). Lookup cost: O(1).
 *
 * @param locations  Ordered list of stops
 * @param calculator Distance strategy to use during construction
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
                matrix[j][i] = d  // symmetric
            }
        }
    }

    /** Returns pre-computed distance between stop [i] and stop [j] in km. */
    fun distance(i: Int, j: Int): Double = matrix[i][j]

    /** Number of stops in the matrix. */
    fun size(): Int = n

    /** Returns the Location at index [i]. */
    fun locationAt(i: Int): Location = locations[i]
}

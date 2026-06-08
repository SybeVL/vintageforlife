package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

// symmetrical distance matrix at n x n dimensions. kind of like an excel sheet
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

    // return distance between stop at index [i] and [j]
    fun distance(i: Int, j: Int): Double = matrix[i][j]

    fun getDistance(i: Int, j: Int): Double = distance(i, j)
    fun size(): Int = n

    fun getSize(): Int = n
    fun locationAt(i: Int): Location = locations[i]

    companion object {
        fun build(locations: List<Location>): DistanceMatrix =
            DistanceMatrix(locations, com.vintage4life.routeplanner.distance.HaversineCalculator())
    }
}

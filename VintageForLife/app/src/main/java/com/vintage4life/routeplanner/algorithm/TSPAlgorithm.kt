package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.model.Location

/**
 * Strategy interface for TSP solving algorithms.
 * Implementations return an ordered list of Location indices
 * representing the optimised visit order.
 */
interface TSPAlgorithm {
    /**
     * Compute or improve a route.
     * @param matrix  Pre-built distance matrix
     * @return        Ordered list of location indices
     */
    fun solve(matrix: DistanceMatrix): List<Int>
}

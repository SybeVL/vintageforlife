package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.model.Location
import org.junit.Assert.assertEquals
import org.junit.Test

class NearestNeighborAlgorithmTest {

    /**
     * Mock calculator that returns a fixed distance for testing.
     */
    class MockDistanceCalculator : DistanceCalculator {
        override fun calculate(from: Location, to: Location): Double {
            // Very simple logic for testing: distance is the difference in indices (hypothetically)
            // But we'll just use the "name" to make it predictable
            val val1 = from.name.toIntOrNull() ?: 0
            val val2 = to.name.toIntOrNull() ?: 0
            return kotlin.math.abs(val1 - val2).toDouble()
        }
    }

    @Test
    fun `solve should return a greedy route starting from index 0`() {
        // Arrange
        val locations = listOf(
            Location("id0", "0", "Addr 0", 0.0, 0.0), // Start
            Location("id1", "10", "Addr 10", 0.0, 0.0), // Far
            Location("id2", "2", "Addr 2", 0.0, 0.0),  // Close to 0
            Location("id3", "5", "Addr 5", 0.0, 0.0)   // Medium
        )
        
        val matrix = DistanceMatrix(locations, MockDistanceCalculator())
        val algorithm = NearestNeighborAlgorithm()

        // Act
        val route = algorithm.solve(matrix)

        // Assert
        // Starting at 0:
        // - Distances from 0: to 10 is 10, to 2 is 2, to 5 is 5. Closest is index 2 ("2").
        // - Distances from 2: to 10 is 8, to 5 is 3. Closest is index 3 ("5").
        // - Remaining: index 1 ("10").
        // Expected route: [0, 2, 3, 1]
        val expected = listOf(0, 2, 3, 1)
        assertEquals(expected, route)
    }

    @Test
    fun `solve with two locations`() {
        val locations = listOf(
            Location("A", "A", "Addr A", 0.0, 0.0),
            Location("B", "B", "Addr B", 0.0, 0.0)
        )
        val matrix = DistanceMatrix(locations, object : DistanceCalculator {
            override fun calculate(from: Location, to: Location): Double = 1.0
        })
        val algorithm = NearestNeighborAlgorithm()

        val route = algorithm.solve(matrix)

        assertEquals(listOf(0, 1), route)
    }
}

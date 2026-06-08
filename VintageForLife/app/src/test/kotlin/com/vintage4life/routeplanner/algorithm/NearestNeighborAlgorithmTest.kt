package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.model.Location
import org.junit.Assert.assertEquals
import org.junit.Test

class NearestNeighborAlgorithmTest {

    /**
     * Mock calculator that returns a fixed distance based on the 'name' property
     * treated as a coordinate on a 1D line.    
     */
    private class MockDistanceCalculator : DistanceCalculator {
        override fun calculate(from: Location, to: Location): Double {
            val val1 = from.name.toDoubleOrNull() ?: 0.0
            val val2 = to.name.toDoubleOrNull() ?: 0.0
            return kotlin.math.abs(val1 - val2)
        }
    }

    @Test
    fun `solve should return a greedy route starting from the first location`() {
        // Arrange
        val locations = listOf(
            Location("id0", "0", "Addr 0", 0.0, 0.0),  // Start
            Location("id1", "10", "Addr 10", 0.0, 0.0), // Far
            Location("id2", "2", "Addr 2", 0.0, 0.0),   // Close to 0
            Location("id3", "5", "Addr 5", 0.0, 0.0)    // Medium
        )
        val calculator = MockDistanceCalculator()
        val algorithm = NearestNeighborAlgorithm()

        // Act
        val route = algorithm.solve(locations, calculator)

        // Assert
        // Logic:
        // 1. Start at "0" (index 0)
        // 2. Unvisited: "10", "2", "5". Distances from "0": 10, 2, 5. Nearest is "2".
        // 3. From "2", unvisited: "10", "5". Distances: 8, 3. Nearest is "5".
        // 4. From "5", unvisited: "10". Distance: 5. Nearest is "10".
        // Expected order: "0", "2", "5", "10"
        val expectedNames = listOf("0", "2", "5", "10")
        val actualNames = route.locations.map { it.name }
        assertEquals(expectedNames, actualNames)

        // Total distance: 0->2 (2) + 2->5 (3) + 5->10 (5) + 10->0 (10) = 20.0
        assertEquals(20.0, route.totalDistance, 0.001)
    }

    @Test
    fun `solve with two locations should return both and round trip distance`() {
        // Arrange
        val locations = listOf(
            Location("A", "0", "Addr A", 0.0, 0.0),
            Location("B", "10", "Addr B", 0.0, 0.0)
        )
        val calculator = MockDistanceCalculator()
        val algorithm = NearestNeighborAlgorithm()

        // Act
        val route = algorithm.solve(locations, calculator)

        // Assert
        assertEquals(2, route.locations.size)
        assertEquals("0", route.locations[0].name)
        assertEquals("10", route.locations[1].name)
        
        // 0 -> 10 is 10. 10 -> 0 is 10. Total 20.
        assertEquals(20.0, route.totalDistance, 0.001)
    }

    @Test
    fun `solve with single location should return one stop and zero distance`() {
        // Arrange
        val locations = listOf(Location("A", "0", "Addr A", 0.0, 0.0))
        val calculator = MockDistanceCalculator()
        val algorithm = NearestNeighborAlgorithm()

        // Act
        val route = algorithm.solve(locations, calculator)

        // Assert
        assertEquals(1, route.locations.size)
        assertEquals(0.0, route.totalDistance, 0.001)
    }

    @Test
    fun `solveIndices with different startFrom should start at that index`() {
        // Arrange
        val locations = listOf(
            Location("id0", "0", "Addr 0", 0.0, 0.0),
            Location("id1", "10", "Addr 10", 0.0, 0.0),
            Location("id2", "2", "Addr 2", 0.0, 0.0),
            Location("id3", "5", "Addr 5", 0.0, 0.0)
        )
        val calculator = MockDistanceCalculator()
        val matrix = com.vintage4life.routeplanner.distance.DistanceMatrix(locations, calculator)
        val algorithm = NearestNeighborAlgorithm()

        // Act
        // Start from index 3 (value "5")
        val indices = algorithm.solveIndices(matrix, startFrom = 3)

        // Assert
        // 1. Start at index 3 ("5")
        // 2. Unvisited: "0", "10", "2". Distances from "5": 5, 5, 3. Nearest is "2" (index 2).
        // 3. From "2", unvisited: "0", "10". Distances: 2, 8. Nearest is "0" (index 0).
        // 4. From "0", unvisited: "10". Distance: 10. Nearest is "10" (index 1).
        // Expected indices: 3, 2, 0, 1
        assertEquals(listOf(3, 2, 0, 1), indices)
    }

    @Test
    fun `solve with empty list should return empty route`() {
        // Arrange
        val locations = emptyList<Location>()
        val calculator = MockDistanceCalculator()
        val algorithm = NearestNeighborAlgorithm()

        // Act
        val route = algorithm.solve(locations, calculator)

        // Assert
        assertEquals(0, route.locations.size)
        assertEquals(0.0, route.totalDistance, 0.001)
    }
}

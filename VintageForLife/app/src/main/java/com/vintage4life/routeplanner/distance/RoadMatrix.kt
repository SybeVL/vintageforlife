package com.vintage4life.routeplanner.distance

// similar to DistanceMatrix but this one uses road data to fill the n x n grid
data class RoadMatrix(
    val durationsSeconds: Array<DoubleArray>,
    val distancesKm: Array<DoubleArray>
) {
    val size: Int get() = durationsSeconds.size

    // Array equals/hashCode override zodat data class correct werkt
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RoadMatrix) return false
        return durationsSeconds.contentDeepEquals(other.durationsSeconds) &&
               distancesKm.contentDeepEquals(other.distancesKm)
    }

    override fun hashCode(): Int =
        31 * durationsSeconds.contentDeepHashCode() + distancesKm.contentDeepHashCode()
}

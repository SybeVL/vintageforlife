package com.vintage4life.routeplanner.distance

/**
 * N×N matrix met echte wegdata van de Mapbox Matrix API.
 *
 * @param durationsSeconds  [i][j] = rijtijd in seconden van stop i naar stop j
 * @param distancesKm       [i][j] = wegafstand in km van stop i naar stop j
 */
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

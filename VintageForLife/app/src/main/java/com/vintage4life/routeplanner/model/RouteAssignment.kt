package com.vintage4life.routeplanner.model

import java.time.LocalDate

/**
 * Koppelt een Route aan een Chauffeur met bezorgdatum en status.
 * Conform UML: heeft [complete()] en [cancel()] methoden.
 */
data class RouteAssignment(
    val id: String,
    val chauffeur: Chauffeur,
    val route: Route,
    val deliveryDate: LocalDate = LocalDate.now(),
    val status: AssignmentStatus = AssignmentStatus.PENDING
) {
    /**
     * Markeert de toewijzing als voltooid.
     * Conform UML: complete(): void
     * Retourneert een nieuwe RouteAssignment (Kotlin-idioom: immutable copy).
     */
    fun complete(): RouteAssignment = copy(status = AssignmentStatus.COMPLETED)

    /**
     * Annuleert de toewijzing.
     * Conform UML: cancel(): void
     * Retourneert een nieuwe RouteAssignment (Kotlin-idioom: immutable copy).
     */
    fun cancel(): RouteAssignment = copy(status = AssignmentStatus.CANCELLED)
}

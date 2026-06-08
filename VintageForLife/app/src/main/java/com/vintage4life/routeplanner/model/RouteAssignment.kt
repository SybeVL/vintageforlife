package com.vintage4life.routeplanner.model

import java.time.LocalDate

// Links a Route to a Chauffeur with a delivery date and status.
data class RouteAssignment(
    val id: String,
    val chauffeur: Chauffeur,
    val route: Route,
    val deliveryDate: LocalDate = LocalDate.now(),
    val status: AssignmentStatus = AssignmentStatus.PENDING
) {
    // Marks the assignment as completed. Returns an immutable copy.
    fun complete(): RouteAssignment = copy(status = AssignmentStatus.COMPLETED)

    // Cancels the assignment. Returns an immutable copy.
    fun cancel(): RouteAssignment = copy(status = AssignmentStatus.CANCELLED)
}

package com.vintage4life.routeplanner.model

import java.time.LocalDate

/**
 * Links a Route to a Chauffeur with a delivery date and status.
 */
data class RouteAssignment(
    val id: String,
    val chauffeur: Chauffeur,
    val route: Route,
    val deliveryDate: LocalDate,
    val status: AssignmentStatus = AssignmentStatus.PENDING
)

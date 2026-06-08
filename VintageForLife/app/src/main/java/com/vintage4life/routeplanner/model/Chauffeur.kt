package com.vintage4life.routeplanner.model

// Driver user with a license number and optional vehicle ID.
data class Chauffeur(
    override val id: String,
    override val name: String,
    override val email: String,
    val licenseNumber: String,
    val vehicleId: String = ""
) : User(id, name, email, Role.CHAUFFEUR)

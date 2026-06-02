package com.vintage4life.routeplanner.model

data class Chauffeur(
    override val id: String,
    override val name: String,
    override val email: String,
    val licenseNumber: String
) : User(id, name, email, Role.CHAUFFEUR)

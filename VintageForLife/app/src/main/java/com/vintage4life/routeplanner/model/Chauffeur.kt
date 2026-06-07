package com.vintage4life.routeplanner.model

/**
 * Chauffeur-gebruiker.
 * Conform UML: heeft [licenseNumber] en [vehicleId].
 *
 * UML-toevoeging: [vehicleId] toegevoegd conform UML-diagram.
 * Geen functionele impact — veld is optioneel (default leeg).
 */
data class Chauffeur(
    override val id: String,
    override val name: String,
    override val email: String,
    val licenseNumber: String,
    val vehicleId: String = ""
) : User(id, name, email, Role.CHAUFFEUR)

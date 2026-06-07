package com.vintage4life.routeplanner.model

/**
 * Abstracte basisklasse voor gebruikers.
 * Conform UML: abstract class met id, name, email en getRole().
 *
 * UML-afwijking: UML toont [role: String] als field.
 * Geïmplementeerd met [Role] enum en [getRole()] methode voor type-veiligheid.
 */
abstract class User(
    open val id: String,
    open val name: String,
    open val email: String,
    protected open val role: Role
)
// Kotlin genereert automatisch getRole() als JVM-getter van de [role] property.
// Een expliciete getRole()-methode zou een JVM-signatuurbotsing veroorzaken.

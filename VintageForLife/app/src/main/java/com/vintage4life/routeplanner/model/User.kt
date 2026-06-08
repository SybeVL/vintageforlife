package com.vintage4life.routeplanner.model

/**
 * Abstract base class for all users.
 * [role] is a typed enum instead of a String — Kotlin auto-generates getRole() as a JVM getter.
 */
abstract class User(
    open val id: String,
    open val name: String,
    open val email: String,
    protected open val role: Role
)

package com.vintage4life.routeplanner.model

data class Administrator(
    override val id: String,
    override val name: String,
    override val email: String
) : User(id, name, email, Role.ADMINISTRATOR)

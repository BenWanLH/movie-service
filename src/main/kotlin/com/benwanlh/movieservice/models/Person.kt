package com.benwanlh.movieservice.models

import com.benwanlh.movieservice.enums.Role

data class Person (
    val id: Long,
    val name: String,
    val roles: List<Role>,
    val birthDate: String?,
    val bio: String?,
    val photoUrl: String?,
)
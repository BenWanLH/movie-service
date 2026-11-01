package com.benwanlh.movieservice.dtos

import com.benwanlh.movieservice.enums.Role

data class PersonDetail (
    val name: String,

    val roles: List<Role>,

    val birthDate: String? = null,

    val bio: String? = null,

    val photoUrl: String? = null,
)
package com.benwanlh.movieservice.dtos

import com.benwanlh.movieservice.enums.Role
import jakarta.validation.constraints.Positive

data class CreditDetail (

    @field:Positive
    val movieId: Long,

    @field:Positive
    val personId: Long,

    val role: Role,

    val characterName: String?

)

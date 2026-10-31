package com.benwanlh.movieservice.dtos

import com.benwanlh.movieservice.enums.Genre
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class MovieDetail(

    @field:NotBlank
    val title: String,

    val synopsis: String? = null,

    val releaseYear: Int? = null,

    @field:Positive
    val runtimeMinutes: Int? = null,

    val genres: Set<Genre> = emptySet(),

    val artworkFilename: String? = null

)
package com.benwanlh.movieservice.repositories.entities

import com.benwanlh.movieservice.enums.Genre
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "movie")
class Movie (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "synopsis")
    var synopsis: String? = null,

    @Column(name = "release_year")
    var releaseYear: Int? = null,

    @Column(name = "runtime_minutes")
    var runtimeMinutes: Int? = null,

    @Column(name = "artwork_filename")
    var artworkFilename: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "movie_genre",
        joinColumns = [JoinColumn(name = "movie_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "genre")
    var genres: MutableSet<Genre> = mutableSetOf(),
)

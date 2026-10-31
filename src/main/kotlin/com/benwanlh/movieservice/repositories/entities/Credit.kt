package com.benwanlh.movieservice.repositories.entities

import com.benwanlh.movieservice.enums.Role
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "credit")
class Credit(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "movie_id", nullable = false)
    var movieId: Long,

    @Column(name = "person_id", nullable = false)
    var personId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: Role,

    @Column(name = "character_name")
    var characterName: String? = null

)
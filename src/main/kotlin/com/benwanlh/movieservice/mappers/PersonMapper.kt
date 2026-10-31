package com.benwanlh.movieservice.mappers

import com.benwanlh.movieservice.enums.Role
import com.benwanlh.movieservice.models.Person
import com.benwanlh.people.v1.Person as PersonProto


fun PersonProto.toModel() = Person(
    id = id,
    name = name,
    roles = rolesList.map { Role.valueOf(it.name) },
    birthDate = if (hasBirthDate()) birthDate else null,
    bio = if (hasBio()) bio else null,
    photoUrl = if (hasPhotoUrl()) photoUrl else null,
)
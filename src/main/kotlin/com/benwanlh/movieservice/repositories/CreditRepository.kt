package com.benwanlh.movieservice.repositories

import com.benwanlh.movieservice.repositories.entities.Credit
import org.springframework.data.jpa.repository.JpaRepository

interface CreditRepository : JpaRepository<Credit, Long> {
    fun findByMovieIdIn(movieIds: Collection<Long>): List<Credit>
    fun deleteByPersonId(personId: Long)
}
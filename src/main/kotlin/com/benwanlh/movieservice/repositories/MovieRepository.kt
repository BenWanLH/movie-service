

package com.benwanlh.movieservice.repositories

import com.benwanlh.movieservice.repositories.entities.Movie
import org.springframework.data.jpa.repository.JpaRepository

interface MovieRepository : JpaRepository<Movie, Long> {
    fun findByTitleContainingIgnoreCase(title: String): List<Movie>
}
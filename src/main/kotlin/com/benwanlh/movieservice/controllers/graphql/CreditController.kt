package com.benwanlh.movieservice.controllers.graphql

import com.benwanlh.movieservice.dtos.CreditDetail
import com.benwanlh.movieservice.repositories.entities.Credit
import com.benwanlh.movieservice.repositories.entities.Movie
import com.benwanlh.movieservice.services.CreditService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller
import kotlin.collections.get

@Controller
class CreditController(
    private val creditService: CreditService
) {
    @MutationMapping
    suspend fun addCredit(@Argument creditDetail: CreditDetail) = creditService.addCredit(creditDetail)

    @MutationMapping
    fun deleteCredit(@Argument id: Long): Boolean {
        creditService.deleteCredit(id)
        return true
    }

    @BatchMapping
    fun credits(movies: List<Movie>): Map<Movie, List<Credit>> {
        val creditsByMovieId = creditService.getCreditsForMovies(movies.map { it.id!! }).groupBy { it.movieId }
        return movies.associateWith { creditsByMovieId[it.id] ?: emptyList() }
    }

}
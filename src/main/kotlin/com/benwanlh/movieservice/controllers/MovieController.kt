package com.benwanlh.movieservice.controllers

import com.benwanlh.movieservice.dtos.MovieDetail
import com.benwanlh.movieservice.exceptions.MovieNotFoundException
import com.benwanlh.movieservice.repositories.entities.Movie
import com.benwanlh.movieservice.services.MovieService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class MovieController(
    private val movieService: MovieService,
) {

    @QueryMapping
    fun movies(@Argument title: String?): List<Movie> =
        if(title.isNullOrBlank()) movieService.listAllMovies()
        else movieService.searchMovies(title)

    @QueryMapping
    fun movie(@Argument id: Long) =
        try {
            movieService.getMovieById(id)
        } catch (e: MovieNotFoundException) {
            null
        }

    @MutationMapping
    fun createMovie(@Argument movieDetail: MovieDetail): Movie = movieService.createMovie(movieDetail)

    @MutationMapping
    fun updateMovie(@Argument id: Long, @Argument movieDetail: MovieDetail) = movieService.updateMovie(id, movieDetail)

    @MutationMapping
    fun deleteMovie(@Argument id: Long): Boolean {
        movieService.deleteMovie(id)
        return true
    }

    @SchemaMapping
    fun artworkUrl(movie: Movie): String? =
        movie.artworkFilename?.let { "/movies/${movie.id}/artwork?v=$it" }

}
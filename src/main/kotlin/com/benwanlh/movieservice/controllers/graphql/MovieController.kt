package com.benwanlh.movieservice.controllers.graphql

import com.benwanlh.movieservice.dtos.MovieDetail
import com.benwanlh.movieservice.exceptions.ArtworkNotFoundException
import com.benwanlh.movieservice.exceptions.InvalidArtworkException
import com.benwanlh.movieservice.exceptions.MovieNotFoundException
import com.benwanlh.movieservice.repositories.entities.Movie
import com.benwanlh.movieservice.services.MovieService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException

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

    @ExceptionHandler(MovieNotFoundException::class, ArtworkNotFoundException::class)
    fun notFound(e: RuntimeException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)

    @ExceptionHandler(InvalidArtworkException::class)
    fun badRequest(e: InvalidArtworkException): ResponseEntity<String> =
        ResponseEntity.badRequest().body(e.message)

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun uploadSizeExceed(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body("File must be 5MB or smaller")

}
package com.benwanlh.movieservice.services

import com.benwanlh.movieservice.dtos.MovieDetail
import com.benwanlh.movieservice.exceptions.MovieNotFoundException
import com.benwanlh.movieservice.repositories.MovieRepository
import com.benwanlh.movieservice.repositories.entities.Movie
import jakarta.validation.Valid
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
@Transactional
class MovieService(
    private val movieRepository: MovieRepository,
) {
    fun getMovieById(id: Long): Movie =
        movieRepository.findByIdOrNull(id) ?: throw MovieNotFoundException(id)

    fun listAllMovies(): List<Movie> = movieRepository.findAll()

    fun createMovie(@Valid movieDetail: MovieDetail): Movie = movieRepository.save(Movie(
        title = movieDetail.title.trim(),
        synopsis = movieDetail.synopsis.blankToNull(),
        releaseYear = movieDetail.releaseYear,
        runtimeMinutes = movieDetail.runtimeMinutes,
        genres = movieDetail.genres.toMutableSet()
    ))

    fun updateMovie(id: Long, @Valid movieDetail: MovieDetail): Movie {

        val movie = movieRepository.findByIdOrNull(id) ?: throw MovieNotFoundException(id)

        return movieRepository.save(Movie(
            id = id,
            title = movieDetail.title.trim(),
            synopsis = movieDetail.synopsis.blankToNull(),
            releaseYear = movieDetail.releaseYear,
            runtimeMinutes = movieDetail.runtimeMinutes,
            genres = movieDetail.genres.toMutableSet(),
            artworkFilename = movie.artworkFilename
        ))
    }

    fun deleteMovie(id: Long) {

        if(!movieRepository.existsById(id)) throw MovieNotFoundException(id)

        movieRepository.deleteById(id)
    }

    private fun String?.blankToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

}
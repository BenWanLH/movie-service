package com.benwanlh.movieservice.services

import com.benwanlh.movieservice.dtos.MovieDetail
import com.benwanlh.movieservice.exceptions.ArtworkNotFoundException
import com.benwanlh.movieservice.exceptions.InvalidArtworkException
import com.benwanlh.movieservice.exceptions.MovieNotFoundException
import com.benwanlh.movieservice.repositories.MovieRepository
import com.benwanlh.movieservice.repositories.entities.Movie
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

@Service
@Validated
@Transactional
class MovieService(
    private val movieRepository: MovieRepository,
    @Value("\${artwork.directory}") artworkStoragePath: String,
) {

    private val artworkDirectory: Path = Path.of(artworkStoragePath)

    private val allowedImageTypes = mapOf(
        "image/jpeg" to "jpg",
        "image/png" to "png",
        "image/webp" to "webp",
    )

    fun getMovieById(id: Long): Movie =
        movieRepository.findByIdOrNull(id) ?: throw MovieNotFoundException(id)

    fun searchMovies(title: String): List<Movie> = movieRepository.findByTitleContainingIgnoreCase(title.trim())

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
        val movie = movieRepository.findByIdOrNull(id) ?: throw MovieNotFoundException(id)

        movieRepository.deleteById(id)

        movie.artworkFilename?.let { Files.deleteIfExists(artworkDirectory.resolve(it)) }
    }

    fun getArtwork(movieId: Long): Resource {
        val filename = this.getMovieById(movieId).artworkFilename
            ?: throw ArtworkNotFoundException(movieId)

        val file = FileSystemResource(artworkDirectory.resolve(filename))
        if (!file.exists()) throw ArtworkNotFoundException(movieId)

        return file
    }

    fun uploadArtwork(movieId: Long, file: MultipartFile): Movie {
        val movie = this.getMovieById(movieId)

        if (file.isEmpty) throw InvalidArtworkException("File is empty")
        val extension = allowedImageTypes[file.contentType]
            ?: throw InvalidArtworkException("Only JPEG, PNG or WebP images are allowed")

        Files.createDirectories(artworkDirectory)
        val filename = "${UUID.randomUUID()}.$extension"
        val target = artworkDirectory.resolve(filename)
        file.inputStream.use { Files.copy(it, target) }

        val oldFilename = movie.artworkFilename

        movie.artworkFilename = filename

        oldFilename?.let { Files.deleteIfExists(artworkDirectory.resolve(it)) }

        return movie
    }


    private fun String?.blankToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

}
package com.benwanlh.movieservice.services

import com.benwanlh.movieservice.dtos.MovieDetail
import com.benwanlh.movieservice.exceptions.InvalidArtworkException
import com.benwanlh.movieservice.exceptions.MovieNotFoundException
import com.benwanlh.movieservice.repositories.MovieRepository
import com.benwanlh.movieservice.repositories.entities.Movie
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.springframework.data.repository.findByIdOrNull
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional

class MovieServiceTest {

    @TempDir
    lateinit var tempDir: Path                       // fresh empty folder per test, deleted afterwards

    private val movieRepository = mockk<MovieRepository>()
    private lateinit var movieService: MovieService

    @BeforeEach
    fun setUp() {
        movieService = MovieService(movieRepository, tempDir.toString())
    }

    @Test
    fun `uploadArtwork throws InvalidArtworkException with a non-image file`() {
        every { movieRepository.findById(1L) } returns Optional.of(Movie(id = 1L, title = "The Matrix"))
        val file = MockMultipartFile("file", "notes.txt", "text/plain", "hello".toByteArray())

        assertThrows<InvalidArtworkException> { movieService.uploadArtwork(1L, file) }
    }

    @Test
    fun `uploadArtwork throws InvalidArtworkException with a empty file`() {
        every { movieRepository.findById(1L) } returns Optional.of(Movie(id = 1L, title = "The Matrix"))
        val file = MockMultipartFile("file", "notes.txt", "image/jpeg", ByteArray(0))

        assertThrows<InvalidArtworkException> { movieService.uploadArtwork(1L, file) }
    }

    @Test
    fun `uploadArtwork throws MovieNotFoundException if movieId is not found`() {
        every { movieRepository.findById(any()) } returns Optional.empty()
        val file = MockMultipartFile("file", "notes.jpg", "image/jpeg", "hello".toByteArray())

        assertThrows<MovieNotFoundException> { movieService.uploadArtwork(1L, file) }
    }

    @Test
    fun `createMovie successfully`() {
        every { movieRepository.save(any()) } answers { firstArg() }

        val movie = movieService.createMovie(MovieDetail(title = "The Matrix  ", synopsis = " "))

        assertEquals(null, movie.synopsis)
        assertEquals("The Matrix", movie.title)
    }

    @Test
    fun `updateMovie throws MovieNotFoundException if movieId is not found` () {
        every { movieRepository.findById(any()) } returns Optional.empty()

        assertThrows<MovieNotFoundException> { movieService.updateMovie(1L, MovieDetail(title = "The Matrix")) }
    }

    @Test
    fun `uploadArtwork should update artworkFileName of Movie`() {
        every { movieRepository.findByIdOrNull(any()) } answers { Movie(id = firstArg(), title = "The Matrix") }

        val file = MockMultipartFile("file", "notes.jpg", "image/jpeg", "hello".toByteArray())
        val movie = movieService.uploadArtwork(1, file)

        val saved = tempDir.resolve(movie.artworkFilename!!)

        assertNotNull(movie.artworkFilename)
        assertTrue(Files.exists(saved))
        assertTrue(movie.artworkFilename!!.endsWith(".jpg"))
    }
}
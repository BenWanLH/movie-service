package com.benwanlh.movieservice.controllers.rest

import com.benwanlh.movieservice.exceptions.ArtworkNotFoundException
import com.benwanlh.movieservice.exceptions.InvalidArtworkException
import com.benwanlh.movieservice.exceptions.MovieNotFoundException
import com.benwanlh.movieservice.services.MovieService
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartFile

@RestController
class MovieArtworkController(
    private val movieService: MovieService,
) {
    @PostMapping("movies/{id}/artwork")
    fun uploadArtwork(@PathVariable id: Long, @RequestParam("file") file: MultipartFile): ResponseEntity<Void> {
        movieService.uploadArtwork(id, file)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("movies/{id}/artwork")
    fun getArtwork(@PathVariable id: Long): ResponseEntity<Resource> {
        val artwork = movieService.getArtwork(id)
        val contentType = MediaTypeFactory.getMediaType(artwork).orElse(MediaType.APPLICATION_OCTET_STREAM)

        return ResponseEntity.ok()
            .contentType(contentType).body(artwork)
    }

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
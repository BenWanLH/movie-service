package com.benwanlh.movieservice.controllers.rest

import com.benwanlh.movieservice.exceptions.ArtworkNotFoundException
import com.benwanlh.movieservice.exceptions.InvalidArtworkException
import com.benwanlh.movieservice.exceptions.MovieNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException

@ControllerAdvice
class RestExceptionHandlers {
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
package com.benwanlh.movieservice.exceptions

class ArtworkNotFoundException (val id: Long) : RuntimeException("Artwork not found for Movie $id")
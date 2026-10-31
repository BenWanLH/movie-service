package com.benwanlh.movieservice.exceptions

class MovieNotFoundException (val id: Long) : RuntimeException("Movie $id not found")
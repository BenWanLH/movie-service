package com.benwanlh.movieservice.exceptions

class CreditNotFoundException (val id: Long) : RuntimeException("Credit $id not found")
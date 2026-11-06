package com.benwanlh.movieservice.grpc.services

import com.benwanlh.movies.v1.DeleteCreditsByPersonRequest
import com.benwanlh.movies.v1.DeleteCreditsByPersonResponse
import com.benwanlh.movies.v1.MovieCreditsServiceGrpcKt
import com.benwanlh.movies.v1.deleteCreditsByPersonResponse
import com.benwanlh.movieservice.services.CreditService
import org.springframework.stereotype.Service

@Service
class MovieCreditsGrpcService(
    private val creditService: CreditService,
) : MovieCreditsServiceGrpcKt.MovieCreditsServiceCoroutineImplBase() {

    override suspend fun deleteCreditsByPerson(request: DeleteCreditsByPersonRequest): DeleteCreditsByPersonResponse {
        creditService.deleteCreditsByPersonId(request.personId)
        return deleteCreditsByPersonResponse { }
    }
}
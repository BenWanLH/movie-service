package com.benwanlh.movieservice.services

import com.benwanlh.movieservice.dtos.CreditDetail
import com.benwanlh.movieservice.exceptions.CreditNotFoundException
import com.benwanlh.movieservice.exceptions.InvalidRoleException
import com.benwanlh.movieservice.exceptions.MovieNotFoundException
import com.benwanlh.movieservice.mappers.toModel
import com.benwanlh.movieservice.repositories.CreditRepository
import com.benwanlh.movieservice.repositories.MovieRepository
import com.benwanlh.movieservice.repositories.entities.Credit
import com.benwanlh.people.v1.PeopleServiceGrpcKt
import com.benwanlh.people.v1.getPersonRequest
import jakarta.validation.Valid
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
class CreditService(
    private val creditRepository: CreditRepository,
    private val movieRepository: MovieRepository,
    private val peopleStub: PeopleServiceGrpcKt.PeopleServiceCoroutineStub,
) {

    fun getCreditsForMovies(movieIds: Collection<Long>): Collection<Credit> = creditRepository.findByMovieIdIn(movieIds)

    suspend fun addCredit(@Valid creditDetail: CreditDetail): Credit {

        movieRepository.findByIdOrNull(creditDetail.movieId) ?: throw MovieNotFoundException(creditDetail.movieId)
        val person = peopleStub.getPerson(getPersonRequest { this.id = creditDetail.personId }).toModel()

        if (!person.roles.contains(creditDetail.role)) throw InvalidRoleException(person.name, creditDetail.role)

        return creditRepository.save(
            Credit(
                movieId = creditDetail.movieId,
                personId = creditDetail.personId,
                role = creditDetail.role,
                characterName = creditDetail.characterName.blankToNull(),
            )
        )
    }

    fun deleteCredit(id: Long) {
        creditRepository.findByIdOrNull(id) ?: throw CreditNotFoundException(id)

        creditRepository.deleteById(id)
    }

    @Transactional
    fun deleteCreditsByPersonId(personId: Long) = creditRepository.deleteByPersonId(personId)

    private fun String?.blankToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

}
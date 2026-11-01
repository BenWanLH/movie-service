package com.benwanlh.movieservice.services

import com.benwanlh.movieservice.dtos.CreditDetail
import com.benwanlh.movieservice.enums.Role
import com.benwanlh.movieservice.exceptions.CreditNotFoundException
import com.benwanlh.movieservice.exceptions.InvalidRoleException
import com.benwanlh.movieservice.exceptions.MovieNotFoundException
import com.benwanlh.movieservice.repositories.CreditRepository
import com.benwanlh.movieservice.repositories.MovieRepository
import com.benwanlh.movieservice.repositories.entities.Movie
import com.benwanlh.people.v1.PeopleServiceGrpcKt
import com.benwanlh.people.v1.Role as RoleProto
import com.benwanlh.people.v1.person
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreditServiceTest {

    private val creditRepository = mockk<CreditRepository>()
    private val movieRepository = mockk<MovieRepository>()
    private val peopleStub = mockk<PeopleServiceGrpcKt.PeopleServiceCoroutineStub>()
    private val creditService = CreditService(creditRepository, movieRepository, peopleStub)

    @Test
    fun `addCredit throws InvalidRoleException when person does not have the role`() = runTest {
        every { movieRepository.findById(1L) } returns Optional.of(Movie(id = 1L, title = "The Matrix"))
        coEvery { peopleStub.getPerson(any(), any()) } returns person {
            id = 7; name = "Keanu Reeves"; roles += RoleProto.ACTOR
        }

        assertFailsWith<InvalidRoleException> {
            creditService.addCredit(
                CreditDetail(
                    movieId = 1,
                    personId = 7,
                    role = com.benwanlh.movieservice.enums.Role.DIRECTOR,
                    characterName = null
                )
            )
        }
    }

    @Test
    fun `addCredit throws MovieNotFoundException when movieId does not exist`() = runTest {
        every { movieRepository.findById(1L) } returns Optional.empty()

        assertFailsWith<MovieNotFoundException> {
            creditService.addCredit(
                CreditDetail(
                    movieId = 1,
                    personId = 7,
                    role = com.benwanlh.movieservice.enums.Role.DIRECTOR,
                    characterName = null
                )
            )
        }
    }

    @Test
    fun `addCredit successfully`() = runTest {
        every { movieRepository.findById(1L) } returns Optional.of(Movie(id = 1L, title = "The Matrix"))
        every{ creditRepository.save(any()) } answers { firstArg() }
        coEvery { peopleStub.getPerson(any(), any()) } returns person {
            id = 7; name = "Keanu Reeves"; roles += RoleProto.ACTOR
        }

        val credit = creditService.addCredit(
            CreditDetail(
                movieId = 1,
                personId = 7,
                role = Role.ACTOR,
                characterName = " "
            )
        )

        assertEquals(null, credit.characterName)
    }

    @Test
    fun `deleteCredit throws CreditNotFoundException when creditId not found `() = runTest {
        every { creditRepository.findById(5L) } returns Optional.empty()

        assertFailsWith<CreditNotFoundException> {
            creditService.deleteCredit(5L)
        }

    }

}
package com.benwanlh.movieservice.controllers.graphql

import com.benwanlh.movieservice.enums.Role
import com.benwanlh.movieservice.dtos.PersonDetail
import com.benwanlh.movieservice.repositories.entities.Credit
import com.benwanlh.movieservice.services.CreditService
import com.benwanlh.people.v1.CreatePersonRequest
import com.benwanlh.people.v1.DeletePersonRequest
import com.benwanlh.people.v1.PeopleServiceGrpcKt
import com.benwanlh.people.v1.batchGetPeopleResponse
import com.benwanlh.people.v1.deletePersonResponse
import com.benwanlh.people.v1.listPeopleResponse
import com.benwanlh.people.v1.person as PersonProto
import com.benwanlh.people.v1.Role as RoleProto
import io.grpc.Status
import io.grpc.StatusException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class PersonControllerTest {

    private val peopleStub = mockk<PeopleServiceGrpcKt.PeopleServiceCoroutineStub>()
    private val creditService = mockk<CreditService>()
    private val personController = PersonController(peopleStub, creditService)

    @Test
    fun `person returns null when people-service says NOT_FOUND`() = runTest {
        coEvery { peopleStub.getPerson(any(), any()) } throws StatusException(Status.NOT_FOUND)

        assertNull(personController.person(99L))
    }

    @Test
    fun `createPerson does not set optional fields that are null`() = runTest {
        val request = slot<CreatePersonRequest>()
        coEvery { peopleStub.createPerson(capture(request), any()) } returns PersonProto {
            id = 1; name = "Keanu Reeves"; roles += RoleProto.ACTOR
        }

        personController.createPerson(PersonDetail(
            name = "Keanu Reeves", roles = listOf(Role.ACTOR),
        ))

        assertFalse(request.captured.hasBio())
        assertFalse(request.captured.hasBirthDate())
    }

    @Test
    fun `deletePerson also deletesCredit relating to person`() = runTest {
        val deletePersonRequest = slot<DeletePersonRequest>()
        val deleteCreditRequest = slot<Long>()

        coEvery { peopleStub.deletePerson(capture(deletePersonRequest),any()) } returns deletePersonResponse {}
        every { creditService.deleteCreditsByPersonId(capture(deleteCreditRequest)) } just Runs

        personController.deletePerson(99L)

        assertEquals(deleteCreditRequest.captured,deletePersonRequest.captured.id )
    }

    @Test
    fun `deletePerson throws Status_NotFound if personId does not exist`() = runTest {
        coEvery { peopleStub.deletePerson(any(), any()) } throws StatusException(Status.NOT_FOUND)

        assertFailsWith<StatusException> { personController.deletePerson(99L) }
    }

    @Test
    fun `listPeople should filter correctly by role`() = runTest {
        coEvery { peopleStub.listPeople(any(),any()) } returns listPeopleResponse {
            people += PersonProto { id = 1; name = "Keanu Reeves"; roles += RoleProto.ACTOR }
            people += PersonProto { id = 2; name = "Lana Wachowski"; roles += RoleProto.DIRECTOR }
        }

        val listOfPeople = personController.people(roles = listOf(Role.DIRECTOR), name = null)

        assertEquals(1, listOfPeople.count())
        assertEquals(listOf(2L), listOfPeople.map { it.id })
    }

    @Test
    fun `people with a empty string lists everyone instead of searching`() = runTest {
        coEvery { peopleStub.listPeople(any(), any()) } returns listPeopleResponse {
            people += PersonProto { id = 1; name = "Keanu Reeves"; roles += RoleProto.ACTOR }
        }

        val result = personController.people(roles = null, name = "   ")

        assertEquals(1, result.size)
        coVerify(exactly = 0) { peopleStub.searchPeople(any(), any()) }
    }

    @Test
    fun `peopleForCredits doesn't fail and only returns credit if person is found`() = runTest {
        coEvery { peopleStub.batchGetPeople(any(), any()) } returns batchGetPeopleResponse {
            people += PersonProto { id = 7; name = "Keanu Reeves"; roles += RoleProto.ACTOR }
        }
        val creditForId7 = Credit(id = 1, movieId = 1, personId = 7, role = Role.ACTOR)
        val creditForId8 = Credit(id = 2, movieId = 1, personId = 8, role = Role.DIRECTOR)

        val result = personController.peopleForCredits(listOf(creditForId7, creditForId8))

        assertEquals("Keanu Reeves", result[creditForId7]?.name)
        assertNull(result[creditForId8])
    }
}
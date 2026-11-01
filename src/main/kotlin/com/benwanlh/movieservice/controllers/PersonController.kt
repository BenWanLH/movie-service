package com.benwanlh.movieservice.controllers

import com.benwanlh.movieservice.dtos.PersonDetail
import com.benwanlh.movieservice.enums.Role
import com.benwanlh.movieservice.mappers.toModel
import com.benwanlh.movieservice.models.Person
import com.benwanlh.movieservice.repositories.entities.Credit
import com.benwanlh.movieservice.services.CreditService
import com.benwanlh.people.v1.PeopleServiceGrpcKt
import com.benwanlh.people.v1.batchGetPeopleRequest
import com.benwanlh.people.v1.createPersonRequest
import com.benwanlh.people.v1.deletePersonRequest
import com.benwanlh.people.v1.getPersonRequest
import com.benwanlh.people.v1.listPeopleRequest
import com.benwanlh.people.v1.searchPeopleRequest
import com.benwanlh.people.v1.updatePersonRequest
import com.benwanlh.people.v1.Role as RoleProto

import io.grpc.Status
import io.grpc.StatusException
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class PersonController(
    private val peopleStub: PeopleServiceGrpcKt.PeopleServiceCoroutineStub,
    private val creditService: CreditService
) {

    @QueryMapping
    suspend fun person(@Argument id: Long): Person? =
        try {
            peopleStub.getPerson(getPersonRequest { this.id = id }).toModel()
        } catch (e: StatusException) {
            if (e.status.code == Status.Code.NOT_FOUND) null else throw e
        }

    @QueryMapping
    suspend fun people(@Argument roles: List<Role>?, @Argument name: String?): List<Person> {
        val people = if(name.isNullOrBlank())
            peopleStub.listPeople(listPeopleRequest { }).peopleList.map { it.toModel() }
        else
            peopleStub.searchPeople(searchPeopleRequest { this.name = name }).peopleList.map { it.toModel() }

        if (roles.isNullOrEmpty()) return people

        return people.filter { person -> person.roles.any { it in roles } }
    }

    @MutationMapping
    suspend fun createPerson(@Argument personDetail: PersonDetail): Person =
        peopleStub.createPerson(createPersonRequest {
            name = personDetail.name
            roles += personDetail.roles.map { RoleProto.valueOf(it.name) }
            personDetail.birthDate?.let { birthDate = it }
            personDetail.bio?.let { bio = it }
            personDetail.photoUrl?.let { photoUrl = it }
        }).toModel()

    @MutationMapping
    suspend fun updatePerson(@Argument id: Long, @Argument personDetail: PersonDetail): Person =
        peopleStub.updatePerson(updatePersonRequest {
            this.id = id
            name = personDetail.name
            roles += personDetail.roles.map { RoleProto.valueOf(it.name) }
            birthDate = personDetail.birthDate ?: ""
            bio = personDetail.bio ?: ""
            photoUrl = personDetail.photoUrl ?: ""
        }).toModel()

    @MutationMapping
    suspend fun deletePerson(@Argument id: Long): Boolean {
        peopleStub.deletePerson(deletePersonRequest { this.id = id })
        creditService.deleteCreditsByPersonId(id)
        return true
    }

    @BatchMapping(field = "person")
    suspend fun peopleForCredits(credits: List<Credit>): Map<Credit, Person?> {
        val ids = credits.map { it.personId }.distinct()
        val peopleById = peopleStub
            .batchGetPeople(batchGetPeopleRequest { this.ids += ids })
            .peopleList
            .associate { it.id to it.toModel() }

        return credits.associateWith { peopleById[it.personId] }
    }

}
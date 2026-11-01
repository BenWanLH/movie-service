package com.benwanlh.movieservice.controllers.graphql

import com.benwanlh.movieservice.exceptions.CreditNotFoundException
import com.benwanlh.movieservice.exceptions.InvalidRoleException
import com.benwanlh.movieservice.exceptions.MovieNotFoundException
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import io.grpc.Status
import io.grpc.StatusException
import jakarta.validation.ConstraintViolationException
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler
import org.springframework.graphql.execution.ErrorType
import org.springframework.web.bind.annotation.ControllerAdvice
import org.slf4j.LoggerFactory

@ControllerAdvice
class GraphQlExceptionHandlers {

    private val log = LoggerFactory.getLogger(javaClass)

    @GraphQlExceptionHandler(
        MovieNotFoundException::class,
        CreditNotFoundException::class,
    )
    fun notFound(e: RuntimeException, env: DataFetchingEnvironment): GraphQLError =
        GraphqlErrorBuilder.newError(env).errorType(ErrorType.NOT_FOUND).message(e.message).build()

    @GraphQlExceptionHandler(
        ConstraintViolationException::class,
        InvalidRoleException::class,
    )
    fun badRequest(e: Exception, env: DataFetchingEnvironment): GraphQLError =
        GraphqlErrorBuilder.newError(env).errorType(ErrorType.BAD_REQUEST).message(e.message).build()

    @GraphQlExceptionHandler
    fun grpcError(e: StatusException,  env: DataFetchingEnvironment): GraphQLError {
        val (type, message) = when (e.status.code) {
            Status.Code.NOT_FOUND ->
                ErrorType.NOT_FOUND to (e.status.description ?: "Not found")

            Status.Code.INVALID_ARGUMENT ->
                ErrorType.BAD_REQUEST to (e.status.description ?: "Invalid input")

            else -> {
                log.error("grpc call failed ... {}", e.message, e)
                ErrorType.INTERNAL_ERROR to "Unexpected error"
            }
        }

        return GraphqlErrorBuilder.newError(env)
            .errorType(type)
            .message(message)
            .build()
    }

}
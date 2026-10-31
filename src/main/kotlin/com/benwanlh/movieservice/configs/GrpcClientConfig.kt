package com.benwanlh.movieservice.configs

import com.benwanlh.people.v1.PeopleServiceGrpcKt
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.grpc.client.GrpcChannelFactory

@Configuration
class GrpcClientConfig {

    @Bean
    fun peopleStub(channels: GrpcChannelFactory): PeopleServiceGrpcKt.PeopleServiceCoroutineStub =
        PeopleServiceGrpcKt.PeopleServiceCoroutineStub(channels.createChannel("people"))
}
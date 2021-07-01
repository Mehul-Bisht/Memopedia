package com.example.memopedia.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("{name}/{results}")
    suspend fun getByName(
        @Path("name") name: String,
        @Path("results") count: Int = 20
    ): Response
}
package com.example.ejercicio01

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Esta es la que te sale roja, asegúrate de que se llame igual:
    @GET("pokemon")
    suspend fun getPokemonList(@Query("limit") limit: Int): PokemonResponse

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") id: String): PokemonDetailResponse

    @GET("pokemon-species/{id}")
    suspend fun getPokemonDescription(@Path("id") id: String): PokemonSpeciesResponse
}
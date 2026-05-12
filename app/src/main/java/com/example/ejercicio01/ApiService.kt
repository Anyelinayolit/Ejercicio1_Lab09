package com.example.ejercicio01

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("pokemon")
    // Asegúrate de que el parámetro se llame 'limit'
    suspend fun getPokemonList(@Query("limit") limit: Int): PokemonResponse
}
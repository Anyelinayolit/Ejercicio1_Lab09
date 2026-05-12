package com.example.ejercicio01

data class PokemonResponse(
    val results: List<PokemonResult> // Debe llamarse "results"
)

data class PokemonResult(
    val name: String,
    val url: String
) {
    val id: String
        get() = url.split("/").filter { it.isNotEmpty() }.last()

    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
}
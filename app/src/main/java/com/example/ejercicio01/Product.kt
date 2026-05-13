package com.example.ejercicio01

// Esto es lo que ya tienes (No cambia)
data class PokemonResponse(
    val results: List<PokemonResult>
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

// --- NUEVAS CLASES PARA EL DETALLE ---

data class PokemonDetailResponse(
    val height: Int,          // Altura en decímetros
    val weight: Int,          // Peso en hectogramos
    val types: List<TypeSlot> // Lista de tipos (pueden ser uno o dos)
)

data class TypeSlot(
    val slot: Int,
    val type: TypeInfo
)

data class TypeInfo(
    val name: String // "fire", "water", etc.
)

// Esto va en tu archivo de modelos
data class PokemonSpeciesResponse(
    val flavor_text_entries: List<FlavorTextEntry>,
    val evolves_from_species: PreEvolucion? // Agrega esta línea
)

data class PreEvolucion(
    val name: String // El nombre del pokemon anterior (ej: charmander)
)

data class FlavorTextEntry(
    val flavor_text: String,
    val language: Language
)

data class Language(val name: String)
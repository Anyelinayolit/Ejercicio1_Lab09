package com.example.ejercicio01

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "pantalla_lista") {
                    composable("pantalla_lista") {
                        PokemonApp(onNavigateToDetail = { id, name ->
                            navController.navigate("detalle/$id/$name")
                        })
                    }
                    composable(
                        route = "detalle/{id}/{name}",
                        arguments = listOf(
                            navArgument("id") { type = NavType.StringType },
                            navArgument("name") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        PokemonDetailScreen(id, name, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonApp(onNavigateToDetail: (String, String) -> Unit) {
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val service = remember { retrofit.create(ApiService::class.java) }
    var pokemonList by remember { mutableStateOf<List<PokemonResult>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val response = service.getPokemonList(50)
            pokemonList = response.results
        } catch (e: Exception) { e.printStackTrace() }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("POKÉDEX EXPLORER", fontWeight = FontWeight.ExtraBold) }) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = padding,
            modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))
        ) {
            items(pokemonList) { pokemon ->
                PokemonCard(pokemon, onClick = { onNavigateToDetail(pokemon.id, pokemon.name) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(id: String, name: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("Consultando base de datos...") }
    var detail by remember { mutableStateOf<PokemonDetailResponse?>(null) }

    // --- LÓGICA DE COLOR DE FONDO DINÁMICO ---
    val tipoPrincipal = detail?.types?.firstOrNull()?.type?.name ?: "normal"
    val colorBase = when (tipoPrincipal) {
        "fire" -> Color(0xFFFFEBEE)
        "water" -> Color(0xFFE3F2FD)
        "grass" -> Color(0xFFF1F8E9)
        "electric" -> Color(0xFFFFFDE7)
        "poison" -> Color(0xFFF3E5F5)
        else -> Color.White
    }
    val animatedColor by animateColorAsState(targetValue = colorBase)

    // --- ANIMACIÓN DE FLOTADO PARA LA IMAGEN ---
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val traducirTipo = { tipoIngles: String ->
        when (tipoIngles.lowercase()) {
            "fire" -> "Fuego"
            "water" -> "Agua"
            "grass" -> "Planta"
            "poison" -> "Veneno"
            "electric" -> "Eléctrico"
            "flying" -> "Volador"
            "ice" -> "Hielo"
            "bug" -> "Bicho"
            "rock" -> "Roca"
            "ground" -> "Tierra"
            "psychic" -> "Psíquico"
            "ghost" -> "Fantasma"
            "dragon" -> "Dragón"
            "steel" -> "Acero"
            "fairy" -> "Hada"
            "fighting" -> "Lucha"
            "normal" -> "Normal"
            else -> tipoIngles
        }
    }

    val tts = remember {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Seteamos idioma
            }
        }.apply { language = Locale("es", "ES") }
    }

    val speak = { text: String ->
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    val retrofit = remember {
        Retrofit.Builder().baseUrl("https://pokeapi.co/api/v2/").addConverterFactory(GsonConverterFactory.create()).build()
    }
    val service = remember { retrofit.create(ApiService::class.java) }

    LaunchedEffect(id) {
        try {
            detail = service.getPokemonDetail(id)
            val listaTiposES = detail?.types?.map { traducirTipo(it.type.name) }
            val tiposTexto = listaTiposES?.joinToString(", ") ?: ""
            val species = service.getPokemonDescription(id)
            val entry = species.flavor_text_entries.firstOrNull { it.language.name == "es" }
                ?: species.flavor_text_entries.firstOrNull { it.language.name == "en" }
            val textoLimpio = entry?.flavor_text?.replace("\n", " ")?.replace("\u000c", " ") ?: ""
            val evolucionInfo = if (species.evolves_from_species != null) {
                "Forma evolucionada de ${species.evolves_from_species.name.uppercase()}. "
            } else { "" }

            description = "TIPO: $tiposTexto.\n$evolucionInfo$textoLimpio"
            speak("$name. Pokémon tipo $tiposTexto. $evolucionInfo $textoLimpio")
        } catch (e: Exception) { description = "Error de conexión." }
    }

    DisposableEffect(Unit) { onDispose { tts.stop(); tts.shutdown() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DETALLES POKÉDEX", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { IconButton(onClick = { speak(description) }) { Icon(Icons.Default.PlayArrow, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = animatedColor // Aplicamos el color animado aquí
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen con sombra y efecto flotante
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(280.dp)) {
                Surface(modifier = Modifier.size(200.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.5f)) {}
                AsyncImage(
                    model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png",
                    contentDescription = null,
                    modifier = Modifier.size(250.dp).offset(y = offsetY.dp) // FLOTANDO
                )
            }

            Text(text = name.uppercase(), fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFF37474F))

            // Chips de Tipos
            Row(modifier = Modifier.padding(10.dp)) {
                detail?.types?.forEach {
                    val tName = it.type.name
                    Surface(
                        modifier = Modifier.padding(4.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = when(tName){
                            "fire" -> Color(0xFFFF5722)
                            "water" -> Color(0xFF2196F3)
                            "grass" -> Color(0xFF4CAF50)
                            else -> Color.Gray
                        }
                    ) {
                        Text(traducirTipo(tName).uppercase(), Modifier.padding(horizontal = 16.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tarjeta de descripción estilizada
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("REGISTRO:", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(Modifier.height(8.dp))
                    Text(text = description, fontSize = 17.sp, lineHeight = 24.sp)
                }
            }
        }
    }
}

@Composable
fun PokemonCard(pokemon: PokemonResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(model = pokemon.imageUrl, contentDescription = null, modifier = Modifier.size(110.dp))
            Spacer(Modifier.height(8.dp))
            Text(text = pokemon.name.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Text(text = "#${pokemon.id}", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun InfoTag(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
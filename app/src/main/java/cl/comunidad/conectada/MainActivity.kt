package cl.comunidad.conectada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF176B61), secondary = Color(0xFF425E78), background = Color(0xFFF5F7F5))) {
                CommunityApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityApp() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    val tabs = listOf("Inicio", "Chat", "Anuncios", "Perfil")
    Scaffold(
        topBar = { TopAppBar(title = { Text("Comunidad Conectada") }) },
        bottomBar = { NavigationBar { tabs.forEachIndexed { index, title ->
            NavigationBarItem(selected = selected == index, onClick = { selected = index }, icon = { Text(listOf("⌂", "C", "A", "P")[index]) }, label = { Text(title) })
        } } }
    ) { padding ->
        Column(Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            SuggestionChip(onClick = {}, label = { Text("Sin conexión a servicios · Versión inicial") })
            when (selected) {
                0 -> {
                    Text("Tu comunidad, más cerca", style = MaterialTheme.typography.headlineMedium)
                    Text("Información y ayuda en un mismo lugar.")
                    ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Alerta comunitaria", style = MaterialTheme.typography.titleLarge)
                        Text("Solicita ayuda a los vecinos conectados. La integración MQTT está pendiente.")
                        Button(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth()) { Text("Solicitar ayuda") }
                    } }
                    Text("Anuncios recientes", style = MaterialTheme.typography.titleLarge)
                    Text("Todavía no hay anuncios. Se mostrarán al conectar Firebase.")
                }
                1 -> { Text("Chat general", style = MaterialTheme.typography.headlineMedium); Text("Aquí se mostrarán los mensajes de tu comunidad en tiempo real. Firebase aún no está configurado.") }
                2 -> { Text("Información oficial", style = MaterialTheme.typography.headlineMedium); Text("Todos podrán leer. Solo el administrador podrá publicar anuncios.") }
                3 -> { Text("Tu perfil", style = MaterialTheme.typography.headlineMedium); Text("El registro solicitará nombre, vivienda, correo y contraseña. Podrá haber varios residentes en la misma vivienda.") }
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Alerta sin enviar") }, text = { Text("Esta versión aún no está conectada al broker MQTT. No se ha enviado ninguna alerta.") }, confirmButton = { TextButton(onClick = { confirm = false }) { Text("Entendido") } })
}

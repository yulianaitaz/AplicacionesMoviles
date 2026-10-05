package co.edu.mipuente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.edu.mipuente.config.teamMembers
import co.edu.mipuente.ui.components.BrandMark
import co.edu.mipuente.ui.components.InfoCard

@Composable
fun AboutScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
                }
                Text("Acerca de", style = MaterialTheme.typography.headlineMedium)
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BrandMark()
                    Text(
                        "Mi Puente Financiero",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                    Text(
                        "Control y ahorro, sin enredos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Aplicación móvil académica para registrar ingresos y gastos, separar finanzas personales y de negocio, visualizar metas de ahorro y consultar una tasa USD/COP de referencia en línea.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }

        item { InfoCard("Jetpack Compose + Material 3", "Interfaz declarativa, consistente y adaptable.", Icons.Rounded.Code) }
        item { InfoCard("ViewModel", "Conserva y coordina el estado observable de la interfaz.", Icons.Rounded.Groups) }
        item { InfoCard("SQLite local", "Movimientos, metas, balances y preferencias persisten en el dispositivo.", Icons.Rounded.Storage) }
        item { InfoCard("Navigation Component", "NavHost y NavController conectan las distintas pantallas.", Icons.Rounded.Route) }
        item { InfoCard("Servicio en línea", "Consulta HTTP de una tasa USD/COP de referencia.", Icons.Rounded.CloudSync) }

        item {
            Text(
                text = "Equipo de desarrollo",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Desarrollado por los integrantes del microproyecto",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(teamMembers.size) { index ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Text(
                    "${index + 1}. ${teamMembers[index]}",
                    modifier = Modifier.padding(18.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        item {
            Text(
                "Versión 1.1 · Microproyecto Android · Universidad del Cauca",
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

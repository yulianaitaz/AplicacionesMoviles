package co.edu.mipuente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.mipuente.ui.components.AccountSelector
import co.edu.mipuente.ui.components.InfoCard
import co.edu.mipuente.ui.viewmodel.FinanceViewModel

@Composable
fun ProfileScreen(
    viewModel: FinanceViewModel,
    onAboutClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Perfil", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Tu cuenta y las preferencias de Mi Puente Financiero.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(state.userName, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Cuenta principal",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            Text("Entorno activo", style = MaterialTheme.typography.titleMedium)
            AccountSelector(state.selectedAccount, viewModel::selectAccount)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Recordatorios de metas y movimientos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications() }
                    )
                }
            }
        }

        item { InfoCard("Seguridad", "Protege tu información financiera", Icons.Rounded.Security) }
        item { InfoCard("Privacidad", "Controla qué datos guarda la app", Icons.Rounded.Lock) }
        item { InfoCard("Mis cuentas", "Personal y negocio en un mismo lugar", Icons.Rounded.AccountBalanceWallet) }
        item { InfoCard("Ayuda", "Preguntas frecuentes y soporte", Icons.Rounded.HelpOutline) }
        item {
            InfoCard(
                title = "Acerca de y créditos",
                subtitle = "Descripción del proyecto, tecnologías e integrantes",
                icon = Icons.Rounded.Info,
                onClick = onAboutClick
            )
        }
    }
}

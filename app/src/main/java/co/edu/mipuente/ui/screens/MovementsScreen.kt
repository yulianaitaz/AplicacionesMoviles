package co.edu.mipuente.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.mipuente.ui.components.AccountSelector
import co.edu.mipuente.ui.components.formatCop
import co.edu.mipuente.ui.model.Movement
import co.edu.mipuente.ui.model.MovementType
import co.edu.mipuente.ui.theme.ExpenseCoral
import co.edu.mipuente.ui.theme.IncomeGreen
import co.edu.mipuente.ui.viewmodel.FinanceViewModel

private enum class MovementFilter(val label: String) {
    ALL("Todos"), INCOME("Ingresos"), EXPENSE("Gastos")
}

@Composable
fun MovementsScreen(viewModel: FinanceViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(MovementFilter.ALL) }
    var expandedId by rememberSaveable { mutableLongStateOf(-1L) }

    val filtered = remember(state.activeMovements, query, filter) {
        state.activeMovements.filter { movement ->
            val matchesQuery = query.isBlank() ||
                movement.title.contains(query, ignoreCase = true) ||
                movement.category.contains(query, ignoreCase = true)
            val matchesType = when (filter) {
                MovementFilter.ALL -> true
                MovementFilter.INCOME -> movement.type == MovementType.INCOME
                MovementFilter.EXPENSE -> movement.type == MovementType.EXPENSE
            }
            matchesQuery && matchesType
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Movimientos", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Busca, filtra y revisa en qué se está moviendo tu plata.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        item {
            AccountSelector(state.selectedAccount, viewModel::selectAccount)
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.FilterAlt, contentDescription = null) },
                placeholder = { Text("Buscar movimiento o categoría") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MovementFilter.entries.forEach { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { filter = option },
                        label = { Text(option.label) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { movement ->
            MovementCard(
                movement = movement,
                expanded = expandedId == movement.id,
                onToggle = { expandedId = if (expandedId == movement.id) -1L else movement.id },
                onDelete = {
                    viewModel.deleteMovement(movement.id)
                    expandedId = -1L
                }
            )
        }

        if (filtered.isEmpty()) {
            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Text(
                        "No encontramos movimientos con ese filtro.",
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MovementCard(
    movement: Movement,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val amountColor = if (movement.type == MovementType.INCOME) IncomeGreen else ExpenseCoral
    val icon = movementIcon(movement.category, movement.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(amountColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = amountColor)
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        movement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${movement.category} · ${movement.dateLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = (if (movement.type == MovementType.INCOME) "+" else "-") + formatCop(movement.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor
                )
            }

            if (expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Toca de nuevo para cerrar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun movementIcon(category: String, type: MovementType): ImageVector = when {
    type == MovementType.INCOME -> Icons.Rounded.TrendingUp
    category.contains("Transporte", true) -> Icons.Rounded.DirectionsBus
    category.contains("Comida", true) || category.contains("Restaurante", true) || category.contains("Café", true) -> Icons.Rounded.Restaurant
    category.contains("Súper", true) -> Icons.Rounded.ShoppingCart
    else -> Icons.Rounded.TrendingDown
}

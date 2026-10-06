package co.edu.mipuente.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.mipuente.ui.components.AccountSelector
import co.edu.mipuente.ui.components.formatCop
import co.edu.mipuente.ui.model.AccountType
import co.edu.mipuente.ui.model.Movement
import co.edu.mipuente.ui.model.MovementType
import co.edu.mipuente.ui.theme.ExpenseCoral
import co.edu.mipuente.ui.theme.IncomeGreen
import co.edu.mipuente.ui.viewmodel.FinanceViewModel

private enum class MovementFilter(val label: String) {
    ALL("Todos"), INCOME("Ingresos"), EXPENSE("Gastos")
}

private val CATEGORIES_LIST = listOf("Comida", "Transporte", "Súper", "Vivienda", "Servicios", "Trabajo", "Negocio", "Otro")

@Composable
fun MovementsScreen(viewModel: FinanceViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(MovementFilter.ALL) }
    var expandedId by rememberSaveable { mutableLongStateOf(-1L) }

    var movementToEdit by remember { mutableStateOf<Movement?>(null) }
    var movementToDelete by remember { mutableStateOf<Movement?>(null) }

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
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Movimientos recientes", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Busca, filtra y revisa en qué se está moviendo tu plata con persistencia local.",
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
                onEdit = { movementToEdit = movement },
                onDelete = { movementToDelete = movement }
            )
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No encontramos movimientos con ese filtro.",
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Diálogos de Edición y Eliminación
    movementToEdit?.let { mov ->
        EditMovementDialog(
            movement = mov,
            onDismiss = { movementToEdit = null },
            onConfirm = { title, category, amount, type, account ->
                viewModel.updateMovement(mov.id, title, category, amount, type, account)
                movementToEdit = null
            }
        )
    }

    movementToDelete?.let { mov ->
        AlertDialog(
            onDismissRequest = { movementToDelete = null },
            title = { Text("Eliminar movimiento") },
            text = {
                Text("¿Deseas eliminar '${mov.title}' por valor de ${formatCop(mov.amount)}? El saldo de la cuenta se recalculará automáticamente.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMovement(mov.id)
                        movementToDelete = null
                        expandedId = -1L
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { movementToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun MovementCard(
    movement: Movement,
    expanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
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
                        "${movement.category} • ${movement.dateLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = (if (movement.type == MovementType.INCOME) "+" else "-") + formatCop(movement.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
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
                        "Cuenta: ${movement.account.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Editar")
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditMovementDialog(
    movement: Movement,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, amount: Long, type: MovementType, account: AccountType) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(movement.title) }
    var category by rememberSaveable { mutableStateOf(movement.category) }
    var amountText by rememberSaveable { mutableStateOf(movement.amount.toString()) }
    var type by rememberSaveable { mutableStateOf(movement.type) }
    var account by rememberSaveable { mutableStateOf(movement.account) }

    val amount = amountText.toLongOrNull() ?: 0L
    val isValid = title.isNotBlank() && amount > 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar movimiento", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título / Concepto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amountText = it },
                    label = { Text("Monto ($ COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Text("Tipo:", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == MovementType.INCOME,
                        onClick = { type = MovementType.INCOME },
                        label = { Text("Ingreso") }
                    )
                    FilterChip(
                        selected = type == MovementType.EXPENSE,
                        onClick = { type = MovementType.EXPENSE },
                        label = { Text("Gasto") }
                    )
                }

                Text("Cuenta:", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = account == AccountType.PERSONAL,
                        onClick = { account = AccountType.PERSONAL },
                        label = { Text("Personal") }
                    )
                    FilterChip(
                        selected = account == AccountType.BUSINESS,
                        onClick = { account = AccountType.BUSINESS },
                        label = { Text("Negocio") }
                    )
                }

                Text("Categoría:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CATEGORIES_LIST) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, category, amount, type, account) },
                enabled = isValid
            ) {
                Text("Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun movementIcon(category: String, type: MovementType): ImageVector = when {
    type == MovementType.INCOME -> Icons.Rounded.TrendingUp
    category.contains("Transporte", true) -> Icons.Rounded.DirectionsBus
    category.contains("Comida", true) || category.contains("Restaurante", true) || category.contains("Café", true) -> Icons.Rounded.Restaurant
    category.contains("Súper", true) -> Icons.Rounded.ShoppingCart
    else -> Icons.Rounded.TrendingDown
}

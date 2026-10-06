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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.mipuente.ui.components.formatCop
import co.edu.mipuente.ui.model.SavingsGoal
import co.edu.mipuente.ui.theme.IncomeGreen
import co.edu.mipuente.ui.viewmodel.FinanceViewModel

private val AVAILABLE_EMOJIS = listOf("✈️", "🛡️", "💻", "🏠", "🚗", "🎓", "🎁", "🚀", "📱", "💼", "🌴", "🎯")

@Composable
fun GoalsScreen(viewModel: FinanceViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var goalToContribute by remember { mutableStateOf<SavingsGoal?>(null) }
    var goalToEdit by remember { mutableStateOf<SavingsGoal?>(null) }
    var goalToDelete by remember { mutableStateOf<SavingsGoal?>(null) }

    val totalSaved = state.goals.sumOf { it.saved }
    val totalTarget = state.goals.sumOf { it.target }
    val overallProgress = if (totalTarget > 0) (totalSaved.toFloat() / totalTarget.toFloat()).coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Mis metas de ahorro", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Persiste y administra tus objetivos de ahorro con almacenamiento local.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tarjeta de Resumen Global de Ahorro
        if (state.goals.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Progreso total de ahorro",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${(overallProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Ahorrado: ${formatCop(totalSaved)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Meta: ${formatCop(totalTarget)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        items(state.goals, key = { it.id }) { goal ->
            GoalCard(
                goal = goal,
                onContribute = { goalToContribute = goal },
                onEdit = { goalToEdit = goal },
                onDelete = { goalToDelete = goal }
            )
        }

        if (state.goals.isEmpty()) {
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
                        Text("🎯", fontSize = 42.sp)
                        Text(
                            "Aún no tienes metas de ahorro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            "Crea tu primera meta para empezar a guardar dinero.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Text("Nueva meta personalizada", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            }
        }
    }

    // ==========================================
    // DIÁLOGOS DE INTERACCIÓN CON BASE DE DATOS
    // ==========================================

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, target, emoji, saved ->
                viewModel.addGoal(name, target, emoji, saved)
                showAddDialog = false
            }
        )
    }

    goalToContribute?.let { goal ->
        ContributeGoalDialog(
            goal = goal,
            onDismiss = { goalToContribute = null },
            onConfirm = { amount ->
                viewModel.contributeToGoal(goal.id, amount)
                goalToContribute = null
            }
        )
    }

    goalToEdit?.let { goal ->
        EditGoalDialog(
            goal = goal,
            onDismiss = { goalToEdit = null },
            onConfirm = { name, target, emoji ->
                viewModel.updateGoal(goal.id, name, target, emoji)
                goalToEdit = null
            }
        )
    }

    goalToDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = { Text("Eliminar meta") },
            text = { Text("¿Estás seguro de que deseas eliminar la meta '${goal.name}' de la base de datos local?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGoal(goal.id)
                        goalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoal,
    onContribute: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val percent = (goal.progress * 100).toInt()
    val isDone = goal.isCompleted

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(goal.emoji, fontSize = 22.sp)
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(
                            goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isDone) "¡Meta completada! 🎉" else "$percent% alcanzado",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDone) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                Text(
                    "$percent%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) IncomeGreen else MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (isDone) IncomeGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${formatCop(goal.saved)} / ${formatCop(goal.target)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!isDone) {
                    Text(
                        "Faltan ${formatCop(goal.remaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Barra de acciones: Abonar, Editar, Eliminar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onContribute,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Savings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Abonar")
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, target: Long, emoji: String, saved: Long) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var targetText by rememberSaveable { mutableStateOf("") }
    var savedText by rememberSaveable { mutableStateOf("") }
    var selectedEmoji by rememberSaveable { mutableStateOf("🎯") }

    val targetAmount = targetText.toLongOrNull() ?: 0L
    val savedAmount = savedText.toLongOrNull() ?: 0L
    val isValid = name.isNotBlank() && targetAmount > 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva meta de ahorro", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del objetivo") },
                    placeholder = { Text("Ej. Viaje, Moto, Ahorro") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) targetText = it },
                    label = { Text("Monto objetivo ($ COP)") },
                    placeholder = { Text("Ej. 2000000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = savedText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) savedText = it },
                    label = { Text("Ahorro inicial ($ COP opcional)") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Text("Selecciona un icono:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AVAILABLE_EMOJIS) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedEmoji == emoji) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, targetAmount, selectedEmoji, savedAmount) },
                enabled = isValid
            ) {
                Text("Guardar en BD")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ContributeGoalDialog(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long) -> Unit
) {
    var amountText by rememberSaveable { mutableStateOf("") }
    val amount = amountText.toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abonar a '${goal.emoji} ${goal.name}'", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Actualmente tienes ${formatCop(goal.saved)} de ${formatCop(goal.target)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amountText = it },
                    label = { Text("Monto a abonar ($ COP)") },
                    placeholder = { Text("Ej. 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(20_000L, 50_000L, 100_000L).forEach { quickVal ->
                        SuggestionChip(
                            onClick = { amountText = quickVal.toString() },
                            label = { Text(formatCop(quickVal)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amount) },
                enabled = amount > 0L
            ) {
                Text("Abonar ahora")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EditGoalDialog(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onConfirm: (name: String, target: Long, emoji: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(goal.name) }
    var targetText by rememberSaveable { mutableStateOf(goal.target.toString()) }
    var selectedEmoji by rememberSaveable { mutableStateOf(goal.emoji) }

    val targetAmount = targetText.toLongOrNull() ?: 0L
    val isValid = name.isNotBlank() && targetAmount > 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar meta de ahorro", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) targetText = it },
                    label = { Text("Monto objetivo ($ COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Text("Icono:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AVAILABLE_EMOJIS) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedEmoji == emoji) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, targetAmount, selectedEmoji) },
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

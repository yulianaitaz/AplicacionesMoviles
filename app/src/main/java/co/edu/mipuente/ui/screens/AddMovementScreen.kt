package co.edu.mipuente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.mipuente.ui.components.AccountSelector
import co.edu.mipuente.ui.components.formatCop
import co.edu.mipuente.ui.model.MovementType
import co.edu.mipuente.ui.theme.ExpenseCoral
import co.edu.mipuente.ui.theme.IncomeGreen
import co.edu.mipuente.ui.viewmodel.FinanceViewModel

@Composable
fun AddMovementScreen(
    viewModel: FinanceViewModel,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val amount = state.amountDraft.toLongOrNull() ?: 0L
    val categories = listOf("Comida", "Transporte", "Súper", "Vivienda", "Servicios", "Trabajo", "Negocio", "Otro")

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Registrar movimiento", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Anota lo que entró o salió con persistencia inmediata en la base de datos local.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            AccountSelector(state.selectedAccount, viewModel::selectAccount)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Monto", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (amount > 0) formatCop(amount) else "$ 0 COP",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        item {
            Text("Categoría", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = state.categoryDraft == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category) }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = state.noteDraft,
                onValueChange = viewModel::updateNote,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción opcional") },
                placeholder = { Text("Ej. Compra supermercado, Pago nómina") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )
        }

        item {
            AmountKeypad(onKey = viewModel::inputAmountKey)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (viewModel.addMovement(MovementType.INCOME)) onSaved()
                    },
                    enabled = amount > 0,
                    modifier = Modifier.weight(1f).height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Rounded.ArrowUpward, contentDescription = null)
                    Text("Entró plata", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (viewModel.addMovement(MovementType.EXPENSE)) onSaved()
                    },
                    enabled = amount > 0,
                    modifier = Modifier.weight(1f).height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseCoral),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Rounded.ArrowDownward, contentDescription = null)
                    Text("Salió plata", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AmountKeypad(onKey: (String) -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    Button(
                        onClick = { onKey(key) },
                        modifier = Modifier.weight(1f).height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(key, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

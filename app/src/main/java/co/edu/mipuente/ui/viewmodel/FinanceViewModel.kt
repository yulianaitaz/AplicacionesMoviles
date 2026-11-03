package co.edu.mipuente.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import co.edu.mipuente.data.FinanceRepository
import co.edu.mipuente.data.remote.ExchangeRateService
import co.edu.mipuente.ui.model.AccountType
import co.edu.mipuente.ui.model.FinanceUiState
import co.edu.mipuente.ui.model.MovementType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(application)
    private val executor = Executors.newSingleThreadExecutor()

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        loadLocalData()
    }

    fun loadLocalData() {
        executor.execute {
            val movements = repository.getMovements()
            val goals = repository.getGoals()
            val personal = repository.getPersonalBalance()
            val business = repository.getBusinessBalance()
            val notifications = repository.notificationsEnabled()
            _uiState.update {
                it.copy(
                    movements = movements,
                    goals = goals,
                    personalBalance = personal,
                    businessBalance = business,
                    notificationsEnabled = notifications,
                    localDatabaseReady = true
                )
            }
        }
    }

    fun selectAccount(accountType: AccountType) {
        _uiState.update { it.copy(selectedAccount = accountType) }
    }

    fun toggleBalanceVisibility() {
        _uiState.update { it.copy(isBalanceVisible = !it.isBalanceVisible) }
    }

    fun inputAmountKey(key: String) {
        _uiState.update { state ->
            val current = state.amountDraft
            val updated = when (key) {
                "⌫", "DEL", "backspace" -> current.dropLast(1)
                "C" -> ""
                else -> if (current.length < 9 && key.all { it.isDigit() }) {
                    (current + key).trimStart('0').ifBlank { "0" }
                } else current
            }
            state.copy(amountDraft = updated)
        }
    }

    fun clearAmount() {
        _uiState.update { it.copy(amountDraft = "") }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(categoryDraft = category) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(noteDraft = note) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    // ==========================================
    // MOVIMIENTOS (CRUD)
    // ==========================================

    fun addMovement(type: MovementType): Boolean {
        val snapshot = _uiState.value
        val amount = snapshot.amountDraft.toLongOrNull() ?: return false
        if (amount <= 0L) return false

        val title = snapshot.noteDraft.ifBlank {
            when (snapshot.categoryDraft) {
                "Comida" -> "Compra de comida"
                "Transporte" -> "Transporte"
                "Súper" -> "Supermercado"
                "Vivienda" -> "Vivienda"
                "Servicios" -> "Pago de servicios"
                "Trabajo" -> "Ingreso de trabajo"
                else -> snapshot.categoryDraft
            }
        }
        val account = snapshot.selectedAccount

        executor.execute {
            val movement = repository.insertMovement(
                title = title,
                category = snapshot.categoryDraft,
                amount = amount,
                type = type,
                account = account,
                dateLabel = "Ahora"
            )
            val personal = repository.getPersonalBalance()
            val business = repository.getBusinessBalance()
            val allMovements = repository.getMovements()

            _uiState.update { state ->
                state.copy(
                    personalBalance = personal,
                    businessBalance = business,
                    movements = allMovements,
                    amountDraft = "",
                    noteDraft = "",
                    statusMessage = "Movimiento registrado con éxito."
                )
            }
        }
        return true
    }

    fun updateMovement(
        id: Long,
        title: String,
        category: String,
        amount: Long,
        type: MovementType,
        account: AccountType
    ): Boolean {
        if (amount <= 0L || title.isBlank()) return false
        executor.execute {
            val success = repository.updateMovement(
                id = id,
                title = title,
                category = category,
                amount = amount,
                type = type,
                account = account
            )
            if (success) {
                val personal = repository.getPersonalBalance()
                val business = repository.getBusinessBalance()
                val allMovements = repository.getMovements()
                _uiState.update { state ->
                    state.copy(
                        personalBalance = personal,
                        businessBalance = business,
                        movements = allMovements,
                        statusMessage = "Movimiento actualizado."
                    )
                }
            }
        }
        return true
    }

    fun deleteMovement(id: Long) {
        executor.execute {
            val success = repository.deleteMovement(id)
            if (success) {
                val personal = repository.getPersonalBalance()
                val business = repository.getBusinessBalance()
                val allMovements = repository.getMovements()
                _uiState.update { state ->
                    state.copy(
                        personalBalance = personal,
                        businessBalance = business,
                        movements = allMovements,
                        statusMessage = "Movimiento eliminado."
                    )
                }
            }
        }
    }

    // ==========================================
    // METAS DE AHORRO (CRUD)
    // ==========================================

    fun addGoal(name: String, target: Long, emoji: String, initialSaved: Long = 0L): Boolean {
        if (name.isBlank() || target <= 0L) return false
        executor.execute {
            val goal = repository.insertGoal(
                name = name.trim(),
                target = target,
                emoji = emoji.ifBlank { "🎯" },
                initialSaved = initialSaved.coerceAtLeast(0L)
            )
            val goals = repository.getGoals()
            _uiState.update {
                it.copy(
                    goals = goals,
                    statusMessage = "Meta '${goal.name}' creada exitosamente."
                )
            }
        }
        return true
    }

    fun updateGoal(id: Long, name: String, target: Long, emoji: String): Boolean {
        if (name.isBlank() || target <= 0L) return false
        executor.execute {
            val success = repository.updateGoal(id, name.trim(), target, emoji.ifBlank { "🎯" })
            if (success) {
                val goals = repository.getGoals()
                _uiState.update {
                    it.copy(
                        goals = goals,
                        statusMessage = "Meta actualizada."
                    )
                }
            }
        }
        return true
    }

    fun contributeToGoal(id: Long, amount: Long): Boolean {
        if (amount <= 0L) return false
        executor.execute {
            val success = repository.contributeToGoal(id, amount)
            if (success) {
                val goals = repository.getGoals()
                _uiState.update {
                    it.copy(
                        goals = goals,
                        statusMessage = "¡Abono registrado a tu meta!"
                    )
                }
            }
        }
        return true
    }

    fun deleteGoal(id: Long) {
        executor.execute {
            val success = repository.deleteGoal(id)
            if (success) {
                val goals = repository.getGoals()
                _uiState.update {
                    it.copy(
                        goals = goals,
                        statusMessage = "Meta eliminada."
                    )
                }
            }
        }
    }

    fun addDemoGoal() {
        addGoal("Nueva meta", 1_000_000L, "🎯", 0L)
    }

    fun recalculateBalances() {
        executor.execute {
            val (personal, business) = repository.recalculateBalances()
            _uiState.update {
                it.copy(
                    personalBalance = personal,
                    businessBalance = business,
                    statusMessage = "Saldos recalculados e integridad verificada."
                )
            }
        }
    }

    fun toggleNotifications() {
        val newValue = !_uiState.value.notificationsEnabled
        executor.execute {
            repository.saveNotifications(newValue)
            _uiState.update { it.copy(notificationsEnabled = newValue) }
        }
    }

    fun refreshExchangeRate() {
        if (_uiState.value.exchangeRateLoading) return
        _uiState.update {
            it.copy(
                exchangeRateLoading = true,
                exchangeRateMessage = "Consultando servicio en línea..."
            )
        }

        executor.execute {
            runCatching { ExchangeRateService.fetchUsdCop() }
                .onSuccess { rate ->
                    _uiState.update {
                        it.copy(
                            usdCopRate = rate,
                            exchangeRateLoading = false,
                            exchangeRateMessage = "Actualizado desde un servicio HTTP público."
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            exchangeRateLoading = false,
                            exchangeRateMessage = "No fue posible actualizar. Verifica la conexión a Internet."
                        )
                    }
                }
        }
    }

    override fun onCleared() {
        executor.shutdownNow()
        super.onCleared()
    }
}

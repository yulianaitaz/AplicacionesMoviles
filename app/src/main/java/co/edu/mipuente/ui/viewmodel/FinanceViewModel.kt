package co.edu.mipuente.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import co.edu.mipuente.data.FinanceRepository
import co.edu.mipuente.data.remote.ExchangeRateService
import co.edu.mipuente.ui.model.AccountType
import co.edu.mipuente.ui.model.FinanceUiState
import co.edu.mipuente.ui.model.Movement
import co.edu.mipuente.ui.model.MovementType
import co.edu.mipuente.ui.model.SavingsGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import co.edu.mipuente.ui.model.ExchangeRateState

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(application)

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    private class LocalSnapshot(
        val movements: List<Movement>,
        val goals: List<SavingsGoal>,
        val personal: Long,
        val business: Long,
        val notifications: Boolean
    )

    init {
        loadLocalData()
    }

    private fun loadLocalData() {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                LocalSnapshot(
                    movements = repository.getMovements(),
                    goals = repository.getGoals(),
                    personal = repository.getPersonalBalance(),
                    business = repository.getBusinessBalance(),
                    notifications = repository.notificationsEnabled()
                )
            }
            _uiState.update {
                it.copy(
                    movements = data.movements,
                    goals = data.goals,
                    personalBalance = data.personal,
                    businessBalance = data.business,
                    notificationsEnabled = data.notifications,
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
                "⌫" -> current.dropLast(1)
                "C" -> ""
                else -> if (current.length < 9) (current + key).trimStart('0').ifBlank { "0" } else current
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

        val movement = repository.insertMovement(
            title = title,
            category = snapshot.categoryDraft,
            amount = amount,
            type = type,
            account = snapshot.selectedAccount,
            dateLabel = "Ahora"
        )

        _uiState.update { state ->
            state.copy(
                personalBalance = repository.getPersonalBalance(),
                businessBalance = repository.getBusinessBalance(),
                movements = listOf(movement) + state.movements,
                amountDraft = "",
                noteDraft = ""
            )
        }
        return true
    }

    fun deleteMovement(id: Long) {
        val movement = _uiState.value.movements.firstOrNull { it.id == id } ?: return
        repository.deleteMovement(movement)
        _uiState.update { state ->
            state.copy(
                personalBalance = repository.getPersonalBalance(),
                businessBalance = repository.getBusinessBalance(),
                movements = state.movements.filterNot { it.id == id }
            )
        }
    }

    fun addDemoGoal() {
        val goal = repository.insertGoal(name = "Nueva meta", target = 1_000_000, emoji = "🎯")
        _uiState.update { it.copy(goals = it.goals + goal) }
    }

    fun toggleNotifications() {
        val newValue = !_uiState.value.notificationsEnabled
        repository.saveNotifications(newValue)
        _uiState.update { it.copy(notificationsEnabled = newValue) }
    }

    fun refreshExchangeRate() {
    if (_uiState.value.exchangeRate is ExchangeRateState.Loading) return
    _uiState.update { it.copy(exchangeRate = ExchangeRateState.Loading) }
    viewModelScope.launch {
        runCatching {ExchangeRateService.fetchUsdCop() }
            .onSuccess { rate ->
                _uiState.update { it.copy(exchangeRate = ExchangeRateState.Success(rate)) }
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        exchangeRate = ExchangeRateState.Error(
                            "No fue posible actualizar. Verifica la conexión a Internet."
                        )
                    )
                }
            }
    }
}

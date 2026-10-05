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

    private fun loadLocalData() {
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
        val account = snapshot.selectedAccount

        // Para el microproyecto la escritura es pequeña; se refleja en UI y se persiste inmediatamente.
        val movement = repository.insertMovement(
            title = title,
            category = snapshot.categoryDraft,
            amount = amount,
            type = type,
            account = account,
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
        val goal = repository.insertGoal(
            name = "Nueva meta",
            target = 1_000_000,
            emoji = "🎯"
        )
        _uiState.update { it.copy(goals = it.goals + goal) }
    }

    fun toggleNotifications() {
        val newValue = !_uiState.value.notificationsEnabled
        repository.saveNotifications(newValue)
        _uiState.update { it.copy(notificationsEnabled = newValue) }
    }

    fun refreshExchangeRate() {
        if (_uiState.value.exchangeRateLoading) return
        _uiState.update {
            it.copy(
                exchangeRateLoading = true,
                exchangeRateMessage = "Consultando servicio en línea…"
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

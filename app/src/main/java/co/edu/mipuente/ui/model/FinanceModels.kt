package co.edu.mipuente.ui.model

enum class AccountType(val label: String) {
    PERSONAL("Personal"),
    BUSINESS("Negocio")
}

enum class MovementType {
    INCOME,
    EXPENSE
}

data class Movement(
    val id: Long,
    val title: String,
    val category: String,
    val amount: Long,
    val type: MovementType,
    val account: AccountType = AccountType.PERSONAL,
    val dateLabel: String
)

data class SavingsGoal(
    val id: Long,
    val name: String,
    val saved: Long,
    val target: Long,
    val emoji: String
) {
    val progress: Float
        get() = if (target <= 0L) 0f else (saved.toFloat() / target.toFloat()).coerceIn(0f, 1f)
}

sealed interface ExchangeRateState {
    data object Idle : ExchangeRateState
    data object Loading : ExchangeRateState
    data class Success(val rate: Double) : ExchangeRateState
    data class Error(val message: String) : ExchangeRateState
}

data class FinanceUiState(
    val userName: String = "Alejandra",
    val selectedAccount: AccountType = AccountType.PERSONAL,
    val personalBalance: Long = 2_500_000,
    val businessBalance: Long = 2_365_000,
    val isBalanceVisible: Boolean = true,
    val amountDraft: String = "50000",
    val categoryDraft: String = "Comida",
    val noteDraft: String = "",
    val movements: List<Movement> = emptyList(),
    val goals: List<SavingsGoal> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val exchangeRate: ExchangeRateState = ExchangeRateState.Idle,
    val localDatabaseReady: Boolean = false
) {
    val activeBalance: Long
        get() = if (selectedAccount == AccountType.PERSONAL) personalBalance else businessBalance

    val activeMovements: List<Movement>
        get() = movements.filter { it.account == selectedAccount }
}

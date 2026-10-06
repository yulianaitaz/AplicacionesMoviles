package co.edu.mipuente.data

import android.content.Context
import co.edu.mipuente.data.local.FinanceDbHelper
import co.edu.mipuente.ui.model.AccountType
import co.edu.mipuente.ui.model.Movement
import co.edu.mipuente.ui.model.MovementType
import co.edu.mipuente.ui.model.MovementsSummary
import co.edu.mipuente.ui.model.SavingsGoal

class FinanceRepository(context: Context) {
    private val db = FinanceDbHelper(context.applicationContext)

    // ==========================================
    // MOVIMIENTOS
    // ==========================================

    fun getMovements(account: AccountType? = null): List<Movement> = db.getMovements(account)

    fun getMovementById(id: Long): Movement? = db.getMovementById(id)

    fun insertMovement(
        title: String,
        category: String,
        amount: Long,
        type: MovementType,
        account: AccountType,
        dateLabel: String
    ): Movement {
        val id = db.insertMovement(title, category, amount, type, account, dateLabel)
        return Movement(
            id = id,
            title = title,
            category = category,
            amount = amount,
            type = type,
            account = account,
            dateLabel = dateLabel,
            createdAt = System.currentTimeMillis()
        )
    }

    fun updateMovement(
        id: Long,
        title: String,
        category: String,
        amount: Long,
        type: MovementType,
        account: AccountType,
        dateLabel: String? = null
    ): Boolean {
        return db.updateMovement(id, title, category, amount, type, account, dateLabel)
    }

    fun deleteMovement(movementId: Long): Boolean {
        return db.deleteMovement(movementId)
    }

    fun getMovementsSummary(account: AccountType): MovementsSummary {
        return db.calculateMovementsSummary(account)
    }

    // ==========================================
    // METAS DE AHORRO
    // ==========================================

    fun getGoals(): List<SavingsGoal> = db.getGoals()

    fun getGoalById(id: Long): SavingsGoal? = db.getGoalById(id)

    fun insertGoal(name: String, target: Long, emoji: String, initialSaved: Long = 0L): SavingsGoal {
        val id = db.insertGoal(name = name, saved = initialSaved, target = target, emoji = emoji)
        return SavingsGoal(
            id = id,
            name = name,
            saved = initialSaved,
            target = target,
            emoji = emoji,
            createdAt = System.currentTimeMillis()
        )
    }

    fun updateGoal(id: Long, name: String, target: Long, emoji: String, saved: Long? = null): Boolean {
        return db.updateGoal(id, name, target, emoji, saved)
    }

    fun contributeToGoal(id: Long, amount: Long): Boolean {
        return db.addAmountToGoal(id, amount)
    }

    fun deleteGoal(id: Long): Boolean {
        return db.deleteGoal(id)
    }

    // ==========================================
    // BALANCES Y CONFIGURACIÓN
    // ==========================================

    fun getPersonalBalance(): Long = db.getLongSetting(FinanceDbHelper.KEY_PERSONAL_BALANCE, 2_500_000L)

    fun getBusinessBalance(): Long = db.getLongSetting(FinanceDbHelper.KEY_BUSINESS_BALANCE, 2_365_000L)

    fun setPersonalBalance(balance: Long) = db.setLongSetting(FinanceDbHelper.KEY_PERSONAL_BALANCE, balance)

    fun setBusinessBalance(balance: Long) = db.setLongSetting(FinanceDbHelper.KEY_BUSINESS_BALANCE, balance)

    fun recalculateBalances(): Pair<Long, Long> = db.recalculateBalancesFromMovements()

    fun notificationsEnabled(): Boolean = db.getBooleanSetting(FinanceDbHelper.KEY_NOTIFICATIONS, true)

    fun saveNotifications(enabled: Boolean) = db.setBooleanSetting(FinanceDbHelper.KEY_NOTIFICATIONS, enabled)
}

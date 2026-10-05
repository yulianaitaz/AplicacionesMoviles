package co.edu.mipuente.data

import android.content.Context
import co.edu.mipuente.data.local.FinanceDbHelper
import co.edu.mipuente.ui.model.AccountType
import co.edu.mipuente.ui.model.Movement
import co.edu.mipuente.ui.model.MovementType
import co.edu.mipuente.ui.model.SavingsGoal

class FinanceRepository(context: Context) {
    private val db = FinanceDbHelper(context.applicationContext)

    fun getMovements(): List<Movement> = db.getMovements()
    fun getGoals(): List<SavingsGoal> = db.getGoals()

    fun getPersonalBalance(): Long = db.getLongSetting(FinanceDbHelper.KEY_PERSONAL_BALANCE, 2_500_000)
    fun getBusinessBalance(): Long = db.getLongSetting(FinanceDbHelper.KEY_BUSINESS_BALANCE, 2_365_000)
    fun notificationsEnabled(): Boolean = db.getBooleanSetting(FinanceDbHelper.KEY_NOTIFICATIONS, true)

    fun saveNotifications(enabled: Boolean) = db.setBooleanSetting(FinanceDbHelper.KEY_NOTIFICATIONS, enabled)

    fun insertMovement(
        title: String,
        category: String,
        amount: Long,
        type: MovementType,
        account: AccountType,
        dateLabel: String
    ): Movement {
        val id = db.insertMovement(title, category, amount, type, account, dateLabel)
        val signed = if (type == MovementType.INCOME) amount else -amount
        if (account == AccountType.PERSONAL) {
            db.setLongSetting(FinanceDbHelper.KEY_PERSONAL_BALANCE, getPersonalBalance() + signed)
        } else {
            db.setLongSetting(FinanceDbHelper.KEY_BUSINESS_BALANCE, getBusinessBalance() + signed)
        }
        return Movement(id, title, category, amount, type, account, dateLabel)
    }

    fun deleteMovement(movement: Movement) {
        if (db.deleteMovement(movement.id) > 0) {
            val inverse = if (movement.type == MovementType.INCOME) -movement.amount else movement.amount
            if (movement.account == AccountType.PERSONAL) {
                db.setLongSetting(FinanceDbHelper.KEY_PERSONAL_BALANCE, getPersonalBalance() + inverse)
            } else {
                db.setLongSetting(FinanceDbHelper.KEY_BUSINESS_BALANCE, getBusinessBalance() + inverse)
            }
        }
    }

    fun insertGoal(name: String, target: Long, emoji: String): SavingsGoal {
        val id = db.insertGoal(name = name, saved = 0, target = target, emoji = emoji)
        return SavingsGoal(id, name, 0, target, emoji)
    }
}

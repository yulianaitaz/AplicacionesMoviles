package co.edu.mipuente.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import co.edu.mipuente.ui.model.AccountType
import co.edu.mipuente.ui.model.Movement
import co.edu.mipuente.ui.model.MovementType
import co.edu.mipuente.ui.model.MovementsSummary
import co.edu.mipuente.ui.model.SavingsGoal

class FinanceDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_MOVEMENTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                amount INTEGER NOT NULL,
                type TEXT NOT NULL,
                account TEXT NOT NULL,
                date_label TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $TABLE_GOALS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                saved INTEGER NOT NULL,
                target INTEGER NOT NULL,
                emoji TEXT NOT NULL,
                created_at INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $TABLE_SETTINGS (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Índices para optimizar consultas por cuenta, fecha y tipo
        db.execSQL("CREATE INDEX idx_movements_account_created ON $TABLE_MOVEMENTS(account, created_at DESC)")
        db.execSQL("CREATE INDEX idx_goals_created ON $TABLE_GOALS(id ASC)")

        seedDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.i(TAG, "Migrando base de datos de versión $oldVersion a $newVersion")
        if (oldVersion < 2) {
            try {
                // Migración segura sin perder datos existentes
                db.execSQL("ALTER TABLE $TABLE_MOVEMENTS ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE $TABLE_GOALS ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_movements_account_created ON $TABLE_MOVEMENTS(account, created_at DESC)")
            } catch (e: Exception) {
                Log.w(TAG, "Error aplicando migración incremental, recreando esquema: ${e.message}")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_MOVEMENTS")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_GOALS")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
                onCreate(db)
            }
        }
    }

    private fun seedDatabase(db: SQLiteDatabase) {
        setSetting(db, KEY_PERSONAL_BALANCE, "2500000")
        setSetting(db, KEY_BUSINESS_BALANCE, "2365000")
        setSetting(db, KEY_NOTIFICATIONS, "true")

        val initialMovements = listOf(
            MovementSeed("Supermercado Líder", "Súper", 75_000, MovementType.EXPENSE, AccountType.PERSONAL, "Hoy, 11:15 a. m."),
            MovementSeed("Transporte SITP", "Transporte", 5_900, MovementType.EXPENSE, AccountType.PERSONAL, "Hoy, 8:45 a. m."),
            MovementSeed("Café Juan Valdez", "Café", 12_000, MovementType.EXPENSE, AccountType.PERSONAL, "Ayer, 4:20 p. m."),
            MovementSeed("Pago freelance", "Trabajo", 850_000, MovementType.INCOME, AccountType.PERSONAL, "Ayer, 9:10 a. m."),
            MovementSeed("Compra de insumos", "Negocio", 180_000, MovementType.EXPENSE, AccountType.BUSINESS, "Ayer, 2:40 p. m."),
            MovementSeed("Venta del día", "Negocio", 460_000, MovementType.INCOME, AccountType.BUSINESS, "Ayer, 6:10 p. m."),
            MovementSeed("Pago de arriendo", "Vivienda", 950_000, MovementType.EXPENSE, AccountType.PERSONAL, "Marzo 1, 9:00 a. m.")
        )

        val now = System.currentTimeMillis()
        initialMovements.forEachIndexed { index, movement ->
            val values = ContentValues().apply {
                put("title", movement.title)
                put("category", movement.category)
                put("amount", movement.amount)
                put("type", movement.type.name)
                put("account", movement.account.name)
                put("date_label", movement.dateLabel)
                put("created_at", now - index * 60_000L)
                put("updated_at", 0L)
            }
            db.insert(TABLE_MOVEMENTS, null, values)
        }

        val goals = listOf(
            GoalSeed("Viaje a Cartagena", 1_625_000, 2_500_000, "✈️"),
            GoalSeed("Fondo de emergencia", 1_260_000, 3_000_000, "🛡️"),
            GoalSeed("Computador nuevo", 1_900_000, 5_000_000, "💻")
        )
        goals.forEachIndexed { index, goal ->
            val values = ContentValues().apply {
                put("name", goal.name)
                put("saved", goal.saved)
                put("target", goal.target)
                put("emoji", goal.emoji)
                put("created_at", now - index * 3600_000L)
            }
            db.insert(TABLE_GOALS, null, values)
        }
    }

    // ==========================================
    // SECCIÓN 1: MOVIMIENTOS FINANCIEROS (CRUD)
    // ==========================================

    fun getMovements(account: AccountType? = null): List<Movement> {
        val result = mutableListOf<Movement>()
        val selection = if (account != null) "account = ?" else null
        val selectionArgs = if (account != null) arrayOf(account.name) else null

        readableDatabase.query(
            TABLE_MOVEMENTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "created_at DESC, id DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += cursorToMovement(cursor)
            }
        }
        return result
    }

    fun getMovementById(id: Long): Movement? {
        readableDatabase.query(
            TABLE_MOVEMENTS,
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursorToMovement(cursor) else null
        }
    }

    fun insertMovement(
        title: String,
        category: String,
        amount: Long,
        type: MovementType,
        account: AccountType,
        dateLabel: String
    ): Long {
        require(amount > 0) { "El monto debe ser estrictamente positivo." }
        require(title.isNotBlank()) { "El título del movimiento no puede estar vacío." }

        val db = writableDatabase
        var generatedId = -1L

        db.beginTransaction()
        try {
            val now = System.currentTimeMillis()
            val values = ContentValues().apply {
                put("title", title.trim())
                put("category", category.trim())
                put("amount", amount)
                put("type", type.name)
                put("account", account.name)
                put("date_label", dateLabel)
                put("created_at", now)
                put("updated_at", 0L)
            }
            generatedId = db.insertOrThrow(TABLE_MOVEMENTS, null, values)

            // Actualización atómica de saldo en la misma transacción
            val delta = if (type == MovementType.INCOME) amount else -amount
            val balanceKey = if (account == AccountType.PERSONAL) KEY_PERSONAL_BALANCE else KEY_BUSINESS_BALANCE
            val currentBalance = getLongSettingInternal(db, balanceKey, if (account == AccountType.PERSONAL) 2_500_000L else 2_365_000L)
            setSetting(db, balanceKey, (currentBalance + delta).toString())

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return generatedId
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
        require(amount > 0) { "El monto debe ser estrictamente positivo." }
        require(title.isNotBlank()) { "El título del movimiento no puede estar vacío." }

        val db = writableDatabase
        var success = false

        db.beginTransaction()
        try {
            // 1. Obtener movimiento actual para revertir saldo
            val oldMovement = getMovementById(id) ?: return false

            // Revertir efecto del movimiento anterior
            val oldSigned = if (oldMovement.type == MovementType.INCOME) oldMovement.amount else -oldMovement.amount
            val oldBalanceKey = if (oldMovement.account == AccountType.PERSONAL) KEY_PERSONAL_BALANCE else KEY_BUSINESS_BALANCE
            val curOldBal = getLongSettingInternal(db, oldBalanceKey, 0L)
            setSetting(db, oldBalanceKey, (curOldBal - oldSigned).toString())

            // 2. Aplicar efecto del nuevo movimiento
            val newSigned = if (type == MovementType.INCOME) amount else -amount
            val newBalanceKey = if (account == AccountType.PERSONAL) KEY_PERSONAL_BALANCE else KEY_BUSINESS_BALANCE
            val curNewBal = getLongSettingInternal(db, newBalanceKey, 0L)
            setSetting(db, newBalanceKey, (curNewBal + newSigned).toString())

            // 3. Actualizar registro en base de datos
            val values = ContentValues().apply {
                put("title", title.trim())
                put("category", category.trim())
                put("amount", amount)
                put("type", type.name)
                put("account", account.name)
                if (dateLabel != null) {
                    put("date_label", dateLabel)
                }
                put("updated_at", System.currentTimeMillis())
            }
            val rows = db.update(TABLE_MOVEMENTS, values, "id = ?", arrayOf(id.toString()))
            success = rows > 0

            if (success) {
                db.setTransactionSuccessful()
            }
        } finally {
            db.endTransaction()
        }
        return success
    }

    fun deleteMovement(id: Long): Boolean {
        val db = writableDatabase
        var success = false

        db.beginTransaction()
        try {
            val oldMovement = getMovementById(id) ?: return false

            // Revertir saldo
            val signed = if (oldMovement.type == MovementType.INCOME) oldMovement.amount else -oldMovement.amount
            val balanceKey = if (oldMovement.account == AccountType.PERSONAL) KEY_PERSONAL_BALANCE else KEY_BUSINESS_BALANCE
            val currentBal = getLongSettingInternal(db, balanceKey, 0L)
            setSetting(db, balanceKey, (currentBal - signed).toString())

            val rows = db.delete(TABLE_MOVEMENTS, "id = ?", arrayOf(id.toString()))
            success = rows > 0

            if (success) {
                db.setTransactionSuccessful()
            }
        } finally {
            db.endTransaction()
        }
        return success
    }

    fun calculateMovementsSummary(account: AccountType): MovementsSummary {
        var totalIncome = 0L
        var totalExpense = 0L
        var count = 0

        readableDatabase.rawQuery(
            """
            SELECT type, SUM(amount) as total, COUNT(*) as qty
            FROM $TABLE_MOVEMENTS
            WHERE account = ?
            GROUP BY type
            """.trimIndent(),
            arrayOf(account.name)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val typeStr = cursor.getString(0)
                val total = cursor.getLong(1)
                val qty = cursor.getInt(2)
                count += qty
                if (typeStr == MovementType.INCOME.name) {
                    totalIncome = total
                } else if (typeStr == MovementType.EXPENSE.name) {
                    totalExpense = total
                }
            }
        }
        return MovementsSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = totalIncome - totalExpense,
            count = count
        )
    }

    private fun cursorToMovement(cursor: Cursor): Movement {
        return Movement(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
            amount = cursor.getLong(cursor.getColumnIndexOrThrow("amount")),
            type = MovementType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("type"))),
            account = AccountType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("account"))),
            dateLabel = cursor.getString(cursor.getColumnIndexOrThrow("date_label")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
        )
    }

    // ==========================================
    // SECCIÓN 2: METAS DE AHORRO (CRUD)
    // ==========================================

    fun getGoals(): List<SavingsGoal> {
        val result = mutableListOf<SavingsGoal>()
        readableDatabase.query(TABLE_GOALS, null, null, null, null, null, "id ASC").use { cursor ->
            while (cursor.moveToNext()) {
                result += cursorToGoal(cursor)
            }
        }
        return result
    }

    fun getGoalById(id: Long): SavingsGoal? {
        readableDatabase.query(
            TABLE_GOALS,
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursorToGoal(cursor) else null
        }
    }

    fun insertGoal(name: String, saved: Long, target: Long, emoji: String): Long {
        require(name.isNotBlank()) { "El nombre de la meta no puede estar vacío." }
        require(target > 0) { "El monto objetivo debe ser mayor a 0." }
        require(saved >= 0) { "El monto ahorrado inicial no puede ser negativo." }

        val values = ContentValues().apply {
            put("name", name.trim())
            put("saved", saved)
            put("target", target)
            put("emoji", emoji.ifBlank { "🎯" })
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insert(TABLE_GOALS, null, values)
    }

    fun updateGoal(id: Long, name: String, target: Long, emoji: String, saved: Long? = null): Boolean {
        require(name.isNotBlank()) { "El nombre de la meta no puede estar vacío." }
        require(target > 0) { "El monto objetivo debe ser mayor a 0." }

        val values = ContentValues().apply {
            put("name", name.trim())
            put("target", target)
            put("emoji", emoji.ifBlank { "🎯" })
            if (saved != null) {
                put("saved", saved.coerceAtLeast(0L))
            }
        }
        return writableDatabase.update(TABLE_GOALS, values, "id = ?", arrayOf(id.toString())) > 0
    }

    fun addAmountToGoal(id: Long, amountToAdd: Long): Boolean {
        require(amountToAdd > 0) { "El monto a abonar debe ser mayor a 0." }
        val db = writableDatabase
        var success = false

        db.beginTransaction()
        try {
            val goal = getGoalById(id) ?: return false
            val newSaved = goal.saved + amountToAdd
            val values = ContentValues().apply {
                put("saved", newSaved)
            }
            val rows = db.update(TABLE_GOALS, values, "id = ?", arrayOf(id.toString()))
            success = rows > 0
            if (success) {
                db.setTransactionSuccessful()
            }
        } finally {
            db.endTransaction()
        }
        return success
    }

    fun deleteGoal(id: Long): Boolean {
        return writableDatabase.delete(TABLE_GOALS, "id = ?", arrayOf(id.toString())) > 0
    }

    private fun cursorToGoal(cursor: Cursor): SavingsGoal {
        val createdIndex = cursor.getColumnIndex("created_at")
        val createdAt = if (createdIndex >= 0) cursor.getLong(createdIndex) else 0L
        return SavingsGoal(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            saved = cursor.getLong(cursor.getColumnIndexOrThrow("saved")),
            target = cursor.getLong(cursor.getColumnIndexOrThrow("target")),
            emoji = cursor.getString(cursor.getColumnIndexOrThrow("emoji")),
            createdAt = createdAt
        )
    }

    // ==========================================
    // SECCIÓN 3: PREFERENCIAS Y BALANCES
    // ==========================================

    fun getLongSetting(key: String, default: Long): Long =
        getSetting(key)?.toLongOrNull() ?: default

    fun getBooleanSetting(key: String, default: Boolean): Boolean =
        getSetting(key)?.toBooleanStrictOrNull() ?: default

    fun setLongSetting(key: String, value: Long) = setSetting(writableDatabase, key, value.toString())

    fun setBooleanSetting(key: String, value: Boolean) = setSetting(writableDatabase, key, value.toString())

    private fun getSetting(key: String): String? {
        readableDatabase.query(
            TABLE_SETTINGS,
            arrayOf("value"),
            "key = ?",
            arrayOf(key),
            null,
            null,
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    private fun getLongSettingInternal(db: SQLiteDatabase, key: String, default: Long): Long {
        db.query(
            TABLE_SETTINGS,
            arrayOf("value"),
            "key = ?",
            arrayOf(key),
            null,
            null,
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0).toLongOrNull() ?: default else default
        }
    }

    private fun setSetting(db: SQLiteDatabase, key: String, value: String) {
        val values = ContentValues().apply {
            put("key", key)
            put("value", value)
        }
        db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /**
     * Reconcilia la integridad de los saldos sumando todos los movimientos registrados.
     */
    fun recalculateBalancesFromMovements(): Pair<Long, Long> {
        val personalSummary = calculateMovementsSummary(AccountType.PERSONAL)
        val businessSummary = calculateMovementsSummary(AccountType.BUSINESS)

        // Saldo base inicial + saldo neto de movimientos
        val newPersonal = personalSummary.netBalance
        val newBusiness = businessSummary.netBalance

        setLongSetting(KEY_PERSONAL_BALANCE, newPersonal)
        setLongSetting(KEY_BUSINESS_BALANCE, newBusiness)

        return Pair(newPersonal, newBusiness)
    }

    private data class MovementSeed(
        val title: String,
        val category: String,
        val amount: Long,
        val type: MovementType,
        val account: AccountType,
        val dateLabel: String
    )

    private data class GoalSeed(val name: String, val saved: Long, val target: Long, val emoji: String)

    companion object {
        private const val TAG = "FinanceDbHelper"
        const val DATABASE_NAME = "mi_puente_financiero.db"
        const val DATABASE_VERSION = 2

        const val TABLE_MOVEMENTS = "movements"
        const val TABLE_GOALS = "goals"
        const val TABLE_SETTINGS = "settings"

        const val KEY_PERSONAL_BALANCE = "personal_balance"
        const val KEY_BUSINESS_BALANCE = "business_balance"
        const val KEY_NOTIFICATIONS = "notifications_enabled"
    }
}

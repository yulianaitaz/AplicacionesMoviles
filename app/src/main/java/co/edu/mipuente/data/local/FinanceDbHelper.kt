package co.edu.mipuente.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import co.edu.mipuente.ui.model.AccountType
import co.edu.mipuente.ui.model.Movement
import co.edu.mipuente.ui.model.MovementType
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
                created_at INTEGER NOT NULL
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
                emoji TEXT NOT NULL
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

        seedDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MOVEMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GOALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        onCreate(db)
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

        initialMovements.forEachIndexed { index, movement ->
            val values = ContentValues().apply {
                put("title", movement.title)
                put("category", movement.category)
                put("amount", movement.amount)
                put("type", movement.type.name)
                put("account", movement.account.name)
                put("date_label", movement.dateLabel)
                put("created_at", System.currentTimeMillis() - index * 60_000L)
            }
            db.insert(TABLE_MOVEMENTS, null, values)
        }

        val goals = listOf(
            GoalSeed("Viaje a Cartagena", 1_625_000, 2_500_000, "✈️"),
            GoalSeed("Fondo de emergencia", 1_260_000, 3_000_000, "🛟"),
            GoalSeed("Computador nuevo", 1_900_000, 5_000_000, "💻")
        )
        goals.forEach { goal ->
            val values = ContentValues().apply {
                put("name", goal.name)
                put("saved", goal.saved)
                put("target", goal.target)
                put("emoji", goal.emoji)
            }
            db.insert(TABLE_GOALS, null, values)
        }
    }

    fun getMovements(): List<Movement> {
        val result = mutableListOf<Movement>()
        readableDatabase.query(
            TABLE_MOVEMENTS,
            null,
            null,
            null,
            null,
            null,
            "created_at DESC"
        ).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow("id")
            val titleIndex = cursor.getColumnIndexOrThrow("title")
            val categoryIndex = cursor.getColumnIndexOrThrow("category")
            val amountIndex = cursor.getColumnIndexOrThrow("amount")
            val typeIndex = cursor.getColumnIndexOrThrow("type")
            val accountIndex = cursor.getColumnIndexOrThrow("account")
            val dateIndex = cursor.getColumnIndexOrThrow("date_label")
            while (cursor.moveToNext()) {
                result += Movement(
                    id = cursor.getLong(idIndex),
                    title = cursor.getString(titleIndex),
                    category = cursor.getString(categoryIndex),
                    amount = cursor.getLong(amountIndex),
                    type = MovementType.valueOf(cursor.getString(typeIndex)),
                    account = AccountType.valueOf(cursor.getString(accountIndex)),
                    dateLabel = cursor.getString(dateIndex)
                )
            }
        }
        return result
    }

    fun insertMovement(
        title: String,
        category: String,
        amount: Long,
        type: MovementType,
        account: AccountType,
        dateLabel: String
    ): Long {
        val values = ContentValues().apply {
            put("title", title)
            put("category", category)
            put("amount", amount)
            put("type", type.name)
            put("account", account.name)
            put("date_label", dateLabel)
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insert(TABLE_MOVEMENTS, null, values)
    }

    fun deleteMovement(id: Long): Int = writableDatabase.delete(
        TABLE_MOVEMENTS,
        "id = ?",
        arrayOf(id.toString())
    )

    fun getGoals(): List<SavingsGoal> {
        val result = mutableListOf<SavingsGoal>()
        readableDatabase.query(TABLE_GOALS, null, null, null, null, null, "id ASC").use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow("id")
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            val savedIndex = cursor.getColumnIndexOrThrow("saved")
            val targetIndex = cursor.getColumnIndexOrThrow("target")
            val emojiIndex = cursor.getColumnIndexOrThrow("emoji")
            while (cursor.moveToNext()) {
                result += SavingsGoal(
                    id = cursor.getLong(idIndex),
                    name = cursor.getString(nameIndex),
                    saved = cursor.getLong(savedIndex),
                    target = cursor.getLong(targetIndex),
                    emoji = cursor.getString(emojiIndex)
                )
            }
        }
        return result
    }

    fun insertGoal(name: String, saved: Long, target: Long, emoji: String): Long {
        val values = ContentValues().apply {
            put("name", name)
            put("saved", saved)
            put("target", target)
            put("emoji", emoji)
        }
        return writableDatabase.insert(TABLE_GOALS, null, values)
    }

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

    private fun setSetting(db: SQLiteDatabase, key: String, value: String) {
        val values = ContentValues().apply {
            put("key", key)
            put("value", value)
        }
        db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
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
        private const val DATABASE_NAME = "mi_puente_financiero.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_MOVEMENTS = "movements"
        private const val TABLE_GOALS = "goals"
        private const val TABLE_SETTINGS = "settings"

        const val KEY_PERSONAL_BALANCE = "personal_balance"
        const val KEY_BUSINESS_BALANCE = "business_balance"
        const val KEY_NOTIFICATIONS = "notifications_enabled"
    }
}

import Foundation
import SwiftData

/// Único punto de acceso a SwiftData para movimientos, metas y preferencias.
/// Opera sobre el ModelContext principal y guarda cada cambio explícitamente.
@MainActor
final class FinanceRepository {
    private let context: ModelContext

    init(context: ModelContext) {
        self.context = context
    }

    func initializeIfNeeded() throws {
        let marker = try setting("seeded_v1")
        guard marker == nil else { return }
        try upsert("personal_balance", value: "2500000")
        try upsert("business_balance", value: "2365000")
        try upsert("notifications_enabled", value: "true")

        let examples: [(String, String, Int64, TransactionKind, FinanceAccount, Int)] = [
            ("Supermercado Líder", "Súper", 75_000, .expense, .personal, 1),
            ("Transporte SITP", "Transporte", 5_900, .expense, .personal, 2),
            ("Café Juan Valdez", "Comida", 12_000, .expense, .personal, 24),
            ("Pago freelance", "Trabajo", 850_000, .income, .personal, 25),
            ("Restaurante Don Pedro", "Comida", 38_500, .expense, .personal, 48),
            ("Venta del día", "Ventas", 450_000, .income, .business, 3),
            ("Compra de inventario", "Súper", 130_000, .expense, .business, 28)
        ]
        for (title, category, amount, kind, account, hours) in examples {
            context.insert(FinanceMovement(title: title, category: category, amount: amount,
                kind: kind, account: account, createdAt: .now.addingTimeInterval(Double(-hours * 3600))))
        }
        context.insert(SavingsGoalRecord(name: "Viaje a Cartagena", saved: 1_625_000,
                                         target: 2_500_000, emoji: "✈️"))
        context.insert(SavingsGoalRecord(name: "Fondo de emergencia", saved: 1_260_000,
                                         target: 3_000_000, emoji: "🛟"))
        context.insert(SavingsGoalRecord(name: "Computador nuevo", saved: 1_900_000,
                                         target: 5_000_000, emoji: "💻"))
        try upsert("seeded_v1", value: "true")
        try context.save()
    }

    func movements() throws -> [FinanceMovement] {
        let request = FetchDescriptor<FinanceMovement>(sortBy: [SortDescriptor(\.createdAt, order: .reverse)])
        return try context.fetch(request)
    }

    func goals() throws -> [SavingsGoalRecord] {
        let request = FetchDescriptor<SavingsGoalRecord>(sortBy: [SortDescriptor(\.createdAt, order: .forward)])
        return try context.fetch(request)
    }

    func balance(for account: FinanceAccount) throws -> Int64 {
        let key = account == .personal ? "personal_balance" : "business_balance"
        return Int64(try setting(key) ?? "0") ?? 0
    }

    func notificationsEnabled() throws -> Bool {
        (try setting("notifications_enabled")) != "false"
    }

    func setNotifications(_ enabled: Bool) throws {
        try upsert("notifications_enabled", value: enabled ? "true" : "false")
        try context.save()
    }

    func addMovement(title: String, category: String, amount: Int64, kind: TransactionKind,
                     account: FinanceAccount) throws {
        guard amount > 0 else { throw FinanceDataError.invalidAmount }
        let current = try balance(for: account)
        let newBalance: Int64
        if kind == .income {
            let (result, overflow) = current.addingReportingOverflow(amount)
            guard !overflow else { throw FinanceDataError.invalidAmount }
            newBalance = result
        } else {
            let (result, overflow) = current.subtractingReportingOverflow(amount)
            guard !overflow else { throw FinanceDataError.invalidAmount }
            newBalance = result
        }
        let movement = FinanceMovement(title: title, category: category, amount: amount, kind: kind, account: account)
        context.insert(movement)
        try setBalance(newBalance, for: account)
        try context.save()
    }

    func deleteMovement(_ movement: FinanceMovement) throws {
        let balance = try balance(for: movement.account)
        let adjustment = -movement.signedAmount
        let (updated, overflow) = balance.addingReportingOverflow(adjustment)
        guard !overflow else { throw FinanceDataError.invalidAmount }
        try setBalance(updated, for: movement.account)
        context.delete(movement)
        try context.save()
    }

    func addGoal(name: String, target: Int64, emoji: String) throws {
        guard !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty, target > 0 else {
            throw FinanceDataError.invalidGoal
        }
        context.insert(SavingsGoalRecord(name: name.trimmingCharacters(in: .whitespacesAndNewlines),
                                         target: target, emoji: emoji))
        try context.save()
    }

    func updateGoal(_ goal: SavingsGoalRecord, name: String, target: Int64) throws {
        guard !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty, target > 0 else {
            throw FinanceDataError.invalidGoal
        }
        goal.name = name.trimmingCharacters(in: .whitespacesAndNewlines)
        goal.target = target
        try context.save()
    }

    func contribute(to goal: SavingsGoalRecord, amount: Int64) throws {
        guard amount > 0 else { throw FinanceDataError.invalidAmount }
        let (newValue, overflow) = goal.saved.addingReportingOverflow(amount)
        guard !overflow else { throw FinanceDataError.invalidAmount }
        goal.saved = newValue
        try context.save()
    }

    func deleteGoal(_ goal: SavingsGoalRecord) throws {
        context.delete(goal)
        try context.save()
    }

    private func setBalance(_ value: Int64, for account: FinanceAccount) throws {
        try upsert(account == .personal ? "personal_balance" : "business_balance", value: String(value))
    }

    private func setting(_ key: String) throws -> String? {
        try context.fetch(FetchDescriptor<FinanceSetting>()).first(where: { $0.key == key })?.value
    }

    private func upsert(_ key: String, value: String) throws {
        if let existing = try context.fetch(FetchDescriptor<FinanceSetting>()).first(where: { $0.key == key }) {
            existing.value = value
        } else {
            context.insert(FinanceSetting(key: key, value: value))
        }
    }
}

enum FinanceDataError: LocalizedError {
    case invalidAmount
    case invalidGoal

    var errorDescription: String? {
        switch self {
        case .invalidAmount: "El monto debe ser mayor que cero y estar dentro del rango permitido."
        case .invalidGoal: "Escribe un nombre y un objetivo válido para la meta."
        }
    }
}

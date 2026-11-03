import Foundation
import SwiftData

enum FinanceAccount: String, CaseIterable, Identifiable {
    case personal = "PERSONAL"
    case business = "BUSINESS"

    var id: String { rawValue }
    var label: String { self == .personal ? "Personal" : "Negocio" }
}

enum TransactionKind: String, CaseIterable {
    case income = "INCOME"
    case expense = "EXPENSE"

    var label: String { self == .income ? "Ingreso" : "Gasto" }
}

@Model
final class FinanceMovement {
    var id: UUID
    var title: String
    var category: String
    var amount: Int64
    var kindRaw: String
    var accountRaw: String
    var createdAt: Date

    init(title: String, category: String, amount: Int64, kind: TransactionKind,
         account: FinanceAccount, createdAt: Date = .now) {
        self.id = UUID()
        self.title = title
        self.category = category
        self.amount = amount
        self.kindRaw = kind.rawValue
        self.accountRaw = account.rawValue
        self.createdAt = createdAt
    }

    var kind: TransactionKind { TransactionKind(rawValue: kindRaw) ?? .expense }
    var account: FinanceAccount { FinanceAccount(rawValue: accountRaw) ?? .personal }
    var signedAmount: Int64 { kind == .income ? amount : -amount }
}

@Model
final class SavingsGoalRecord {
    var id: UUID
    var name: String
    var saved: Int64
    var target: Int64
    var emoji: String
    var createdAt: Date

    init(name: String, saved: Int64 = 0, target: Int64, emoji: String = "🎯",
         createdAt: Date = .now) {
        self.id = UUID()
        self.name = name
        self.saved = saved
        self.target = target
        self.emoji = emoji
        self.createdAt = createdAt
    }

    var progress: Double {
        guard target > 0 else { return 0 }
        return min(1, max(0, Double(saved) / Double(target)))
    }
}

@Model
final class FinanceSetting {
    @Attribute(.unique) var key: String
    var value: String

    init(key: String, value: String) {
        self.key = key
        self.value = value
    }
}

enum MoneyFormat {
    static func cop(_ value: Int64) -> String {
        let formatted = value.formatted(.number.locale(Locale(identifier: "es_CO")))
        return "$ \(formatted) COP"
    }

    static func amount(_ raw: String) -> String {
        guard let value = Int64(raw), value >= 0 else { return "$ 0 COP" }
        return cop(value)
    }
}

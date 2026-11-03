import Foundation
import Combine

@MainActor
final class FinanceViewModel: ObservableObject {
    @Published var userName = "Alejandra"
    @Published var selectedAccount: FinanceAccount = .personal
    @Published var personalBalance: Int64 = 2_500_000
    @Published var businessBalance: Int64 = 2_365_000
    @Published var balanceIsVisible = true
    @Published var amountDraft = "50000"
    @Published var categoryDraft = "Comida"
    @Published var noteDraft = ""
    @Published var movements: [FinanceMovement] = []
    @Published var goals: [SavingsGoalRecord] = []
    @Published var notificationsEnabled = true
    @Published var exchangeRate: Double?
    @Published var exchangeRateLoading = false
    @Published var exchangeRateMessage = "Toca actualizar para consultar la tasa en línea."
    @Published var dataLoaded = false
    @Published var alertMessage: String?

    private let repository: FinanceRepository
    private let rateService = ExchangeRateService()

    init(repository: FinanceRepository) {
        self.repository = repository
        reload()
    }

    var activeBalance: Int64 { selectedAccount == .personal ? personalBalance : businessBalance }
    var activeMovements: [FinanceMovement] {
        movements.filter { $0.account == selectedAccount }
    }

    func reload() {
        do {
            try repository.initializeIfNeeded()
            try refreshFromDisk()
            dataLoaded = true
        } catch {
            dataLoaded = false
            alertMessage = "No fue posible cargar la base local. \(error.localizedDescription)"
        }
    }

    private func refreshFromDisk() throws {
        movements = try repository.movements()
        goals = try repository.goals()
        personalBalance = try repository.balance(for: .personal)
        businessBalance = try repository.balance(for: .business)
        notificationsEnabled = try repository.notificationsEnabled()
    }

    func toggleBalanceVisibility() { balanceIsVisible.toggle() }

    func pressAmountKey(_ key: String) {
        switch key {
        case "C": amountDraft = ""
        case "⌫": amountDraft = String(amountDraft.dropLast())
        default:
            guard key.count == 1, key.allSatisfy(\.isNumber), amountDraft.count < 10 else { return }
            amountDraft = String((amountDraft + key).drop(while: { $0 == "0" }))
            if amountDraft.isEmpty { amountDraft = "0" }
        }
    }

    @discardableResult
    func addMovement(kind: TransactionKind) -> Bool {
        guard let amount = Int64(amountDraft), amount > 0 else {
            alertMessage = "Ingresa un monto válido antes de registrar el movimiento."
            return false
        }
        let title = noteDraft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? defaultTitle(for: categoryDraft) : noteDraft.trimmingCharacters(in: .whitespacesAndNewlines)
        do {
            try repository.addMovement(title: title, category: categoryDraft,
                                       amount: amount, kind: kind, account: selectedAccount)
            try refreshFromDisk()
            amountDraft = ""
            noteDraft = ""
            return true
        } catch {
            alertMessage = error.localizedDescription
            return false
        }
    }

    func deleteMovement(_ movement: FinanceMovement) {
        do {
            try repository.deleteMovement(movement)
            try refreshFromDisk()
        } catch {
            alertMessage = error.localizedDescription
        }
    }

    @discardableResult
    func createGoal(name: String, target: Int64, emoji: String) -> Bool {
        do {
            try repository.addGoal(name: name, target: target, emoji: emoji)
            try refreshFromDisk()
            return true
        } catch {
            alertMessage = error.localizedDescription
            return false
        }
    }

    @discardableResult
    func editGoal(_ goal: SavingsGoalRecord, name: String, target: Int64) -> Bool {
        do {
            try repository.updateGoal(goal, name: name, target: target)
            try refreshFromDisk()
            return true
        } catch {
            alertMessage = error.localizedDescription
            return false
        }
    }

    @discardableResult
    func deposit(_ amount: Int64, into goal: SavingsGoalRecord) -> Bool {
        do {
            try repository.contribute(to: goal, amount: amount)
            try refreshFromDisk()
            return true
        } catch {
            alertMessage = error.localizedDescription
            return false
        }
    }

    func deleteGoal(_ goal: SavingsGoalRecord) {
        do {
            try repository.deleteGoal(goal)
            try refreshFromDisk()
        } catch {
            alertMessage = error.localizedDescription
        }
    }

    func setNotifications(_ enabled: Bool) {
        do {
            try repository.setNotifications(enabled)
            notificationsEnabled = enabled
        } catch {
            alertMessage = error.localizedDescription
        }
    }

    func refreshExchangeRate() {
        guard !exchangeRateLoading else { return }
        exchangeRateLoading = true
        exchangeRateMessage = "Consultando tasa de referencia..."
        Task {
            do {
                exchangeRate = try await rateService.fetchUSDToCOP()
                exchangeRateMessage = "Tasa pública actualizada. No corresponde a la TRM oficial."
            } catch {
                exchangeRateMessage = "No fue posible actualizar la tasa. Verifica tu conexión."
            }
            exchangeRateLoading = false
        }
    }

    private func defaultTitle(for category: String) -> String {
        switch category {
        case "Comida": "Compra de comida"
        case "Transporte": "Transporte"
        case "Súper": "Supermercado"
        case "Vivienda": "Pago de vivienda"
        case "Servicios": "Pago de servicios"
        case "Trabajo": "Ingreso de trabajo"
        case "Ventas": "Venta"
        default: category
        }
    }
}

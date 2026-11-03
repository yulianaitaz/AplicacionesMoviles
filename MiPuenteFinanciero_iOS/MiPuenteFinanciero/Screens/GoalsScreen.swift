import SwiftUI

struct GoalsScreen: View {
    @ObservedObject var model: FinanceViewModel
    @State private var creatingGoal = false
    @State private var selectedGoal: SavingsGoalRecord?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                ScreenTitle(title: "Metas de ahorro", subtitle: "Convierte un objetivo grande en avances pequeños y visibles")

                if model.goals.isEmpty {
                    ContentUnavailableView("Sin metas todavía", systemImage: "target", description: Text("Crea tu primera meta para comenzar a ahorrar."))
                }
                ForEach(model.goals) { goal in
                    Button { selectedGoal = goal } label: {
                        VStack(alignment: .leading, spacing: 12) {
                            HStack(alignment: .firstTextBaseline, spacing: 8) {
                                Text(goal.emoji).font(.title3)
                                Text(goal.name).font(.headline).foregroundStyle(.primary)
                                Spacer(minLength: 3)
                                Text("\(Int(goal.progress * 100))%")
                                    .font(.headline).foregroundStyle(Brand.teal)
                            }
                            Text("\(Int(goal.progress * 100))% completado")
                                .font(.caption).foregroundStyle(.secondary)
                            ProgressView(value: goal.progress).tint(Brand.teal)
                            Text("\(MoneyFormat.cop(goal.saved)) / \(MoneyFormat.cop(goal.target))")
                                .font(.caption).foregroundStyle(.secondary)
                            HStack {
                                Spacer()
                                Label("Editar o abonar", systemImage: "square.and.pencil")
                                    .font(.caption.weight(.medium)).foregroundStyle(Brand.teal)
                            }
                        }
                        .surfaceCard()
                    }
                    .buttonStyle(.plain)
                }

                Button { creatingGoal = true } label: {
                    Label("Nueva meta", systemImage: "plus")
                        .font(.headline).frame(maxWidth: .infinity).frame(height: 56)
                        .foregroundStyle(.white)
                        .background(Brand.teal, in: RoundedRectangle(cornerRadius: 17))
                }
                .buttonStyle(.plain)
            }
            .padding(18)
        }
        .background(Brand.background)
        .toolbar(.hidden, for: .navigationBar)
        .sheet(isPresented: $creatingGoal) { CreateGoalSheet(model: model) }
        .sheet(item: $selectedGoal) { goal in GoalDetailSheet(model: model, goal: goal) }
    }
}

struct CreateGoalSheet: View {
    @ObservedObject var model: FinanceViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var name = ""
    @State private var targetText = ""
    @State private var emoji = "🎯"

    var body: some View {
        NavigationStack {
            Form {
                Section("Tu nueva meta") {
                    TextField("Nombre de la meta", text: $name)
                    TextField("Objetivo en pesos colombianos", text: $targetText)
                        .keyboardType(.numberPad)
                    Picker("Ícono", selection: $emoji) {
                        ForEach(["🎯", "✈️", "🛟", "💻", "🏠", "🎓", "🚗"], id: \.self) { icon in
                            Text(icon).tag(icon)
                        }
                    }
                }
            }
            .navigationTitle("Nueva meta")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancelar") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Guardar") {
                        guard let target = Int64(targetText), model.createGoal(name: name, target: target, emoji: emoji) else { return }
                        dismiss()
                    }
                    .disabled(name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || (Int64(targetText) ?? 0) <= 0)
                }
            }
        }
        .tint(Brand.teal)
    }
}

struct GoalDetailSheet: View {
    @ObservedObject var model: FinanceViewModel
    let goal: SavingsGoalRecord
    @Environment(\.dismiss) private var dismiss
    @State private var name: String
    @State private var targetText: String
    @State private var contributionText = ""
    @State private var confirmingDeletion = false

    init(model: FinanceViewModel, goal: SavingsGoalRecord) {
        self.model = model
        self.goal = goal
        _name = State(initialValue: goal.name)
        _targetText = State(initialValue: String(goal.target))
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Progreso actual") {
                    Text(goal.name).font(.headline)
                    ProgressView(value: goal.progress).tint(Brand.teal)
                    Text("Ahorrado: \(MoneyFormat.cop(goal.saved))")
                    Text("Objetivo: \(MoneyFormat.cop(goal.target))")
                }
                Section("Agregar dinero ahorrado") {
                    TextField("Valor del aporte en COP", text: $contributionText)
                        .keyboardType(.numberPad)
                    Button("Registrar aporte") {
                        guard let amount = Int64(contributionText), model.deposit(amount, into: goal) else { return }
                        contributionText = ""
                    }
                    .disabled((Int64(contributionText) ?? 0) <= 0)
                }
                Section("Editar la meta") {
                    TextField("Nombre", text: $name)
                    TextField("Objetivo en COP", text: $targetText).keyboardType(.numberPad)
                    Button("Guardar cambios") {
                        guard let amount = Int64(targetText), model.editGoal(goal, name: name, target: amount) else { return }
                        dismiss()
                    }
                    .disabled(name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || (Int64(targetText) ?? 0) <= 0)
                }
                Section {
                    Button("Eliminar meta", role: .destructive) { confirmingDeletion = true }
                }
            }
            .navigationTitle("Detalle de meta")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .confirmationAction) { Button("Listo") { dismiss() } } }
            .confirmationDialog("¿Eliminar esta meta?", isPresented: $confirmingDeletion) {
                Button("Eliminar definitivamente", role: .destructive) {
                    model.deleteGoal(goal)
                    dismiss()
                }
            }
        }
        .tint(Brand.teal)
    }
}

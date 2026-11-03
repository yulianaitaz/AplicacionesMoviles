import SwiftUI

private enum MovementFilter: String, CaseIterable, Identifiable {
    case all = "Todos"
    case income = "Ingresos"
    case expense = "Gastos"
    var id: String { rawValue }

    func matches(_ movement: FinanceMovement) -> Bool {
        switch self {
        case .all: true
        case .income: movement.kind == .income
        case .expense: movement.kind == .expense
        }
    }
}

struct MovementsScreen: View {
    @ObservedObject var model: FinanceViewModel
    @State private var search = ""
    @State private var filter: MovementFilter = .all
    @State private var movementToDelete: FinanceMovement?

    private var filtered: [FinanceMovement] {
        model.activeMovements.filter { movement in
            filter.matches(movement) && (search.isEmpty ||
                movement.title.localizedCaseInsensitiveContains(search) ||
                movement.category.localizedCaseInsensitiveContains(search))
        }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                ScreenTitle(title: "Movimientos", subtitle: "Busca, filtra y revisa en qué se está moviendo tu plata")
                AccountSelector(selection: $model.selectedAccount)

                HStack {
                    Image(systemName: "magnifyingglass").foregroundStyle(.secondary)
                    TextField("Buscar movimiento o categoría", text: $search)
                        .autocorrectionDisabled()
                        .accessibilityLabel("Buscar movimientos")
                    if !search.isEmpty {
                        Button { search = "" } label: {
                            Image(systemName: "xmark.circle.fill").foregroundStyle(.secondary)
                        }
                    }
                }
                .padding(14)
                .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 14))

                HStack(spacing: 9) {
                    ForEach(MovementFilter.allCases) { option in
                        Button { filter = option } label: {
                            Text(option.rawValue)
                                .font(.subheadline.weight(.medium))
                                .padding(.horizontal, 14).padding(.vertical, 10)
                                .foregroundStyle(filter == option ? Brand.navy : Color.secondary)
                                .background(filter == option ? Brand.paleBlue : Color(uiColor: .secondarySystemGroupedBackground),
                                            in: Capsule())
                        }
                        .buttonStyle(.plain)
                    }
                }
                if filtered.isEmpty {
                    ContentUnavailableView("Sin movimientos", systemImage: "tray", description:
                        Text("Prueba con otro filtro o registra tu primer movimiento."))
                } else {
                    LazyVStack(spacing: 10) {
                        ForEach(filtered) { movement in
                            FinancialRow(movement: movement)
                                .surfaceCard()
                                .contextMenu {
                                    Button(role: .destructive) { movementToDelete = movement } label: {
                                        Label("Eliminar movimiento", systemImage: "trash")
                                    }
                                }
                                .accessibilityHint("Mantén presionado para eliminar")
                        }
                    }
                }
                Text("Mantén presionado un movimiento para eliminarlo. El saldo se corregirá automáticamente.")
                    .font(.caption).foregroundStyle(.secondary)
            }
            .padding(18)
        }
        .background(Brand.background)
        .toolbar(.hidden, for: .navigationBar)
        .confirmationDialog("¿Eliminar movimiento?", isPresented: Binding(
            get: { movementToDelete != nil },
            set: { if !$0 { movementToDelete = nil } }
        )) {
            Button("Eliminar", role: .destructive) {
                if let movementToDelete { model.deleteMovement(movementToDelete) }
                movementToDelete = nil
            }
        } message: { Text("Este cambio actualizará el saldo guardado localmente.") }
    }
}

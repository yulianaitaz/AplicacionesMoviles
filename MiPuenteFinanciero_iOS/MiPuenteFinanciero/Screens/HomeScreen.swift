import SwiftUI

struct HomeScreen: View {
    @ObservedObject var model: FinanceViewModel
    @Binding var selectedTab: AppTab
    @State private var placeholderAction: String?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 15) {
                HStack(alignment: .center, spacing: 12) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text("Hola, \(model.userName)").font(.title2.bold()).foregroundStyle(Brand.teal)
                        Text("Así se ve tu plata hoy").font(.subheadline).foregroundStyle(.secondary)
                    }
                    Spacer()
                    Text(String(model.userName.prefix(1)))
                        .font(.headline).foregroundStyle(Brand.navy)
                        .frame(width: 44, height: 44)
                        .background(Brand.mint, in: Circle())
                }

                AccountSelector(selection: $model.selectedAccount)

                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Text("Saldo total").font(.subheadline.weight(.medium)).foregroundStyle(Brand.navy)
                        Spacer()
                        Button { model.toggleBalanceVisibility() } label: {
                            Image(systemName: model.balanceIsVisible ? "eye" : "eye.slash")
                                .font(.title3).foregroundStyle(Brand.navy)
                                .padding(7)
                                .background(Color.white.opacity(0.6), in: Circle())
                        }
                        .accessibilityLabel(model.balanceIsVisible ? "Ocultar saldo" : "Mostrar saldo")
                    }
                    Text(model.balanceIsVisible ? MoneyFormat.cop(model.activeBalance) : "••••••••")
                        .font(.system(size: 30, weight: .bold, design: .rounded))
                        .foregroundStyle(Brand.navy).lineLimit(1).minimumScaleFactor(0.7)
                    Text(model.dataLoaded ? "Datos guardados localmente" : "Comprobando almacenamiento local")
                        .font(.caption).foregroundStyle(Brand.navy.opacity(0.75))
                }
                .padding(20)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Brand.mint, in: RoundedRectangle(cornerRadius: 23))

                if let goal = model.goals.first {
                    Button { selectedTab = .goals } label: {
                        VStack(alignment: .leading, spacing: 10) {
                            HStack(alignment: .firstTextBaseline) {
                                Text("Meta de ahorro").font(.headline)
                                Spacer()
                                Text("\(Int(goal.progress * 100))%").font(.subheadline.weight(.bold))
                            }
                            Text(goal.name).font(.subheadline).foregroundStyle(.secondary)
                            ProgressView(value: goal.progress).tint(Brand.coral)
                            Text("\(MoneyFormat.cop(goal.saved)) de \(MoneyFormat.cop(goal.target))")
                                .font(.caption).foregroundStyle(.secondary)
                        }
                        .surfaceCard()
                    }
                    .buttonStyle(.plain)
                    .accessibilityHint("Abrir metas de ahorro")
                }

                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Image(systemName: "dollarsign.arrow.circlepath").foregroundStyle(Brand.teal)
                        Text("Servicio en línea · USD/COP").font(.caption.weight(.medium))
                    }
                    HStack {
                        VStack(alignment: .leading, spacing: 5) {
                            if let rate = model.exchangeRate {
                                Text("1 USD ≈ \(MoneyFormat.cop(Int64(rate.rounded())))")
                                    .font(.headline).foregroundStyle(Brand.navy)
                            } else {
                                Text("Tasa aún no consultada").font(.headline).foregroundStyle(Brand.navy)
                            }
                            Text(model.exchangeRateMessage).font(.caption).foregroundStyle(Brand.navy.opacity(0.75))
                        }
                        Spacer()
                        if model.exchangeRateLoading {
                            ProgressView().tint(Brand.teal)
                        } else {
                            Button { model.refreshExchangeRate() } label: {
                                Image(systemName: "arrow.clockwise").font(.headline)
                            }
                            .accessibilityLabel("Actualizar tasa USD a COP")
                        }
                    }
                }
                .padding(18)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Brand.paleBlue, in: RoundedRectangle(cornerRadius: 22))

                SectionTitle(title: "Acciones rápidas", subtitle: "Lo más usado, a un toque")
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                    action("Transferir", symbol: "arrow.left.arrow.right") { placeholderAction = "Transferir" }
                    action("Servicios", symbol: "creditcard") { placeholderAction = "Servicios" }
                    action("Recargar", symbol: "iphone") { placeholderAction = "Recargar" }
                    action("Historial", symbol: "clock.arrow.circlepath") { selectedTab = .movements }
                }

                HStack {
                    SectionTitle(title: "Movimientos recientes", subtitle: "Tus últimas entradas y salidas")
                    Button { selectedTab = .add } label: {
                        Image(systemName: "plus").font(.headline)
                            .frame(width: 42, height: 42)
                            .background(Brand.paleMint, in: Circle())
                    }
                    .accessibilityLabel("Añadir movimiento")
                }
                VStack(spacing: 2) {
                    if model.activeMovements.isEmpty {
                        Text("Todavía no hay movimientos para este entorno.")
                            .font(.subheadline).foregroundStyle(.secondary)
                    } else {
                        ForEach(Array(model.activeMovements.prefix(4))) { movement in
                            FinancialRow(movement: movement)
                            if movement.id != model.activeMovements.prefix(4).last?.id { Divider() }
                        }
                    }
                }
                .surfaceCard()
            }
            .padding(18)
        }
        .background(Brand.background)
        .toolbar(.hidden, for: .navigationBar)
        .alert("Próxima funcionalidad", isPresented: Binding(
            get: { placeholderAction != nil },
            set: { if !$0 { placeholderAction = nil } }
        )) {
            Button("Entendido", role: .cancel) { placeholderAction = nil }
        } message: {
            Text("\(placeholderAction ?? "Esta acción") estará disponible en una versión posterior del microproyecto.")
        }
    }

    private func action(_ label: String, symbol: String, onTap: @escaping () -> Void) -> some View {
        Button(action: onTap) {
            HStack(spacing: 9) {
                Image(systemName: symbol)
                    .foregroundStyle(Brand.teal)
                    .frame(width: 31, height: 31)
                    .background(Brand.paleMint, in: RoundedRectangle(cornerRadius: 10))
                Text(label).font(.subheadline.weight(.medium)).foregroundStyle(.primary)
                Spacer(minLength: 0)
            }
            .padding(10)
            .frame(maxWidth: .infinity)
            .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 14))
        }
        .buttonStyle(.plain)
    }
}

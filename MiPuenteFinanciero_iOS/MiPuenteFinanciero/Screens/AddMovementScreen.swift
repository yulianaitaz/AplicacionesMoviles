import SwiftUI

struct AddMovementScreen: View {
    @ObservedObject var model: FinanceViewModel
    @Binding var selectedTab: AppTab

    private let keys = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "⌫"]
    private let categories = ["Comida", "Transporte", "Súper", "Vivienda", "Servicios", "Trabajo", "Ventas", "Otros"]
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 9), count: 3)

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 15) {
                ScreenTitle(title: "Registrar movimiento", subtitle: "Anota lo que entró o salió sin llenar formularios largos")
                AccountSelector(selection: $model.selectedAccount)

                VStack(spacing: 8) {
                    Text("Monto").font(.subheadline.weight(.medium)).foregroundStyle(Brand.navy)
                    Text(MoneyFormat.amount(model.amountDraft))
                        .font(.system(size: 32, weight: .bold, design: .rounded))
                        .foregroundStyle(Brand.navy)
                        .minimumScaleFactor(0.6)
                        .lineLimit(1)
                        .accessibilityLabel("Monto actual \(MoneyFormat.amount(model.amountDraft))")
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 22)
                .background(Brand.mint, in: RoundedRectangle(cornerRadius: 22))

                Text("Categoría").font(.headline)
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(categories, id: \.self) { category in
                            Button { model.categoryDraft = category } label: {
                                Text(category).font(.subheadline)
                                    .padding(.horizontal, 13).padding(.vertical, 10)
                                    .foregroundStyle(model.categoryDraft == category ? Brand.navy : Color.secondary)
                                    .background(model.categoryDraft == category ? Brand.paleBlue : Color(uiColor: .secondarySystemGroupedBackground), in: Capsule())
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }

                TextField("Descripción opcional", text: $model.noteDraft)
                    .textInputAutocapitalization(.sentences)
                    .padding(14)
                    .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 14))
                    .accessibilityLabel("Descripción del movimiento")

                LazyVGrid(columns: columns, spacing: 9) {
                    ForEach(keys, id: \.self) { key in
                        Button { model.pressAmountKey(key) } label: {
                            Text(key).font(.title2.weight(.semibold)).foregroundStyle(Brand.navy)
                                .frame(maxWidth: .infinity).frame(height: 57)
                                .background(Brand.paleBlue, in: RoundedRectangle(cornerRadius: 16))
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(key == "⌫" ? "Borrar último dígito" : key == "C" ? "Borrar monto" : key)
                    }
                }

                HStack(spacing: 11) {
                    movementButton("Entró plata", icon: "arrow.up", kind: .income, color: Brand.green)
                    movementButton("Salió plata", icon: "arrow.down", kind: .expense, color: Brand.coral)
                }
                Text("La app guarda cada movimiento en el dispositivo y actualiza el saldo de tu entorno activo.")
                    .font(.caption).foregroundStyle(.secondary)
            }
            .padding(18)
        }
        .background(Brand.background)
        .toolbar(.hidden, for: .navigationBar)
    }

    private func movementButton(_ text: String, icon: String, kind: TransactionKind, color: Color) -> some View {
        Button {
            if model.addMovement(kind: kind) { selectedTab = .movements }
        } label: {
            HStack(spacing: 5) {
                Image(systemName: icon).font(.headline)
                Text(text).font(.headline).minimumScaleFactor(0.8).lineLimit(1)
            }
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity).frame(height: 56)
            .background(color, in: RoundedRectangle(cornerRadius: 16))
        }
        .buttonStyle(.plain)
    }
}

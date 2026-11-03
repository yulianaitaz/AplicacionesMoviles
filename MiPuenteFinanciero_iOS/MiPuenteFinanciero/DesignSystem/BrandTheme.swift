import SwiftUI

enum Brand {
    static let teal = Color(red: 0.0, green: 0.42, blue: 0.42)
    static let tealBright = Color(red: 0.10, green: 0.70, blue: 0.68)
    static let mint = Color(red: 0.63, green: 0.94, blue: 0.91)
    static let paleMint = Color(red: 0.88, green: 0.98, blue: 0.96)
    static let paleBlue = Color(red: 0.85, green: 0.91, blue: 0.99)
    static let navy = Color(red: 0.07, green: 0.18, blue: 0.27)
    static let coral = Color(red: 0.89, green: 0.36, blue: 0.33)
    static let green = Color(red: 0.13, green: 0.60, blue: 0.31)
    static let background = Color(uiColor: UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor.systemBackground : UIColor(red: 0.975, green: 0.986, blue: 0.986, alpha: 1)
    })
}

struct SurfaceCard: ViewModifier {
    func body(content: Content) -> some View {
        content
            .padding(17)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 22))
    }
}

extension View {
    func surfaceCard() -> some View { modifier(SurfaceCard()) }
}

struct ScreenTitle: View {
    let title: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(title).font(.largeTitle.bold()).foregroundStyle(.primary)
            Text(subtitle).font(.subheadline).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .combine)
    }
}

struct SectionTitle: View {
    let title: String
    var subtitle: String? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 3) {
            Text(title).font(.title3.bold())
            if let subtitle { Text(subtitle).font(.subheadline).foregroundStyle(.secondary) }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct AccountSelector: View {
    @Binding var selection: FinanceAccount

    var body: some View {
        HStack(spacing: 6) {
            ForEach(FinanceAccount.allCases) { account in
                Button {
                    selection = account
                } label: {
                    Text(account.label)
                        .font(.subheadline.weight(selection == account ? .semibold : .regular))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .foregroundStyle(selection == account ? Brand.navy : Color.secondary)
                        .background(selection == account ? Brand.paleBlue : .clear, in: RoundedRectangle(cornerRadius: 11))
                }
                .buttonStyle(.plain)
                .accessibilityAddTraits(selection == account ? .isSelected : [])
            }
        }
        .padding(5)
        .background(Color(uiColor: .tertiarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 15))
    }
}

struct FinancialRow: View {
    let movement: FinanceMovement
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: iconForCategory(movement.category))
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(movement.kind == .income ? Brand.green : Brand.coral)
                .frame(width: 42, height: 42)
                .background(movement.kind == .income ? Brand.green.opacity(0.11) : Brand.coral.opacity(0.11), in: Circle())
            VStack(alignment: .leading, spacing: 3) {
                Text(movement.title).font(.subheadline.weight(.semibold)).lineLimit(1)
                Text("\(movement.category) · \(movement.createdAt.formatted(date: .abbreviated, time: .shortened))")
                    .font(.caption).foregroundStyle(.secondary).lineLimit(1)
            }
            Spacer(minLength: 4)
            Text("\(movement.kind == .income ? "+" : "−")\(MoneyFormat.cop(movement.amount))")
                .font(.caption.weight(.semibold))
                .foregroundStyle(movement.kind == .income ? Brand.green : Brand.coral)
                .lineLimit(1)
                .minimumScaleFactor(0.75)
        }
        .padding(.vertical, 7)
    }
}

func iconForCategory(_ name: String) -> String {
    switch name {
    case "Súper": "cart.fill"
    case "Transporte": "bus.fill"
    case "Comida": "fork.knife"
    case "Vivienda": "house.fill"
    case "Servicios": "bolt.fill"
    case "Trabajo": "briefcase.fill"
    case "Ventas": "chart.line.uptrend.xyaxis"
    default: "creditcard.fill"
    }
}

import SwiftUI

struct ProfileScreen: View {
    @ObservedObject var model: FinanceViewModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                ScreenTitle(title: "Perfil", subtitle: "Tu cuenta y las preferencias de Mi Puente Financiero")
                HStack(spacing: 15) {
                    Image(systemName: "person.crop.circle.fill")
                        .font(.system(size: 45)).foregroundStyle(Brand.teal)
                    VStack(alignment: .leading, spacing: 3) {
                        Text(model.userName).font(.title3.bold())
                        Text("Cuenta principal").font(.subheadline).foregroundStyle(.secondary)
                    }
                    Spacer()
                }
                .surfaceCard()

                SectionTitle(title: "Entorno activo")
                AccountSelector(selection: $model.selectedAccount)

                Toggle(isOn: Binding(
                    get: { model.notificationsEnabled },
                    set: { model.setNotifications($0) }
                )) {
                    Label {
                        VStack(alignment: .leading) {
                            Text("Notificaciones")
                            Text("Preferencia guardada localmente").font(.caption).foregroundStyle(.secondary)
                        }
                    } icon: { Image(systemName: "bell.fill").foregroundStyle(Brand.teal) }
                }
                .tint(Brand.teal)
                .surfaceCard()

                VStack(spacing: 7) {
                    profileItem("Seguridad", subtitle: "Datos financieros guardados en este dispositivo", symbol: "shield.lefthalf.filled")
                    Divider()
                    profileItem("Privacidad", subtitle: "La app no requiere una cuenta bancaria", symbol: "lock.shield.fill")
                    Divider()
                    profileItem("Mis cuentas", subtitle: "Personal y Negocio en un mismo lugar", symbol: "wallet.pass.fill")
                }
                .surfaceCard()

                NavigationLink {
                    AboutScreen()
                } label: {
                    HStack {
                        Image(systemName: "info.circle.fill").foregroundStyle(Brand.teal)
                        Text("Acerca de y créditos").foregroundStyle(.primary)
                        Spacer()
                        Image(systemName: "chevron.right").foregroundStyle(.secondary)
                    }
                    .padding(17)
                    .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 18))
                }
                .buttonStyle(.plain)
            }
            .padding(18)
        }
        .background(Brand.background)
    }

    private func profileItem(_ title: String, subtitle: String, symbol: String) -> some View {
        HStack(alignment: .center, spacing: 14) {
            Image(systemName: symbol).font(.title3)
                .frame(width: 40, height: 40)
                .foregroundStyle(Brand.teal)
                .background(Brand.paleMint, in: Circle())
            VStack(alignment: .leading, spacing: 3) {
                Text(title).font(.subheadline.weight(.semibold))
                Text(subtitle).font(.caption).foregroundStyle(.secondary)
            }
            Spacer()
        }
        .padding(.vertical, 7)
    }
}

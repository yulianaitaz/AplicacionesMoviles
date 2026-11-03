import SwiftUI

struct AboutScreen: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                HStack(spacing: 15) {
                    Image("BrandIcon")
                        .resizable().scaledToFit()
                        .frame(width: 72, height: 72)
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Mi Puente Financiero").font(.title2.bold())
                        Text("Control y ahorro, sin enredos").font(.subheadline).foregroundStyle(.secondary)
                    }
                }
                .surfaceCard()

                SectionTitle(title: "Acerca de la aplicación")
                Text("Mi Puente Financiero ayuda a registrar ingresos y gastos, separar la plata personal de la del negocio y seguir objetivos de ahorro en una interfaz sencilla. Esta versión nativa para iOS se desarrolló con SwiftUI.")
                    .font(.body)
                    .frame(maxWidth: .infinity, alignment: .leading)

                SectionTitle(title: "Arquitectura")
                VStack(spacing: 12) {
                    technology("SwiftUI", detail: "Interfaz nativa, navegación y formularios", icon: "rectangle.3.group")
                    technology("MVVM", detail: "FinanceViewModel con ObservableObject y @Published", icon: "square.stack")
                    technology("SwiftData", detail: "Persistencia local de movimientos, metas y preferencias", icon: "externaldrive.fill")
                    technology("URLSession", detail: "Petición HTTPS para consultar USD/COP", icon: "network")
                }
                .surfaceCard()

                SectionTitle(title: "Equipo de desarrollo")
                VStack(alignment: .leading, spacing: 12) {
                    Text("Guillermo Adolfo Cuéllar Fonseca")
                    Text("José Luis Sánchez López")
                    Text("Yennifer Yuliana Uni Itaz")
                }
                .font(.subheadline)
                .surfaceCard()

                Text("Universidad del Cauca · Departamento de Telemática · Electiva Desarrollo de Aplicaciones Móviles · 2026")
                    .font(.caption).foregroundStyle(.secondary)
            }
            .padding(18)
        }
        .background(Brand.background)
        .navigationTitle("Acerca de")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func technology(_ name: String, detail: String, icon: String) -> some View {
        HStack(alignment: .center, spacing: 12) {
            Image(systemName: icon).frame(width: 30).foregroundStyle(Brand.teal)
            VStack(alignment: .leading, spacing: 3) {
                Text(name).font(.subheadline.weight(.semibold))
                Text(detail).font(.caption).foregroundStyle(.secondary)
            }
            Spacer()
        }
    }
}

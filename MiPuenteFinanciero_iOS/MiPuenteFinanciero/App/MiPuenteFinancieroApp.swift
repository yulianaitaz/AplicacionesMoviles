import SwiftUI
import SwiftData

@main
struct MiPuenteFinancieroApp: App {
    var body: some Scene {
        WindowGroup {
            BootstrapView()
        }
        .modelContainer(for: [FinanceMovement.self, SavingsGoalRecord.self, FinanceSetting.self])
    }
}

/// Construye el ViewModel una sola vez con el contexto persistente de SwiftData.
struct BootstrapView: View {
    @Environment(\.modelContext) private var context
    @State private var viewModel: FinanceViewModel?

    var body: some View {
        Group {
            if let viewModel {
                MainTabView(model: viewModel)
            } else {
                SplashView()
            }
        }
        .task {
            guard viewModel == nil else { return }
            viewModel = FinanceViewModel(repository: FinanceRepository(context: context))
        }
    }
}

enum AppTab: String, CaseIterable, Identifiable {
    case home, movements, add, goals, profile
    var id: Self { self }

    var label: String {
        switch self {
        case .home: "Inicio"
        case .movements: "Movimientos"
        case .add: "Añadir"
        case .goals: "Metas"
        case .profile: "Perfil"
        }
    }

    var symbol: String {
        switch self {
        case .home: "house.fill"
        case .movements: "list.bullet.rectangle"
        case .add: "plus.circle.fill"
        case .goals: "target"
        case .profile: "person.fill"
        }
    }
}

struct MainTabView: View {
    @ObservedObject var model: FinanceViewModel
    @State private var selectedTab: AppTab = .home

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationStack {
                HomeScreen(model: model, selectedTab: $selectedTab)
            }
            .tabItem { Label(AppTab.home.label, systemImage: AppTab.home.symbol) }
            .tag(AppTab.home)

            NavigationStack {
                MovementsScreen(model: model)
            }
            .tabItem { Label(AppTab.movements.label, systemImage: AppTab.movements.symbol) }
            .tag(AppTab.movements)

            NavigationStack {
                AddMovementScreen(model: model, selectedTab: $selectedTab)
            }
            .tabItem { Label(AppTab.add.label, systemImage: AppTab.add.symbol) }
            .tag(AppTab.add)

            NavigationStack {
                GoalsScreen(model: model)
            }
            .tabItem { Label(AppTab.goals.label, systemImage: AppTab.goals.symbol) }
            .tag(AppTab.goals)

            NavigationStack {
                ProfileScreen(model: model)
            }
            .tabItem { Label(AppTab.profile.label, systemImage: AppTab.profile.symbol) }
            .tag(AppTab.profile)
        }
        .tint(Brand.teal)
        .alert("Mi Puente Financiero", isPresented: Binding(
            get: { model.alertMessage != nil },
            set: { if !$0 { model.alertMessage = nil } }
        )) {
            Button("Entendido", role: .cancel) { model.alertMessage = nil }
        } message: {
            Text(model.alertMessage ?? "")
        }
    }
}

import SwiftUI

struct SplashView: View {
    var body: some View {
        ZStack {
            LinearGradient(colors: [Brand.teal, Brand.navy], startPoint: .topLeading, endPoint: .bottomTrailing)
                .ignoresSafeArea()
            VStack(spacing: 18) {
                Image("BrandIcon")
                    .resizable().scaledToFit()
                    .frame(width: 116, height: 116)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .accessibilityHidden(true)
                Text("Mi Puente Financiero")
                    .font(.largeTitle.bold()).multilineTextAlignment(.center)
                Text("Control y ahorro, sin enredos")
                    .font(.headline.weight(.regular)).foregroundStyle(.white.opacity(0.83))
            }
            .foregroundStyle(.white)
            .padding(32)
        }
    }
}

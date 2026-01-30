import SwiftUI

struct SplashView: View {
    var body: some View {
        ZStack {
            // Match LaunchScreen.storyboard exactly for seamless transition
            Color(.systemBackground)
                .ignoresSafeArea()

            Image("LogoIcon")
                .resizable()
                .scaledToFit()
                .frame(width: 100, height: 100)
        }
    }
}

#Preview {
    SplashView()
}

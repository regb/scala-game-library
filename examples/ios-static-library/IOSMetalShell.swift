import MetalKit
import QuartzCore
import UIKit

final class TouchMetalView: MTKView {
    private var touchIds: [ObjectIdentifier: Int32] = [:]
    private var nextTouchId: Int32 = 0

    private func forward(_ touches: Set<UITouch>, eventType: Int32) {
        let displayScale = Float(window?.screen.scale ?? UIScreen.main.scale)
        for touch in touches {
            let key = ObjectIdentifier(touch)
            let pointerId: Int32
            if let existing = touchIds[key] {
                pointerId = existing
            } else {
                pointerId = nextTouchId
                nextTouchId &+= 1
                touchIds[key] = pointerId
            }
            let point = touch.location(in: self)
            sgl_ios_pointer(eventType, Float(point.x) * displayScale, Float(point.y) * displayScale, pointerId)
            if eventType == 2 || eventType == 3 { touchIds.removeValue(forKey: key) }
        }
    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) { forward(touches, eventType: 0) }
    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) { forward(touches, eventType: 1) }
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) { forward(touches, eventType: 2) }
    override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) { forward(touches, eventType: 3) }
}

final class Renderer: NSObject, MTKViewDelegate {
    private var previousTime = CACurrentMediaTime()

    private static func forwardSafeArea(_ view: MTKView, scale: Float) {
        let insets = view.safeAreaInsets
        sgl_ios_safe_area(
            Int32((insets.left * CGFloat(scale)).rounded()),
            Int32((insets.top * CGFloat(scale)).rounded()),
            Int32((insets.right * CGFloat(scale)).rounded()),
            Int32((insets.bottom * CGFloat(scale)).rounded())
        )
    }

    init?(view: MTKView) {
        guard let device = MTLCreateSystemDefaultDevice(),
              let layer = view.layer as? CAMetalLayer else { return nil }
        view.device = device
        view.colorPixelFormat = .bgra8Unorm
        view.framebufferOnly = false
        view.preferredFramesPerSecond = 60
        let scale = Float(view.window?.screen.scale ?? UIScreen.main.scale)
        view.drawableSize = CGSize(
            width: max(1, view.bounds.width * CGFloat(scale)),
            height: max(1, view.bounds.height * CGFloat(scale))
        )
        super.init()
        let size = view.drawableSize
        sgl_ios_attach_metal_layer(Unmanaged.passUnretained(layer).toOpaque(), scale)
        Self.forwardSafeArea(view, scale: scale)
        guard sgl_ios_initialize(Int32(size.width), Int32(size.height), scale) != 0 else { return nil }
    }

    func mtkView(_ view: MTKView, drawableSizeWillChange size: CGSize) {
        let scale = Float(view.window?.screen.scale ?? UIScreen.main.scale)
        Self.forwardSafeArea(view, scale: scale)
        sgl_ios_resize(Int32(size.width), Int32(size.height), scale)
    }

    func draw(in view: MTKView) {
        let now = CACurrentMediaTime()
        let delta = min(0.25, max(0, now - previousTime))
        previousTime = now
        _ = sgl_ios_frame(delta)
    }
}

@MainActor
final class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?
    private var renderer: Renderer?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        guard let windowScene = scene as? UIWindowScene else { return }
        let window = UIWindow(windowScene: windowScene)
        let controller = UIViewController()
        let metalView = TouchMetalView(frame: windowScene.coordinateSpace.bounds)
        metalView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        controller.view = metalView
        window.rootViewController = controller
        window.makeKeyAndVisible()
        metalView.frame = window.bounds
        metalView.layoutIfNeeded()
        self.window = window
        renderer = Renderer(view: metalView)
        metalView.delegate = renderer
    }

    func sceneDidBecomeActive(_ scene: UIScene) { sgl_ios_resume() }
    func sceneWillResignActive(_ scene: UIScene) { sgl_ios_pause() }
    func sceneDidDisconnect(_ scene: UIScene) { sgl_ios_dispose() }
}

@main
@MainActor
final class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        return ScalaNativeInit() == 0
    }

    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        let configuration = UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
        configuration.delegateClass = SceneDelegate.self
        return configuration
    }
}

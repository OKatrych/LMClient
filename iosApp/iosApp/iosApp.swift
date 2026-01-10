import UIKit
import LMClientShared

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    private var stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: nil)
    private var backDispatcher: BackDispatcher = BackDispatcherKt.BackDispatcher()
    private lazy var appComponent: AppComponent = AppComponentImpl(
        componentContext: DefaultComponentContext(
            lifecycle: ApplicationLifecycle(),
            stateKeeper: stateKeeper,
            instanceKeeper: nil,
            backHandler: backDispatcher
        )
    )

    var window: UIWindow?


    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        let debug = isDebug()
        AppInitializer.shared.doInit(coinConfiguration: { configuration in
            configuration.properties(values: [
                KoinApp.shared.PROPERTY_IS_DEBUG: debug
            ])
        })
        
        window = UIWindow(frame: UIScreen.main.bounds)
        if let window = window {
            window.rootViewController = MainKt.MainViewController(component: appComponent)
            window.makeKeyAndVisible()
        }
        return true
    }

    func application(_ application: UIApplication, shouldSaveSecureApplicationState coder: NSCoder) -> Bool {
        //StateKeeperUtilsKt.save(coder: coder, state: stateKeeper.save())
        return true
    }

    func application(_ application: UIApplication, shouldRestoreSecureApplicationState coder: NSCoder) -> Bool {
        //stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: StateKeeperUtilsKt.restore(coder: coder))
        return true
    }
    
    func isDebug() -> Bool {
        #if DEBUG
            return true
        #else
            return false
        #endif
    }
}

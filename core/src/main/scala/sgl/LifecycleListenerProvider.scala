package sgl

trait LifecycleListenerProvider {

  val lifecycleListener: LifecycleListener = SilentLifecyclieListener

}

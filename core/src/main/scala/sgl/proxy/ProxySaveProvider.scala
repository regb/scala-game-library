package sgl
package proxy

/** Persistent save component backed by the active [[PlatformProxy]]. */
trait ProxySaveProvider extends SaveComponent {
  val PlatformProxy: PlatformProxy

  /** Platform-specific save namespace. Keep it stable across releases. */
  def SaveName: String

  override type Save = AbstractSave
  override lazy val Save: Save = PlatformProxy.createSave(SaveName)
}

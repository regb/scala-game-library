package sgl.ads

/** Optional capability of an Ads backend: the privacy message that some regions
  * require before ads (with AdMob: Google's User Messaging Platform).
  *
  * The game decides when: it calls updateConsent once per launch before its
  * first ad, shows the form at a good moment if isConsentFormReady, and loads
  * ads only once canRequestAds. Where no message is required, canRequestAds
  * becomes true after updateConsent and no form is ever ready.
  *
  * Callbacks get a native error code, 0 for none, and come on a native thread
  * (Android: the UI thread). Calling any of these methods is safe from the game
  * loop, and the status can be polled at any time.
  */
trait ConsentAds {
  /** Asks for the current requirements, and loads the form if one must be shown.
    * May be called again after an error. */
  def updateConsent(onDone: (Int) => Unit): Unit

  /** Whether ads may be requested: the choice was made, or none is required.
    * Before updateConsent it is false. */
  def canRequestAds: Boolean

  /** The form must be shown before ads, and it is loaded. */
  def isConsentFormReady: Boolean

  /** Shows the form over the game. Returns false, without a callback, if none is ready. */
  def showConsentForm(onClosed: (Int) => Unit): Boolean

  /** The game must offer a way to change the choice (a button calling showPrivacyOptions). */
  def isPrivacyOptionsRequired: Boolean

  def showPrivacyOptions(onClosed: (Int) => Unit): Unit
}

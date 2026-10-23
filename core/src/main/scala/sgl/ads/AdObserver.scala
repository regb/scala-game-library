package sgl.ads

/** Optional capability. Backends without native lifecycle events do not implement it.
  * Events describe SDK activity, not game eligibility or placement opportunities.
  */
trait ObservableAds {
  def setAdObserver(observer: AdObserver): Unit
}

/** Native SDK precision code: 0 unknown, 1 estimated, 2 publisher-provided, 3 precise. */
final case class AdRevenue(
    valueMicros: Long,
    currencyCode: String,
    precision: Int,
    network: String,
    format: String,
    adUnitId: String
) {
  def currencyUnits: Double = valueMicros.toDouble / 1000000.0
}

/** Optional callbacks. Android delivers these on the UI thread.
  * format is "interstitial" or "rewarded". Error codes are native SDK codes.
  * Paid callbacks are observations only. Do not forward them to Firebase when
  * the AdMob/Firebase integration already owns automatic ad_impression logging.
  * Observers must not throw or block. Games own opportunity/eligibility events.
  */
abstract class AdObserver {
  def onRequest(format: String, adUnitId: String): Unit = ()
  def onLoaded(format: String, adUnitId: String): Unit = ()
  def onLoadFailed(format: String, adUnitId: String, errorCode: Int): Unit = ()
  def onImpression(format: String, adUnitId: String): Unit = ()
  def onDismissed(format: String, adUnitId: String): Unit = ()
  def onShowFailed(format: String, adUnitId: String, errorCode: Int): Unit = ()
  def onPaid(revenue: AdRevenue): Unit = ()
}

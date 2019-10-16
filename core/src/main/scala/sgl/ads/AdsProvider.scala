package sgl
package ads

trait AdsProvider {

  /** Controls how ads are preloaded.
    *
    * If true, ads (that support preloading) are always preloaded, starting
    * from the app initialization and then each time after an ads is shown, the
    * next ads is already starting to preload.
    *
    * If false, you will need to manually invoke the loading of an ads, because
    * we cannot show an ads that isn't loaded (no blocking call). This design
    * follows good practices, because ads (in particular interstitial and
    * videos) can take a few seconds to load (they must come from an advertiser
    * somewhere, because of the dynamic nature of ads market), and because it's
    * important for the flow of the game that an ads is shown at the correct
    * timing, Thus it should be possible to make a show call that will complete
    * asynchronously.
    */
  val AlwaysPreload: Boolean

  trait Ads {

    /** Explicitly load the interstitial ads. */
    def loadInterstitial(): Unit

    /** Show the interstitial ads.
      *
      * This will return true if the ads was shown, or false if it
      * wasn't yet loaded (and thus not shown).
      */
    def showInterstitial(): Boolean

    /** Check if the interstitial is ready to be shown. */
    def isInterstitialLoaded(): Boolean
  }

  /** Ads provides the central controller for Ads displayed in the game.
    *
    * This is a somewhat simplified version of reality, that will assume a
    * single global Interstitial unit. Some framework supports differenciating
    * interstitial (that you can configure individually on their platform) but
    * this won't be the case with this abstraction here. This assumes that
    * there is a single interstitial (after all, one can never show more than
    * one interstitial at a time) and it can be controlled with various
    * methods.
    *
    * Currently this only supports interstitial, but this should be extended to
    * reward videos and banner ads as needed (when somebody will want to make a
    * game with these).
    */
  val Ads: Ads

}

/** AdsProviders that doesn't show any ads.
  *
  * NoAdsProvider pretends that ads are loaded and ready to show, but doesn't
  * actually load and show anything. This is a convenient way to make an ads
  * free version if your app implements the ads logic, you can just inject this
  * backend instead of a working backend and it will look as if no ads were
  * shown.
  **/
trait NoAdsProvider extends AdsProvider {

  override val AlwaysPreload: Boolean = true

  object NoAds extends Ads {
    def loadInterstitial(): Unit = {}

    def showInterstitial(): Boolean = true

    def isInterstitialLoaded(): Boolean = true
  }
  override val Ads = NoAds

}


// TODO: FakeAdsProvider should display a fake add (actually draw soething on
// top of the screen) it would be a good local testing strategy (without the
// need for the testing config of the ads backend).

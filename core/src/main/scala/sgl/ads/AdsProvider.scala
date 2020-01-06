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

    /** Explicitly load the interstitial ads.
      *
      * Not necessary if AlwaysPreload is on. If the ad is already loaded/loading,
      * this will be a no-op.
      **/
    def loadInterstitial(): Unit

    /** Check if the interstitial is ready to be shown. */
    def isInterstitialLoaded: Boolean

    /** Show the interstitial ads.
      *
      * This will return true if the ads was loaded and ready to show.  Note
      * that it will show the ads in an asynchronous way (this returns
      * immediately), meaning that the function will return first and then the
      * ads might get shown half a second later.
      */
    def showInterstitial(): Boolean
    // TODO: Export an onClose (and maybe onFailed) callback so that we can do
    //       an explicit action when the ad is completed, because otherwise we
    //       do not know when to resume the game.


    def loadRewarded(): Unit

    def isRewardedLoaded: Boolean

    /** Show a rewarded ad.
      *
      * The policy for rewarded ads is that the player must be shown an
      * explicit choice to see the ad before hand (it has to be opt in). If the
      * player says yes, then we can show a rewarded ad. This will typically be
      * a video ad, and if the user watches for long enough, it will earn the
      * reward.  Whether the user earned the reward or not will be specified by
      * the Boolean argument of the onClosed callback.
      *
      * In practice, users have the options to close the ad before the required
      * time, meaning that they would not get the reward. Ads implementation
      * would typically display a warning to the user that they will not
      * receive the reward, so it is fine to process a value of false as a
      * non-reward and to not give the user the promised reward.
      *
      * The showRewarded call itself is asynchronous, it returns immediately
      * and the ad might be shown anytime within the next few seconds. The returned
      * Boolean value is whether the ad was loaded or not, and thus whether the
      * ad is going to show or not.
      */
    def showRewarded(onClosed: (Boolean) => Unit): Boolean
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
    override def loadInterstitial(): Unit = {}
    override def isInterstitialLoaded: Boolean = true
    override def showInterstitial(): Boolean = true

    override def loadRewarded(): Unit = {}
    override def isRewardedLoaded: Boolean = true
    override def showRewarded(onClosed: (Boolean) => Unit): Boolean = {
      // TODO: async? How about false?
      onClosed(true)
      false
    }
  }
  override val Ads = NoAds

}


// TODO: FakeAdsProvider should display a fake add (actually draw soething on
// top of the screen) it would be a good local testing strategy (without the
// need for the testing config of the ads backend).

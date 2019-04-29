package sgl
package android
package ads

import sgl.ads._

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd

import _root_.android.app.Activity
import _root_.android.os.Bundle

trait GoogleAdsProvider extends Activity with AdsProvider {

  val AdUnitId: Option[String] = None

  /** The special name for a test AdUnitId.
    *
    * This value will be used if the AdUnitId is
    * not set.
    */
  val TestAdUnitId = "/6499/example/interstitial"

  private var publisherInterstitialAd: PublisherInterstitialAd = null

  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)

    publisherInterstitialAd = new PublisherInterstitialAd(this)
    publisherInterstitialAd.setAdUnitId(AdUnitId.getOrElse(TestAdUnitId))

    if(AlwaysPreload) {
      publisherInterstitialAd.loadAd(new PublisherAdRequest.Builder().build())
      publisherInterstitialAd.setAdListener(new AdListener {
        override def onAdClosed() {
          publisherInterstitialAd.loadAd(new PublisherAdRequest.Builder().build())
        }
      })
    }
  }

  object GoogleAds extends Ads {

    override def loadInterstitial(): Unit = {
      publisherInterstitialAd.loadAd(new PublisherAdRequest.Builder().build())
    }

    override def isInterstitialLoaded(): Boolean = {
      publisherInterstitialAd.isLoaded
    }

    override def showInterstitial(): Boolean = {
      if(publisherInterstitialAd.isLoaded) {
        publisherInterstitialAd.show();
        true
      } else {
        false
      }
    }
  }
  override val Ads = GoogleAds

}

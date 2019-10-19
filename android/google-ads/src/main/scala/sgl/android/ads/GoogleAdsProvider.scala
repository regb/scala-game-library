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

  private var isLoaded = false

  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)

    publisherInterstitialAd = new PublisherInterstitialAd(this)
    publisherInterstitialAd.setAdUnitId(AdUnitId.getOrElse(TestAdUnitId))
    publisherInterstitialAd.setAdListener(new AdListener {
      override def onAdLoaded(): Unit = {
        isLoaded = true
      }

      override def onAdFailedToLoad(errorCode: Int): Unit = {
        // Code to be executed when an ad request fails.
      }

      override def onAdOpened(): Unit = {
        // Code to be executed when the ad is displayed.
      }

      override def onAdClicked(): Unit = {
        // Code to be executed when the user clicks on an ad.
      }

      override def onAdLeftApplication(): Unit = {
        // Code to be executed when the user has left the app.
      }

      override def onAdClosed(): Unit = {
        if(AlwaysPreload)
          publisherInterstitialAd.loadAd(new PublisherAdRequest.Builder().build())
      }
    })

    if(AlwaysPreload) {
      publisherInterstitialAd.loadAd(new PublisherAdRequest.Builder().build())
    }
  }

  object GoogleAds extends Ads {

    override def loadInterstitial(): Unit = {
      runOnUiThread(new Runnable {
        override def run(): Unit = {
          publisherInterstitialAd.loadAd(new PublisherAdRequest.Builder().build())
        }
      })
    }

    override def isInterstitialLoaded: Boolean = isLoaded

    override def showInterstitial(): Boolean = {
      if(isLoaded) {
        runOnUiThread(new Runnable {
          override def run(): Unit = {
            publisherInterstitialAd.show();
          }
        })
        true
      } else {
        false
      }
    }
  }
  override val Ads = GoogleAds

}

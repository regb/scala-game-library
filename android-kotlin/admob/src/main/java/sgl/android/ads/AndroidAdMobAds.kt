package sgl.android.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import sgl.ads.AdsProvider
import scala.Function1
import scala.runtime.BoxedUnit

/**
 * Android AdMob implementation of SGL's Ads controller.
 *
 * This class is intentionally packaged in the optional :sgl-android-admob
 * module so games that do not opt into ads do not carry the Google Mobile Ads
 * SDK in their APK/AAB.
 */
class AndroidAdMobAds(
    context: Context,
    private val alwaysPreload: Boolean = true,
    private val interstitialAdUnitId: String = TEST_INTERSTITIAL_AD_UNIT_ID,
    private val rewardedAdUnitId: String = TEST_REWARDED_AD_UNIT_ID,
) : AdsProvider.Ads {

    private val activity: Activity = context as? Activity
        ?: error("AndroidAdMobAds requires an Activity context")

    init {
        initialize(activity) {
            if (alwaysPreload) {
                loadInterstitial()
                loadRewarded()
            }
        }
    }

    override fun loadInterstitial() {
        if (isInterstitialLoading || interstitialAd != null) return
        isInterstitialLoading = true
        activity.runOnUiThread {
            InterstitialAd.load(
                activity,
                interstitialAdUnitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        isInterstitialLoading = false
                        interstitialAd = ad
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                interstitialAd = null
                                if (alwaysPreload) loadInterstitial()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                                interstitialAd = null
                                if (alwaysPreload) loadInterstitial()
                            }
                        }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isInterstitialLoading = false
                        interstitialAd = null
                    }
                },
            )
        }
    }

    override fun isInterstitialLoaded(): Boolean = interstitialAd != null

    override fun showInterstitial(): Boolean {
        val ad = interstitialAd ?: return false
        interstitialAd = null
        activity.runOnUiThread { ad.show(activity) }
        return true
    }

    override fun loadRewarded() {
        if (isRewardedLoading || rewardedAd != null) return
        isRewardedLoading = true
        activity.runOnUiThread {
            RewardedAd.load(
                activity,
                rewardedAdUnitId,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        isRewardedLoading = false
                        rewardedAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isRewardedLoading = false
                        rewardedAd = null
                    }
                },
            )
        }
    }

    override fun isRewardedLoaded(): Boolean = rewardedAd != null

    override fun showRewarded(onClosed: Function1<Any, BoxedUnit>): Boolean {
        val ad = rewardedAd ?: return false
        rewardedAd = null
        activity.runOnUiThread {
            var earned = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onClosed.apply(earned)
                    if (alwaysPreload) loadRewarded()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    onClosed.apply(false)
                    if (alwaysPreload) loadRewarded()
                }
            }
            ad.show(activity) { _: RewardItem -> earned = true }
        }
        return true
    }

    companion object {
        const val TEST_ADMOB_APPLICATION_ID = "ca-app-pub-3940256099942544~3347511713"
        const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

        @Volatile
        private var initialized = false

        @Volatile
        private var initializing = false

        private val pendingInitializationCallbacks = mutableListOf<() -> Unit>()

        @Volatile
        private var interstitialAd: InterstitialAd? = null

        @Volatile
        private var isInterstitialLoading = false

        @Volatile
        private var rewardedAd: RewardedAd? = null

        @Volatile
        private var isRewardedLoading = false

        fun fromResources(context: Context): AndroidAdMobAds {
            val resources = context.resources
            val packageName = context.packageName
            fun stringResource(name: String, defaultValue: String): String {
                val id = resources.getIdentifier(name, "string", packageName)
                return if (id == 0) defaultValue else resources.getString(id)
            }
            fun boolResource(name: String, defaultValue: Boolean): Boolean {
                val id = resources.getIdentifier(name, "bool", packageName)
                return if (id == 0) defaultValue else resources.getBoolean(id)
            }

            return AndroidAdMobAds(
                context = context,
                alwaysPreload = boolResource("sgl_admob_always_preload", true),
                interstitialAdUnitId = stringResource("sgl_admob_interstitial_ad_unit_id", TEST_INTERSTITIAL_AD_UNIT_ID),
                rewardedAdUnitId = stringResource("sgl_admob_rewarded_ad_unit_id", TEST_REWARDED_AD_UNIT_ID),
            )
        }

        private fun initialize(activity: Activity, onInitialized: () -> Unit) {
            synchronized(this) {
                if (initialized) {
                    onInitialized()
                    return
                }
                pendingInitializationCallbacks += onInitialized
                if (initializing) return
                initializing = true
            }

            activity.runOnUiThread {
                MobileAds.initialize(activity) {
                    val callbacks = synchronized(this) {
                        initialized = true
                        initializing = false
                        pendingInitializationCallbacks.toList().also {
                            pendingInitializationCallbacks.clear()
                        }
                    }
                    callbacks.forEach { it() }
                }
            }
        }
    }
}

package sgl.android.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import sgl.ads.AdObserver
import sgl.ads.AdsProvider
import sgl.ads.ObservableAds
import scala.Function1
import scala.runtime.BoxedUnit

/** Optional AdMob backend. No analytics events are logged by this controller. */
class AndroidAdMobAds(
    context: Context,
    private val alwaysPreload: Boolean = true,
    private val interstitialAdUnitId: String = TEST_INTERSTITIAL_AD_UNIT_ID,
    private val rewardedAdUnitId: String = TEST_REWARDED_AD_UNIT_ID,
) : AdsProvider.Ads, ObservableAds {
    private val activity = context as? Activity ?: error("AndroidAdMobAds requires an Activity context")
    @Volatile private var observer: AdObserver? = null
    @Volatile private var interstitial: InterstitialAd? = null
    @Volatile private var rewarded: RewardedAd? = null
    private var interstitialLoading = false
    private var rewardedLoading = false
    private var interstitialShowing = false
    private var rewardedShowing = false
    @Volatile private var destroyed = false
    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityDestroyed(owner: Activity) { if (owner === activity) destroy() }
        override fun onActivityCreated(owner: Activity, state: Bundle?) {}
        override fun onActivityStarted(owner: Activity) {}
        override fun onActivityResumed(owner: Activity) {}
        override fun onActivityPaused(owner: Activity) {}
        override fun onActivityStopped(owner: Activity) {}
        override fun onActivitySaveInstanceState(owner: Activity, state: Bundle) {}
    }

    override fun setAdObserver(observer: AdObserver) { this.observer = observer }

    init {
        activity.application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
        activity.runOnUiThread {
            MobileAds.initialize(activity.applicationContext) {
                activity.runOnUiThread {
                    if (alwaysPreload && !destroyed && !activity.isDestroyed) {
                        loadInterstitial()
                        loadRewarded()
                    }
                }
            }
        }
    }

    /** Automatic on Activity destruction; may also be called earlier by the owner. */
    @Synchronized fun destroy() {
        if (destroyed) return
        destroyed = true
        activity.application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        observer = null
        interstitial = null
        rewarded = null
    }

    @Synchronized override fun loadInterstitial() {
        if (destroyed || interstitialLoading || interstitialShowing || interstitial != null) return
        interstitialLoading = true
        activity.runOnUiThread {
            if (destroyed || activity.isDestroyed) return@runOnUiThread
            observer?.onRequest("interstitial", interstitialAdUnitId)
            InterstitialAd.load(activity, interstitialAdUnitId, AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        synchronized(this@AndroidAdMobAds) {
                            interstitialLoading = false
                            if (destroyed || activity.isDestroyed) return
                            val lifecycle = AdLifecycle({ observer }, "interstitial", interstitialAdUnitId)
                            ad.setOnPaidEventListener {
                                lifecycle.paid(it.valueMicros, it.currencyCode, it.precisionType,
                                    ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: "AdMob")
                            }
                            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdImpression() = lifecycle.impression()
                                override fun onAdDismissedFullScreenContent() {
                                    if (lifecycle.close(beforeNotify = { interstitialFinished() })) {
                                        if (alwaysPreload) loadInterstitial()
                                    }
                                }
                                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                    if (lifecycle.close(error.code) { interstitialFinished() }) {
                                        if (alwaysPreload) loadInterstitial()
                                    }
                                }
                            }
                            interstitial = ad
                        }
                        observer?.onLoaded("interstitial", interstitialAdUnitId)
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        synchronized(this@AndroidAdMobAds) { interstitialLoading = false }
                        observer?.onLoadFailed("interstitial", interstitialAdUnitId, error.code)
                    }
                })
        }
    }

    @Synchronized private fun interstitialFinished() {
        interstitialShowing = false
    }
    override fun isInterstitialLoaded(): Boolean = interstitial != null
    @Synchronized override fun showInterstitial(): Boolean {
        val ad = interstitial ?: return false
        if (destroyed || activity.isDestroyed) return false
        interstitial = null
        interstitialShowing = true
        activity.runOnUiThread { if (!destroyed && !activity.isDestroyed) ad.show(activity) }
        return true
    }

    @Synchronized override fun loadRewarded() {
        if (destroyed || rewardedLoading || rewardedShowing || rewarded != null) return
        rewardedLoading = true
        activity.runOnUiThread {
            if (destroyed || activity.isDestroyed) return@runOnUiThread
            observer?.onRequest("rewarded", rewardedAdUnitId)
            RewardedAd.load(activity, rewardedAdUnitId, AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        synchronized(this@AndroidAdMobAds) {
                            rewardedLoading = false
                            if (destroyed || activity.isDestroyed) return
                            rewarded = ad
                        }
                        observer?.onLoaded("rewarded", rewardedAdUnitId)
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        synchronized(this@AndroidAdMobAds) { rewardedLoading = false }
                        observer?.onLoadFailed("rewarded", rewardedAdUnitId, error.code)
                    }
                })
        }
    }

    override fun isRewardedLoaded(): Boolean = rewarded != null
    @Synchronized override fun showRewarded(onClosed: Function1<Any, BoxedUnit>): Boolean {
        val ad = rewarded ?: return false
        if (destroyed || activity.isDestroyed) return false
        rewarded = null
        rewardedShowing = true
        activity.runOnUiThread {
            if (destroyed || activity.isDestroyed) {
                onClosed.apply(false)
                return@runOnUiThread
            }
            var earned = false
            val lifecycle = AdLifecycle({ observer }, "rewarded", rewardedAdUnitId)
            ad.setOnPaidEventListener {
                lifecycle.paid(it.valueMicros, it.currencyCode, it.precisionType,
                    ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: "AdMob")
            }
            fun finish(error: Int? = null) {
                if (!lifecycle.close(error) {
                    synchronized(this@AndroidAdMobAds) { rewardedShowing = false }
                }) return
                onClosed.apply(error == null && earned)
                if (alwaysPreload) loadRewarded()
            }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdImpression() = lifecycle.impression()
                override fun onAdDismissedFullScreenContent() = finish()
                override fun onAdFailedToShowFullScreenContent(error: AdError) = finish(error.code)
            }
            ad.show(activity) { earned = true }
        }
        return true
    }

    companion object {
        const val TEST_ADMOB_APPLICATION_ID = "ca-app-pub-3940256099942544~3347511713"
        const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

        fun fromResources(context: Context): AndroidAdMobAds {
            val resources = context.resources
            val packageName = context.packageName
            fun stringResource(name: String, default: String): String {
                val id = resources.getIdentifier(name, "string", packageName)
                return if (id == 0) default else resources.getString(id)
            }
            val preloadId = resources.getIdentifier("sgl_admob_always_preload", "bool", packageName)
            return AndroidAdMobAds(context,
                if (preloadId == 0) true else resources.getBoolean(preloadId),
                stringResource("sgl_admob_interstitial_ad_unit_id", TEST_INTERSTITIAL_AD_UNIT_ID),
                stringResource("sgl_admob_rewarded_ad_unit_id", TEST_REWARDED_AD_UNIT_ID))
        }
    }
}

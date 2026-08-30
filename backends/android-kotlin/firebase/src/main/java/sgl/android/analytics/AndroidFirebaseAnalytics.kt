package sgl.android.analytics

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import scala.Option
import scala.Tuple2
import scala.collection.immutable.Seq
import sgl.analytics.AbstractAnalytics
import sgl.analytics.AnalyticsBooleanValue
import sgl.analytics.AnalyticsDoubleValue
import sgl.analytics.AnalyticsLongValue
import sgl.analytics.AnalyticsStringValue
import sgl.analytics.AnalyticsValue
import sgl.analytics.EventParams
import com.google.firebase.analytics.FirebaseAnalytics

class AndroidFirebaseAnalytics(private val context: Context) : AbstractAnalytics() {
    
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    init {
        if (Settings.System.getString(context.contentResolver, "firebase.test.lab") == "true") {
            firebaseAnalytics.setAnalyticsCollectionEnabled(false)
        }
    }

    private fun putAnalyticsValue(bundle: Bundle, key: String, value: AnalyticsValue?) {
        when (value) {
            is AnalyticsStringValue -> bundle.putString(key, value.value())
            is AnalyticsLongValue -> bundle.putLong(key, value.value())
            is AnalyticsDoubleValue -> bundle.putDouble(key, value.value())
            is AnalyticsBooleanValue -> bundle.putLong(key, if (value.value()) 1L else 0L)
        }
    }

    private fun putCustoms(bundle: Bundle, customs: Seq<Tuple2<String, AnalyticsValue>>?) {
        customs?.let {
            val iterator = it.iterator()
            while (iterator.hasNext()) {
                val tuple = iterator.next()
                putAnalyticsValue(bundle, tuple._1(), tuple._2())
            }
        }
    }
    
    private fun eventParamsToBundle(params: EventParams?): Bundle {
        val bundle = Bundle()
        params?.let { p ->
            p.level().getOrNull()?.let { 
                when (it) {
                    is Long -> bundle.putLong(FirebaseAnalytics.Param.LEVEL, it)
                    is Int -> bundle.putLong(FirebaseAnalytics.Param.LEVEL, it.toLong())
                    is Number -> bundle.putLong(FirebaseAnalytics.Param.LEVEL, it.toLong())
                }
            }
            p.value().getOrNull()?.let { 
                when (it) {
                    is Double -> bundle.putDouble(FirebaseAnalytics.Param.VALUE, it)
                    is Float -> bundle.putDouble(FirebaseAnalytics.Param.VALUE, it.toDouble())
                    is Number -> bundle.putDouble(FirebaseAnalytics.Param.VALUE, it.toDouble())
                }
            }
            p.itemId().getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.ITEM_ID, it) }
            p.score().getOrNull()?.let { 
                when (it) {
                    is Long -> bundle.putLong(FirebaseAnalytics.Param.SCORE, it)
                    is Int -> bundle.putLong(FirebaseAnalytics.Param.SCORE, it.toLong())
                    is Number -> bundle.putLong(FirebaseAnalytics.Param.SCORE, it.toLong())
                }
            }
            p.levelName().getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, it) }
            p.character().getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.CHARACTER, it) }
            
            val customs = p.customs()
            if (customs != null) {
                val iterator = customs.iterator()
                while (iterator.hasNext()) {
                    val tuple = iterator.next()
                    putAnalyticsValue(bundle, tuple._1(), tuple._2())
                }
            }
        }
        return bundle
    }
    
    private fun <T> Option<T>?.getOrNull(): T? {
        return this?.let { if (it.isDefined) it.get() else null }
    }

    override fun logCustomEvent(name: String?, params: EventParams?) {
        name?.let {
            firebaseAnalytics.logEvent(it, eventParamsToBundle(params))
        }
    }

    override fun logLevelUpEvent(level: Long, customs: Seq<Tuple2<String, AnalyticsValue>>?) {
        val bundle = Bundle()
        bundle.putLong(FirebaseAnalytics.Param.LEVEL, level)
        putCustoms(bundle, customs)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle)
    }

    override fun logLevelStartEvent(levelName: String?, customs: Seq<Tuple2<String, AnalyticsValue>>?) {
        val bundle = Bundle()
        levelName?.let { bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, it) }
        putCustoms(bundle, customs)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_START, bundle)
    }

    override fun logLevelEndEvent(levelName: String?, success: Boolean, customs: Seq<Tuple2<String, AnalyticsValue>>?) {
        val bundle = Bundle()
        levelName?.let { bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, it) }
        bundle.putLong(FirebaseAnalytics.Param.SUCCESS, if (success) 1 else 0)
        putCustoms(bundle, customs)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_END, bundle)
    }

    override fun logShareEvent(itemId: Option<String>?, customs: Seq<Tuple2<String, AnalyticsValue>>?) {
        val bundle = Bundle()
        itemId.getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.ITEM_ID, it) }
        putCustoms(bundle, customs)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }

    override fun logGameOverEvent(score: Option<Any>?, levelName: Option<String>?, customs: Seq<Tuple2<String, AnalyticsValue>>?) {
        val bundle = Bundle()
        score.getOrNull()?.let { s ->
            when (s) {
                is Long -> bundle.putLong(FirebaseAnalytics.Param.SCORE, s)
                is Int -> bundle.putLong(FirebaseAnalytics.Param.SCORE, s.toLong())
                is Number -> bundle.putLong(FirebaseAnalytics.Param.SCORE, s.toLong())
            }
        }
        levelName.getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, it) }
        putCustoms(bundle, customs)
        firebaseAnalytics.logEvent("game_over", bundle)
    }

    override fun logBeginTutorialEvent(tutorialId: String?, levelName: Option<String>?, customs: Seq<Tuple2<String, AnalyticsValue>>?) {
        val bundle = Bundle()
        tutorialId?.takeIf { it.isNotEmpty() }?.let { bundle.putString("tutorial_id", it) }
        levelName.getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, it) }
        putCustoms(bundle, customs)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN, bundle)
    }

    override fun logCompleteTutorialEvent(tutorialId: String?, levelName: Option<String>?, customs: Seq<Tuple2<String, AnalyticsValue>>?) {
        val bundle = Bundle()
        tutorialId?.takeIf { it.isNotEmpty() }?.let { bundle.putString("tutorial_id", it) }
        levelName.getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, it) }
        putCustoms(bundle, customs)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, bundle)
    }

    override fun logUnlockAchievementEvent(achievementId: String?, customs: Seq<Tuple2<String, AnalyticsValue>>?) {
        achievementId?.let {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, it)
            putCustoms(bundle, customs)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle)
        }
    }

    override fun logPurchaseEvent(transactionId: Option<String>?, value: Double, currency: String?, itemId: Option<String>?, customs: Seq<Tuple2<String, AnalyticsValue>>?) {
        val bundle = Bundle()
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, value)
        currency?.let { bundle.putString(FirebaseAnalytics.Param.CURRENCY, it) }
        transactionId.getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, it) }
        itemId.getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.ITEM_ID, it) }
        putCustoms(bundle, customs)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)
    }

    override fun logPostScoreEvent(score: Long, level: Option<Any>?, character: Option<String>?, customs: Seq<Tuple2<String, AnalyticsValue>>?) {
        val bundle = Bundle()
        bundle.putLong(FirebaseAnalytics.Param.SCORE, score)
        level.getOrNull()?.let { l ->
            when (l) {
                is Long -> bundle.putLong(FirebaseAnalytics.Param.LEVEL, l)
                is Int -> bundle.putLong(FirebaseAnalytics.Param.LEVEL, l.toLong())
                is Number -> bundle.putLong(FirebaseAnalytics.Param.LEVEL, l.toLong())
            }
        }
        character.getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.CHARACTER, it) }
        putCustoms(bundle, customs)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle)
    }

    override fun setGameScreen(gameScreen: String?) {
        gameScreen?.let {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, it)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        }
    }

    override fun setPlayerProperty(name: String?, value: String?) {
        if (name != null && value != null) {
            firebaseAnalytics.setUserProperty(name, value)
        }
    }
}

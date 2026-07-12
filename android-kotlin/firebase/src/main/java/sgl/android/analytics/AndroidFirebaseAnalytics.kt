package sgl.android.analytics

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import scala.Option
import sgl.analytics.AbstractAnalytics
import sgl.analytics.EventParams
import com.google.firebase.analytics.FirebaseAnalytics

class AndroidFirebaseAnalytics(private val context: Context) : AbstractAnalytics() {
    
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    init {
        if (Settings.System.getString(context.contentResolver, "firebase.test.lab") == "true") {
            firebaseAnalytics.setAnalyticsCollectionEnabled(false)
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
            p.levelName().getOrNull()?.let { bundle.putString("level_map", it) }
            p.character().getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.CHARACTER, it) }
            
            // Handle Scala Map iteration
            val customs = p.customs()
            if (customs != null) {
                val iterator = customs.iterator()
                while (iterator.hasNext()) {
                    val tuple = iterator.next()
                    bundle.putString(tuple._1(), tuple._2())
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

    override fun logLevelUpEvent(level: Long) {
        val bundle = Bundle()
        bundle.putLong(FirebaseAnalytics.Param.LEVEL, level)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle)
    }

    override fun logLevelStartEvent(level: String?) {
        val bundle = Bundle()
        level?.let { bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, it) }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_START, bundle)
    }

    override fun logLevelEndEvent(level: String?, success: Boolean) {
        val bundle = Bundle()
        level?.let { bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, it) }
        bundle.putLong(FirebaseAnalytics.Param.SUCCESS, if (success) 1 else 0)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_END, bundle)
    }

    override fun logShareEvent(itemId: Option<String>?) {
        val bundle = Bundle()
        itemId.getOrNull()?.let { bundle.putString(FirebaseAnalytics.Param.ITEM_ID, it) }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }

    override fun logGameOverEvent(score: Option<Any>?, map: Option<String>?) {
        val bundle = Bundle()
        score.getOrNull()?.let { s ->
            when (s) {
                is Long -> bundle.putLong(FirebaseAnalytics.Param.SCORE, s)
                is Int -> bundle.putLong(FirebaseAnalytics.Param.SCORE, s.toLong())
                is Number -> bundle.putLong(FirebaseAnalytics.Param.SCORE, s.toLong())
            }
        }
        map.getOrNull()?.let { bundle.putString("level_map", it) }
        firebaseAnalytics.logEvent("game_over", bundle)
    }

    override fun logBeginTutorialEvent() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN, Bundle())
    }

    override fun logCompleteTutorialEvent() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, Bundle())
    }

    override fun logUnlockAchievementEvent(achievementId: String?) {
        achievementId?.let {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, it)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle)
        }
    }

    override fun logPostScoreEvent(score: Long, level: Option<Any>?, character: Option<String>?) {
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
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle)
    }

    override fun setGameScreen(gameScreen: String?) {
        gameScreen?.let {
            if (context is android.app.Activity) {
                firebaseAnalytics.setCurrentScreen(context, it, null)
            }
        }
    }

    override fun setPlayerProperty(name: String?, value: String?) {
        if (name != null && value != null) {
            firebaseAnalytics.setUserProperty(name, value)
        }
    }
}
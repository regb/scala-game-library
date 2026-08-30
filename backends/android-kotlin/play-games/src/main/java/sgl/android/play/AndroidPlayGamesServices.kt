package sgl.android.play

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk

/**
 * Optional Google Play Games Services helper for Android SGL apps.
 *
 * This module intentionally has no dependency from the base :sgl-android
 * module. Games opt in by including :sgl-android-play-games from the generated
 * Android project and by creating this helper from their Android wiring code.
 */
class AndroidPlayGamesServices(
    context: Context,
    private val autoSignIn: Boolean = true,
) {
    private val activity: Activity = context as? Activity
        ?: error("AndroidPlayGamesServices requires an Activity context")

    @Volatile
    var isAuthenticated: Boolean = false
        private set

    init {
        PlayGamesSdk.initialize(activity)
        if (autoSignIn) {
            signInSilently()
        } else {
            refreshAuthenticationState()
        }
    }

    fun refreshAuthenticationState(onResult: ((Boolean) -> Unit)? = null) {
        PlayGames.getGamesSignInClient(activity)
            .isAuthenticated
            .addOnCompleteListener { task ->
                isAuthenticated = task.isSuccessful && task.result.isAuthenticated
                onResult?.invoke(isAuthenticated)
            }
    }

    fun signInSilently(onResult: ((Boolean) -> Unit)? = null) {
        refreshAuthenticationState { authenticated ->
            if (authenticated) {
                onResult?.invoke(true)
            } else if (autoSignIn) {
                signIn(onResult)
            } else {
                onResult?.invoke(false)
            }
        }
    }

    fun signIn(onResult: ((Boolean) -> Unit)? = null) {
        activity.runOnUiThread {
            PlayGames.getGamesSignInClient(activity)
                .signIn()
                .addOnCompleteListener { task ->
                    isAuthenticated = task.isSuccessful && task.result.isAuthenticated
                    onResult?.invoke(isAuthenticated)
                }
        }
    }

    fun showAchievements(): Boolean {
        if (!isAuthenticated) {
            signIn()
            return false
        }
        activity.runOnUiThread {
            PlayGames.getAchievementsClient(activity)
                .achievementsIntent
                .addOnSuccessListener { intent: Intent ->
                    activity.startActivityForResult(intent, RC_ACHIEVEMENTS)
                }
        }
        return true
    }

    fun showLeaderboard(leaderboardId: String): Boolean {
        if (!isAuthenticated) {
            signIn()
            return false
        }
        activity.runOnUiThread {
            PlayGames.getLeaderboardsClient(activity)
                .getLeaderboardIntent(leaderboardId)
                .addOnSuccessListener { intent: Intent ->
                    activity.startActivityForResult(intent, RC_LEADERBOARDS)
                }
        }
        return true
    }

    fun unlockAchievement(achievementId: String) {
        if (isAuthenticated) {
            PlayGames.getAchievementsClient(activity).unlock(achievementId)
        }
    }

    fun incrementAchievement(achievementId: String, incrementBy: Int) {
        if (isAuthenticated) {
            PlayGames.getAchievementsClient(activity).increment(achievementId, incrementBy)
        }
    }

    fun submitLeaderboardScore(leaderboardId: String, score: Long) {
        if (isAuthenticated) {
            PlayGames.getLeaderboardsClient(activity).submitScore(leaderboardId, score)
        }
    }

    companion object {
        const val RC_ACHIEVEMENTS = 8001
        const val RC_LEADERBOARDS = 9001
    }
}

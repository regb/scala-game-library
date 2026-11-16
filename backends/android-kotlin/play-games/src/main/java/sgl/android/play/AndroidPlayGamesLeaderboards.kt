package sgl.android.play

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.leaderboard.LeaderboardScore
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import scala.Function1
import scala.Option
import scala.runtime.BoxedUnit
import scala.util.Either
import scala.util.Left
import scala.util.Right
import sgl.leaderboards.AbstractLeaderboards
import sgl.leaderboards.LeaderboardEntry
import sgl.leaderboards.LeaderboardSpan

/**
 * Google Play Games Services leaderboards behind SGL's shared
 * [[AbstractLeaderboards]] API (Play Games Services v2 SDK).
 *
 * Play Games signs players in automatically when the game starts; this
 * class only checks that state, and [signIn] asks explicitly (call it from a
 * player action, such as opening a leaderboard). Callbacks run on the UI
 * thread: games store the result and read it from their update loop.
 *
 * The game's manifest needs the `com.google.android.gms.games.APP_ID`
 * metadata, which sgl_android_app adds when `play_games_project_id` is set.
 */
class AndroidPlayGamesLeaderboards(
    context: Context,
    /** Start Play Games (and its automatic sign-in) right away. False: wait for [connect] or
     * [signIn], for games that ask the player first; also set play_games_auto_start = False on
     * sgl_android_app so the SDK does not start itself at launch. */
    connectAtStart: Boolean = true,
) : AbstractLeaderboards() {
    private val activity: Activity = context as? Activity
        ?: error("AndroidPlayGamesLeaderboards requires an Activity context")

    @Volatile
    private var signedIn: Boolean = false
    @Volatile
    private var playerId: String? = null

    @Volatile
    private var connected: Boolean = false

    init {
        if (connectAtStart) connect()
    }

    override fun connect() {
        if (!connected) {
            connected = true
            PlayGamesSdk.initialize(activity)
            refresh(null)
        } else if (!signedIn) {
            // Called again while signed out (the sign-in at start failed, e.g. offline): check
            // again, then ask Play Games to sign in the player, who already agreed to it.
            activity.runOnUiThread {
                val client = PlayGames.getGamesSignInClient(activity)
                client.isAuthenticated.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result.isAuthenticated) updateSignedIn(true, null)
                    else client.signIn().addOnCompleteListener { t ->
                        updateSignedIn(t.isSuccessful && t.result.isAuthenticated, null)
                    }
                }
            }
        }
    }

    private fun refresh(onResult: ((Boolean) -> Unit)?) {
        PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnCompleteListener { task ->
            updateSignedIn(task.isSuccessful && task.result.isAuthenticated, onResult)
        }
    }

    private fun updateSignedIn(authenticated: Boolean, onResult: ((Boolean) -> Unit)?) {
        signedIn = authenticated
        Log.i(TAG, "signed in: $authenticated")
        if (!authenticated) {
            onResult?.invoke(false)
            return
        }
        // The player's id marks their own rows in loaded scores.
        PlayGames.getPlayersClient(activity).currentPlayerId.addOnCompleteListener { task ->
            if (task.isSuccessful) playerId = task.result
            onResult?.invoke(true)
        }
    }

    override fun isAvailable(): Boolean = true

    override fun isSignedIn(): Boolean = signedIn

    override fun signIn(onResult: Function1<Any, BoxedUnit>) {
        if (!connected) {
            connected = true
            PlayGamesSdk.initialize(activity)
        }
        activity.runOnUiThread {
            PlayGames.getGamesSignInClient(activity).signIn().addOnCompleteListener { task ->
                updateSignedIn(task.isSuccessful && task.result.isAuthenticated) { ok -> onResult.apply(ok) }
            }
        }
    }

    override fun submitScore(leaderboardId: String, score: Long) {
        if (!signedIn) {
            Log.i(TAG, "submit skipped (not signed in): $leaderboardId")
            return
        }
        // The immediate variant reports what Play recorded (or why it failed), for the logs.
        PlayGames.getLeaderboardsClient(activity).submitScoreImmediate(leaderboardId, score)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) Log.i(TAG, "submitted $score to $leaderboardId: ${task.result}")
                else Log.w(TAG, "submit to $leaderboardId failed: ${task.exception}")
            }
    }

    override fun loadPlayerScore(
        leaderboardId: String,
        span: LeaderboardSpan,
        onResult: Function1<Option<LeaderboardEntry>, BoxedUnit>,
    ) {
        if (!signedIn) {
            onResult.apply(Option.empty())
            return
        }
        PlayGames.getLeaderboardsClient(activity)
            .loadCurrentPlayerLeaderboardScore(leaderboardId, timeSpan(span), LeaderboardVariant.COLLECTION_PUBLIC)
            .addOnCompleteListener { task ->
                val score = if (task.isSuccessful) task.result.get() else null
                if (!task.isSuccessful) Log.w(TAG, "player score failed: ${task.exception}")
                else Log.i(TAG, "player score on $leaderboardId: ${score?.rawScore ?: "none"}, rank ${score?.rank ?: "-"} (stale: ${task.result.isStale})")
                onResult.apply(if (score == null) Option.empty() else Option.apply(entry(score, isPlayer = true)))
            }
    }

    override fun loadTopScores(
        leaderboardId: String,
        span: LeaderboardSpan,
        maxResults: Int,
        onResult: Function1<Either<String, scala.collection.immutable.Seq<LeaderboardEntry>>, BoxedUnit>,
    ) {
        if (!signedIn) {
            onResult.apply(Left("not signed in"))
            return
        }
        PlayGames.getLeaderboardsClient(activity)
            .loadTopScores(leaderboardId, timeSpan(span), LeaderboardVariant.COLLECTION_PUBLIC, maxResults.coerceIn(1, 25))
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "top scores failed: ${task.exception}")
                    onResult.apply(Left("request failed"))
                    return@addOnCompleteListener
                }
                val scores = task.result.get()
                if (scores == null) {
                    onResult.apply(Right(scala.collection.immutable.`List$`.`MODULE$`.empty<LeaderboardEntry>()))
                    return@addOnCompleteListener
                }
                val rows = ArrayList<LeaderboardEntry>()
                try {
                    for (s in scores.scores) rows.add(entry(s, isPlayer = s.scoreHolder?.playerId == playerId))
                } finally {
                    scores.release()
                }
                Log.i(TAG, "top scores on $leaderboardId: ${rows.size} rows (stale: ${task.result.isStale})")
                onResult.apply(Right(scala.jdk.javaapi.CollectionConverters.asScala(rows).toList()))
            }
    }

    override fun showLeaderboard(leaderboardId: String, span: LeaderboardSpan): Boolean {
        if (!signedIn) return false
        activity.runOnUiThread {
            PlayGames.getLeaderboardsClient(activity)
                .getLeaderboardIntent(leaderboardId, timeSpan(span))
                .addOnSuccessListener { intent -> activity.startActivityForResult(intent, RC_LEADERBOARD) }
        }
        return true
    }

    private fun entry(s: LeaderboardScore, isPlayer: Boolean): LeaderboardEntry =
        LeaderboardEntry(s.rank, s.scoreHolderDisplayName, s.rawScore, isPlayer)

    private fun timeSpan(span: LeaderboardSpan): Int = when (span) {
        LeaderboardSpan.`Daily$`.`MODULE$` -> LeaderboardVariant.TIME_SPAN_DAILY
        LeaderboardSpan.`Weekly$`.`MODULE$` -> LeaderboardVariant.TIME_SPAN_WEEKLY
        else -> LeaderboardVariant.TIME_SPAN_ALL_TIME
    }

    companion object {
        private const val TAG = "SglPlayGames"
        const val RC_LEADERBOARD = 9002
    }
}

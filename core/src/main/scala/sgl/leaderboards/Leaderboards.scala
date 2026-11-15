package sgl
package leaderboards

/** Online leaderboards, such as Google Play Games Services.
  *
  * Games get an implementation from their platform wiring (for example
  * `sgl.android.play.AndroidPlayGamesLeaderboards` on Android) and fall back
  * to [[NoLeaderboards]] where the platform has none (desktop, web).
  *
  * All calls are asynchronous and never block the game loop. Callbacks may
  * run on another thread (the Android UI thread): a game should only store
  * their result and read it from its own update loop.
  *
  * Scores are plain longs, ranked by the order configured on the service
  * ("larger is better" or "smaller is better"); games that need several
  * criteria encode them into one number.
  */
abstract class AbstractLeaderboards {

  /** Whether this platform has online leaderboards at all. */
  def isAvailable: Boolean

  /** Whether the player is signed in to the service. */
  def isSignedIn: Boolean

  /** Starts the service, signing in silently a player who already agreed to it. Services that
    * start on their own ignore it; games that ask before using the service (for example before
    * sharing a first score) call it at launch only once the player has agreed. */
  def connect(): Unit = ()

  /** Asks the player to sign in (may show the service's UI). Calls back
    * with the new signed-in state. Only call it from an explicit player
    * action: services usually sign players in automatically at start. */
  def signIn(onResult: Boolean => Unit): Unit

  /** Submits a score. The service keeps a score only when it beats the
    * player's current one for each time span. Ignored when not signed in. */
  def submitScore(leaderboardId: String, score: Long): Unit

  /** Loads the signed-in player's own entry, None if they have no score in
    * this span (or are not signed in, or the request failed). */
  def loadPlayerScore(leaderboardId: String, span: LeaderboardSpan,
                      onResult: Option[LeaderboardEntry] => Unit): Unit

  /** Loads the best entries (up to `maxResults`, at most 25 on Play Games).
    * Calls back with Left(reason) when the request fails. */
  def loadTopScores(leaderboardId: String, span: LeaderboardSpan, maxResults: Int,
                    onResult: Either[String, Seq[LeaderboardEntry]] => Unit): Unit

  /** Opens the service's own leaderboard screen, if it has one. Returns
    * false when it cannot (not available, or not signed in). */
  def showLeaderboard(leaderboardId: String, span: LeaderboardSpan): Boolean
}

/** One row of a leaderboard. `rank` starts at 1. */
case class LeaderboardEntry(rank: Long, displayName: String, score: Long, isPlayer: Boolean)

/** The time window of a leaderboard. Play Games resets daily boards at
  * midnight UTC-7 and weekly boards on Saturday midnight UTC-7. */
sealed trait LeaderboardSpan
object LeaderboardSpan {
  case object Daily extends LeaderboardSpan
  case object Weekly extends LeaderboardSpan
  case object AllTime extends LeaderboardSpan
}

/** No online leaderboards: every request finds nothing. */
class NoLeaderboards extends AbstractLeaderboards {
  override def isAvailable: Boolean = false
  override def isSignedIn: Boolean = false
  override def signIn(onResult: Boolean => Unit): Unit = onResult(false)
  override def submitScore(leaderboardId: String, score: Long): Unit = ()
  override def loadPlayerScore(leaderboardId: String, span: LeaderboardSpan,
                               onResult: Option[LeaderboardEntry] => Unit): Unit = onResult(None)
  override def loadTopScores(leaderboardId: String, span: LeaderboardSpan, maxResults: Int,
                             onResult: Either[String, Seq[LeaderboardEntry]] => Unit): Unit =
    onResult(Left("leaderboards are not available on this platform"))
  override def showLeaderboard(leaderboardId: String, span: LeaderboardSpan): Boolean = false
}

/** In-memory leaderboards for development and tests: always signed in,
  * seeded with a few made-up players, ranked by `largerIsBetter`. Submitted
  * scores replace the player's own when better. */
class LocalLeaderboards(playerName: String = "You", largerIsBetter: String => Boolean = _ => true,
                        seed: Map[String, Seq[(String, Long)]] = Map.empty) extends AbstractLeaderboards {
  private val boards = scala.collection.mutable.Map.empty[String, Map[String, Long]] ++
    seed.map { case (id, rows) => id -> rows.toMap }
  override def isAvailable: Boolean = true
  override def isSignedIn: Boolean = true
  override def signIn(onResult: Boolean => Unit): Unit = onResult(true)
  override def submitScore(leaderboardId: String, score: Long): Unit = synchronized {
    val board = boards.getOrElse(leaderboardId, Map.empty)
    val better = board.get(playerName).forall(old => if(largerIsBetter(leaderboardId)) score > old else score < old)
    if(better) boards(leaderboardId) = board + (playerName -> score)
  }
  private def ranked(leaderboardId: String): Seq[LeaderboardEntry] = synchronized {
    val rows = boards.getOrElse(leaderboardId, Map.empty).toSeq
      .sortBy { case (_, s) => if(largerIsBetter(leaderboardId)) -s else s }
    rows.zipWithIndex.map { case ((name, s), i) => LeaderboardEntry(i + 1L, name, s, name == playerName) }
  }
  override def loadPlayerScore(leaderboardId: String, span: LeaderboardSpan,
                               onResult: Option[LeaderboardEntry] => Unit): Unit =
    onResult(ranked(leaderboardId).find(_.isPlayer))
  override def loadTopScores(leaderboardId: String, span: LeaderboardSpan, maxResults: Int,
                             onResult: Either[String, Seq[LeaderboardEntry]] => Unit): Unit =
    onResult(Right(ranked(leaderboardId).take(maxResults)))
  override def showLeaderboard(leaderboardId: String, span: LeaderboardSpan): Boolean = false
}

package sgl.achievements

/*
 * An achievement system should be provided locally and backend-agnostic.
 * Additionnaly, we should provide achievements abstraction in services,
 * with concrete implemenation to different services (such as google game
 * services)
 */
trait AchievementsComponent {

  //this abstract type can become concrete in different backend implementation.
  //typically, android would set it to the string name
  type AchievementId

  case class Achievement(id: AchievementId, name: String)

  def unlockAchievement(achievement: Achievement): Unit
}

trait LocalSaveAchievements extends AchievementsComponent {

  type AchievementId = Int

  //TODO
  //override def unlockAchievement(achievement: Achievement): Unit

}

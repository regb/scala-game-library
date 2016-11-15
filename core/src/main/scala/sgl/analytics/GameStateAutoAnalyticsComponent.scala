package sgl
package analytics

/** A GameStateComponent that automates analytics
  *
  * This extends default game state implementation with
  * an implementation that automatically tracks game screen
  * navigation.
  *
  * If you want to use it, make sure to mix it in AFTER mixin the
  * standard App trait, since that trait provides the default
  * GameStateComponent, and you want this one to override it.
  *
  * We offer this as a separate component, so that client can
  * choose to not use analytics (no dependency to analytics in
  * the default GameStateComponent) or can choose more
  * fine grained way to track game screens, if necessary.
  */
trait GameStateAutoAnalyticsComponent extends GameStateComponent {
  this: GraphicsProvider with AnalyticsProvider =>

  override val gameState: GameState = new GameStateAutoAnalytics

  class GameStateAutoAnalytics extends GameState {
    override def pushScreen(screen: GameScreen): Unit = {
      Analytics.logGameScreen(screen)
      super.pushScreen(screen)
    }
    override def newScreen(screen: GameScreen): Unit = {
      Analytics.logGameScreen(screen)
      super.newScreen(screen)
    }
  }

}

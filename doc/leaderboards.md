# Leaderboards

`sgl.leaderboards.AbstractLeaderboards` is a small shared API for online
leaderboards: availability, sign-in state, submitting a score, loading the
player's own entry and the top entries, and opening the service's own screen.

| Implementation | Where |
|---|---|
| `NoLeaderboards` | any platform without online leaderboards (desktop, web): never signed in, loads nothing |
| `LocalLeaderboards` | in memory, always signed in, optional seeded players: development and tests |
| `sgl.android.play.AndroidPlayGamesLeaderboards` | Android, Google Play Games Services v2 |

All calls are asynchronous. Callbacks may run on another thread (the Android
UI thread), so a game stores the results and reads them from its update loop.
Scores are longs ranked by the order configured on the service; encode several
criteria into one number when needed.

## Android setup

1. Create the Play Games Services project in Play Console and an Android OAuth
   credential for every certificate that signs installs you want signed in:
   the Play app signing key (store installs), and, for local testing, the
   upload or debug key.
2. Set `play_games_project_id` on `sgl_android_app`. The generated project then
   includes the `:sgl-android-play-games` module, the
   `com.google.android.gms.games.APP_ID` manifest metadata, and imports
   `AndroidPlayGamesLeaderboards` and `AndroidPlayGamesServices` in
   `MainActivity`.
3. Pass `AndroidPlayGamesLeaderboards(context)` to the game from
   `wiring_expression`, for example:

   ```python
   sgl_android_app(
       ...
       wiring_expression = "com.example.Wiring.wireAndroid(platformProxy, AndroidPlayGamesLeaderboards(context))",
       play_games_project_id = "123456789012",
   )
   ```

Play Games signs players in automatically at launch. Call `signIn` only from an
explicit player action (for example a SIGN IN button on a leaderboard screen).
While the Play Games project is unpublished, only its testers can sign in.

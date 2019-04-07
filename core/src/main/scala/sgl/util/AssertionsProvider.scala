
/*
 * This is a draft on an idea for an assertion module.
 *
 * An assertion provider should provide assert/require so
 * that we can use them whenever we assume that a condition should
 * always hold. Assertions are very useful concept for system with
 * complex state, such as games, because they let us clarify
 * our mind and catch errors early and thus simplify debugging.
 *
 * In release mode, there's an argument for not failing unless
 * we really have to, and maybe an unexpected state can still be
 * ok, which is why we would not want to crash on every assert even
 * if the state is corrupted. That should of course be balanced
 * with safety (for saved data) and there is always a risk of
 * corrupting player progress. My idea here is to provide an
 * assert and a fatalAssert, with the fatal would crash even
 * in a release mode, but the assert would not.
 *
 * The idea then is to have various possible implementations, the
 * default one would use built-in assert/requires, and just crash
 * with an exception. A release one would maybe do nothing. There
 * would be in-between solution, that would involve logging the
 * asserts but not crashing, or even better a crash report that
 * could be sent with all the asserts that failed.
 */

//trait AssertionsProvider {
//
//
//  trait Asserts {
//
//    def assert()
//
//    // crashes in all cases, even in release mode.
//    def fatal()
//
//  }
//  val Asserts: Asserts
//
//}

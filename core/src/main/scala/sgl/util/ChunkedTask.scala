package sgl.util

/** A ChunkedTask is an expensive computation that can be split in small chunks.
  *
  * A ChunkedTask is typically an expensive computation that one would
  * like to execute in a thread because it would take too much time to
  * fit on on game loop update call. The idea is to break such computations
  * into small units, and execute each unit as part of calls to run. This
  * abstraction provides a way to spread the computation over several
  * iteration of the game loop, each time exploiting some remaining time after
  * the main updates. It can be scheduled with the SchedulerProvider.
  */
abstract class ChunkedTask {

  import ChunkedTask._

  /*
   * There are two possible designs for an InterruptibleTask object.
   *   1) An execute(ms: Long) method that tell the task to execute for a given amount of ms milliseconds.
   *   2) A perform() method that tells the task to start executing and a pause() method that tells the task to stop.
   * However, I just realized that option 2) is kind of a no-go as it would require an external thread of control
   * to make the callback to stop, which obviously defeats the purpose of this task interface for platforms that
   * are singly-threaded (such as Javascript).
   */

  // TODO: Priority for scheduling?
  // val priority: Priority

  private var _status : Status = Pending
  def status: Status = _status

  /** Wrapper around the run method to be used internally.
    * 
    * Method called by the scheduler, it will maintain
    * the internal state of the task and invoke the run method.
    * The run method should not be called directly.
    */
  final private[sgl] def doRun(ms: Long): Unit = {
    _status = run(ms)
    assert(_status != Pending)
  }

  /** run this task for at most ms milliseconds.
    *
    * When the scheduler schedules a chunked task, the run
    * method will be invoked with some amount of milliseconds.
    * The way the scheduler decides how much time to give to a
    * task and which task to schedule is up to the exact implementation
    * of the scheduler.
    *
    * The task needs to respect the ms parameter and try as hard as
    * possible to stop when running out of time. It should also try
    * to maximize the use of this time as the game loop might
    * sleep for the remaining time after the return. The scheduler
    * will never enforce the ms constraint, as some implementation
    * might literally call the run method in the same thread of
    * execution (such as scalajs implementation for HTML5).
    *
    * Note that if the task does not return, it might block the whole
    * game loop and freeze the game. Although, if the platform provides
    * a threading system, it is also possible that ChunkedTask are run
    * in a thread and won't impact the main loop.
    *
    * There is no upper limit to the ms value, although most likely a
    * thread-based implementation is still going to pick some default
    * value and call run on the same task in an infinite loop.
    *
    * The method should return its status after this call to run, valid
    * values are either InProgress (if not completed) or Completed. The Pending
    * status is the default status when the task is still waiting to be
    * scheduled.
    */
  protected def run(ms: Long): Status

}

object ChunkedTask {

  /** Status of a task. */
  sealed abstract class Status
  /** The task is waiting to be scheduled. */
  case object Pending extends Status
  /** The task is not done and the scheduler should schedule it again. */
  case object InProgress extends Status
  /** The task is completed and the scheduler can disregard it. */
  case object Completed extends Status

}

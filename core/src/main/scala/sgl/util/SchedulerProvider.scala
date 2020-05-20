package sgl
package util

/** The provider for the platform-specific Scheduler.
  *
  * A scheduler should be unique per game and is platform
  * specific. This makes it a great candidate for being injected
  * by a Provider.
  */
trait SchedulerProvider {

  abstract class Scheduler {

    /** Register the task to be executed asynchronously.
      * 
      * The task will be executed when the scheduler has available resources,
      * the details will depend on the scheduler implementation.
      */
    def schedule(task: ChunkedTask): Unit

  }
  val Scheduler: Scheduler

}

/** This is a default implementation of the SchedulerProvider
  * that does not rely on any platform-specific features. It is able
  * to execute tasks by allocating CPU from the main game loop
  * when invoked by the run() method.
  */
trait SingleThreadSchedulerProvider extends SchedulerProvider {
  this: LoggingProvider with SystemProvider =>

  import scala.collection.mutable.Queue

  private implicit val Tag = Logger.Tag("single-thread-scheduler")

  // For obvious reasons, this Scheduler is not thread-safe. It should
  // always be called from the game loop thread and never from another
  // place.
  // This scheduler is also simple and fair, just giving up-to 5ms to each
  // of the task in the queue, in order. More advanced implementation of the
  // Scheduler interface could take into account priorities or more advanced
  // things.
  class SingleThreadScheduler extends Scheduler {

    // TODO: this is actually not very fair, as if run is called with a non-multiple
    //       of 5ms, the last task to be scheduled will only get the remainder (<5) and
    //       then be re-enqueued at the end of the queue.

    private val taskQueue: Queue[ChunkedTask] = new Queue

    override def schedule(task: ChunkedTask): Unit = {
      taskQueue.enqueue(task)
    }

    /** Give control to the scheduler to allocate
      * CPU to the pending tasks. The scheduler returns
      * either after the ms amount of time, or as soon
      * as no more work is required.
      *
      * Return true if the task queue is empty.
      */
    def run(ms: Long): Boolean = {
      logger.trace("Running SingleThreadScheduler with taskQueue size of: " + taskQueue.size)
      val endTime = System.nanoTime + ms*1000l*1000l
      var remaining = endTime - System.nanoTime
      while(remaining > 0 && taskQueue.nonEmpty) {
        val available = (remaining/(1000l*1000l)) min 5
        val task = taskQueue.dequeue()
        task.doRun(available) 
        if(task.status == ChunkedTask.InProgress)
          taskQueue.enqueue(task)
        remaining = endTime - System.nanoTime
      }
      taskQueue.isEmpty
    }

  }
  override val Scheduler = new SingleThreadScheduler
}

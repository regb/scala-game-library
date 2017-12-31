package sgl.util

import java.util.concurrent.Executors
import scala.collection.mutable.Queue

trait ThreadPoolSchedulerProvider extends SchedulerProvider {
  this: LoggingProvider =>

  private implicit val Tag = Logger.Tag("threadpool-scheduler")

  class ThreadPoolScheduler extends Scheduler {
    private val pool = Executors.newFixedThreadPool(4)

    private val tasks: Queue[ChunkedTask] = new Queue
    private val taskQueueLock = new Object

    private var r1: ChunksRunner = null
    private var r2: ChunksRunner = null
    private var r3: ChunksRunner = null
    private var r4: ChunksRunner = null

    override def schedule(task: ChunkedTask): Unit = {
      taskQueueLock.synchronized {
        tasks.enqueue(task)
      }
    }

    /** Resume the execution of all scheduled task.
      *
      * This method should always be called after creating the
      * Scheduler. This helps in maintaining the symetry with pause().
      */
    def resume(): Unit = {
      r1 = new ChunksRunner
      r2 = new ChunksRunner
      r3 = new ChunksRunner
      r4 = new ChunksRunner
      pool.submit(r1)
      pool.submit(r2)
      pool.submit(r3)
      pool.submit(r4)
    }

    /** Pause the execution of all scheduled task.
      *
      * This will prevent all worker threads from doing work but
      * will not release them.
      */
    def pause(): Unit = {
      r1.shouldStop = true
      r2.shouldStop = true
      r3.shouldStop = true
      r4.shouldStop = true
    }

    /** The Scheduler will stop executing all scheduled task
      * and it will clean-up all platform-specific resources
      * (for example, running worker threads).
      */
    def shutdown(): Unit = {
      pool.shutdown()
      r1.shouldStop = true
      r2.shouldStop = true
      r3.shouldStop = true
      r4.shouldStop = true
    }

    // Simple Runnable class that picks up the first available ChunkedTask and
    // run one chunk of it.
    // Note that if there is only one ChunkedTask in the queue, there will only
    // be one busy Thread at a time as ChunkedTask are assumed to be sequentials.
    // In order to optimize the use of the thread pool, one should try to split
    // parallel work into several independent ChunkedTask.
    class ChunksRunner extends Runnable {
      var shouldStop = false
      override def run(): Unit = {
        while(!shouldStop) {
          val task = taskQueueLock.synchronized {
            if(tasks.isEmpty) {
              None
            } else {
              Some(tasks.dequeue())
            }
          }
          task match {
            case None => Thread.sleep(50)
            case Some(task) => {
              logger.debug("Executing some ChunkedTask from the task queue.")
              task.doRun(5l)
              if(task.status != ChunkedTask.Completed)
                taskQueueLock.synchronized { tasks.enqueue(task) }
            }
          }
        }
      }
    }
  }
  override val Scheduler = new ThreadPoolScheduler

}

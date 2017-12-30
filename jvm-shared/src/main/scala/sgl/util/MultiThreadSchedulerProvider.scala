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

    val r1 = new RunnableChunks
    val r2 = new RunnableChunks
    val r3 = new RunnableChunks
    val r4 = new RunnableChunks
    pool.submit(r1)
    pool.submit(r2)
    pool.submit(r3)
    pool.submit(r4)

    override def schedule(task: ChunkedTask): Unit = {
      taskQueueLock.synchronized {
        tasks.enqueue(task)
      }
    }

    override def run(ms: Long): Unit = {
      logger.trace("Current task queue size: " + tasks.size)
    }

    override def shutdown(): Unit = {
      pool.shutdown()
      r1.shouldStop = true
      r2.shouldStop = true
      r3.shouldStop = true
      r4.shouldStop = true
    }

    class RunnableChunks extends Runnable {
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

package sgl
package proxy

import sgl.util._

trait ProxySchedulerProvider extends SchedulerProvider {

  val PlatformProxy: PlatformProxy

  class ProxyScheduler extends Scheduler {
    override def schedule(task: ChunkedTask): Unit = PlatformProxy.schedulerProxy.schedule(task)
  }
  override val Scheduler: Scheduler = new ProxyScheduler

}

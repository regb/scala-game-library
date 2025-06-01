package sgl.android

import sgl.proxy.SchedulerProxy
import sgl.util.ChunkedTask

// TODO: implement this
class AndroidSchedulerProxy: SchedulerProxy {
    override fun schedule(task: ChunkedTask?) {
        println("scheduling")
    }

}
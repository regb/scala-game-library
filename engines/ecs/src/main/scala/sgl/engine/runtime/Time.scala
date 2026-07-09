package sgl.engine.runtime

import sgl.engine.ecs.Resource

final case class DeltaTime(seconds: Double)
object DeltaTime {
  given Resource[DeltaTime] = Resource("sgl.engine.runtime.DeltaTime")
}

final case class ElapsedTime(seconds: Double)
object ElapsedTime {
  given Resource[ElapsedTime] = Resource("sgl.engine.runtime.ElapsedTime")
}

final case class FixedDeltaTime(seconds: Double)
object FixedDeltaTime {
  given Resource[FixedDeltaTime] = Resource("sgl.engine.runtime.FixedDeltaTime")
}

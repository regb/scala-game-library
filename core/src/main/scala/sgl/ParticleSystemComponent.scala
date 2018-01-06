package sgl

trait ParticleSystemComponent {
  this: GraphicsProvider =>

  case class ParticleSystemConfig(
    // Duration during which the particle system will spawn particles.
    duration: Long,
    // If loop is true, the system reset to the starting age after its duration and restart.
    loop: Boolean,
    // number of particles emitted per seconds
    spawnRate: Int,
    // List of bursts of particles to create at a given point in the lifetime of the particle system.
    spawnBursts: List[(Long, Int)],
    // Base spawn direction, in radians.
    spawnDirectionBase: Double,
    // The variation for the angle direction in radians.
    // With math.Pi, the variation goes from -pi to +pi, which
    // means 360 degrees.
    spawnDirectionVariation: Double,
    //var spawnDirectionVariation = math.Pi
    minParticleVelocity: Double,
    maxParticleVelocity: Double,
    maxParticleSize: Int,
    minParticleSize: Int,
    maxParticleAge: Int,
    minParticleAge: Int,
    particleKeyColors: Array[(Int, Int, Int, Int)],
    particleAccelerationX: Double,
    particleAccelerationY: Double,
    /*
     * If None, the rendering does not guarantee any consistent order
     * for rendering. None is potentially more efficient as the renderer
     * can just render in any order convenient.
     */
    renderingSortMode: Option[ParticleSystemConfig.RenderingSortMode]
  )

  object ParticleSystemConfig {
    /*
     * In which order are the particles rendered?
     * YoungestFirst means that we first render the
     * youngest particles, hence the oldest particles
     * will appear on top of the new ones.
     */
    sealed trait RenderingSortMode
    case object YoungestFirst extends RenderingSortMode
    case object OldestFirst extends RenderingSortMode
  }

  class ParticleSystem(var config: ParticleSystemConfig) {

    import scala.collection.mutable.ArrayBuffer

    var x = 100
    var y = 200

    // java.util.Random seems to be supported by scalajs, scala-native, and is useable on Android.
    // We will keep the implementation simple and based on that for now, but apparently
    // the quality of random numbers is not great. We should eventually revisit if we need
    // a custom implementation for games, but at the very least it seems like we should extract
    // this into some sort of Provider with the possibility to choose how to seed it. We could
    // have a global RandomProvider, with a Random interface, which can be seeded. Although, it's not
    // too clear how useful that is for games.
    private val random = new java.util.Random()

    // Returns random value between min and max, both inclusive.
    def random(min: Double, max: Double): Double = {
      val diff = max - min
      min + random.nextDouble()*diff
    }

    // Returns random value between min and max, both inclusive.
    def random(min: Int, max: Int): Int = {
      val diff = max - min
      min + (random.nextDouble()*diff).toInt
    }

    // We choose a random velocity within these constraints.
    private def setStartingVelocity(p: Particle): Unit = {
      val variation = random(-config.spawnDirectionVariation, config.spawnDirectionVariation)
      val direction = config.spawnDirectionBase + variation
      p.vx = math.cos(direction) * random(config.minParticleVelocity, config.maxParticleVelocity)
      p.vy = math.sin(direction) * random(config.minParticleVelocity, config.maxParticleVelocity)
    }

    private var age = 0l

    private var spawnCarry = 0f
    private var nextSpawnBurst = config.spawnBursts

    private val particles = new ArrayBuffer[Particle]
    reset()

    private val particlePool = new ArrayBuffer[Particle]

    def reset(): Unit = {
      for(p <- particles)
        particlePool.append(p)
      particles.clear()
      age = 0
      spawnCarry = 0
      nextSpawnBurst = config.spawnBursts
    }
    
    def update(dt: Long): Unit = {
      age += dt

      val spawnRateMs: Float = config.spawnRate/1000f
      val spawnAmount = (spawnRateMs*dt + spawnCarry)
      val particlesToSpawn = spawnAmount.toInt
      spawnCarry = spawnAmount - particlesToSpawn

      val totalParticlesToSpawn = if(nextSpawnBurst.headOption.exists(age >= _._1)) {
        val n = nextSpawnBurst.head._2
        nextSpawnBurst = nextSpawnBurst.tail
        n
      } else particlesToSpawn

      for(i <- 1 to totalParticlesToSpawn) {
        val particle = if(particlePool.isEmpty) {
          new Particle
        } else {
          val p = particlePool.last
          p.reset()
          particlePool.remove(particlePool.size-1)
          p
        }
        setStartingVelocity(particle)
        particle.radius = random(config.minParticleSize, config.maxParticleSize)
        particle.maxAge = random(config.minParticleAge, config.maxParticleAge)
        particle.ax = config.particleAccelerationX
        particle.ay = config.particleAccelerationY
        particle.keyColors = config.particleKeyColors
        particles.append(particle)
      }

      particles.foreach(p => p.update(dt))

      // TODO: use an iterator and merge to the update iteration
      for(i <- particles.size-1 to 0 by -1) {
        if(particles(i).isDead) {
          particlePool.append(particles(i))
          particles.remove(i)
        }
      }
    }

    def render(canvas: Graphics.Canvas): Unit = {
      canvas.translate(x, y)
      config.renderingSortMode match {
        case None | Some(ParticleSystemConfig.OldestFirst) =>
          particles.foreach(p => p.render(canvas))
        case Some(ParticleSystemConfig.YoungestFirst) =>
          for(i <- particles.size-1 to 0 by -1)
            particles(i).render(canvas)
      }
      canvas.translate(-x, -y)
    }

  }

  class Particle {

    // position relative to the particle system
    private var x = 0d
    private var y = 0d

    // velocity is in dp per seconds
    var vx = 0d
    var vy = 0d

    var ax = 0d
    var ay = 0d

    var radius = 0

    var maxAge = 0l

    var age = 0l

    var keyColors: Array[(Int, Int, Int, Int)] = null

    def colorInterpolation(age: Long): Graphics.Color = {
      val progress = (age/maxAge.toDouble)

      val fromIndex = (keyColors.size-1) min (progress*(keyColors.size-1)).toInt

      val fromKey = keyColors(fromIndex)
      //val toKey = keyColors((keyColors.size-1) min (progress*keyColors.size + 1).toInt)
      val toKey = keyColors(fromIndex+1)

      // The current progress [0, 1] in the intermediate key colors
      val keyProgress: Double = progress*(keyColors.size-1) - (progress*(keyColors.size-1)).toInt

      Graphics.Color.rgba(fromKey._1 + (keyProgress*(toKey._1 - fromKey._1)).toInt,
                          fromKey._2 + (keyProgress*(toKey._2 - fromKey._2)).toInt,
                          fromKey._3 + (keyProgress*(toKey._3 - fromKey._3)).toInt,
                          fromKey._4 + (keyProgress*(toKey._4 - fromKey._4)).toInt)
    }

    // Reset the particle to make it ready to be reused.
    def reset(): Unit = {
      age = 0
      x = 0
      y = 0
    }

    def isDead: Boolean = age >= maxAge

    def update(dt: Long): Unit = {
      age += dt

      x += vx*(dt/1000d)
      y += vy*(dt/1000d)

      vx += ax*(dt/1000d)
      vy += ay*(dt/1000d)
    }

    def render(canvas: Graphics.Canvas): Unit = {
      if(!isDead) {
        canvas.drawCircle(x.toInt, y.toInt, radius, Graphics.defaultPaint.withColor(colorInterpolation(age)))
        //canvas.drawCircle(x.toInt, y.toInt, radius, defaultPaint.withColor(Color.rgba(255, ((1 - age/maxAge.toDouble)*255).toInt, 0, ((1- (age/maxAge.toDouble))*255).toInt)))
        //canvas.drawRect(position.x.toInt, position.y.toInt, 1, 1, defaultPaint.withColor(Color.Red))
      }
    }

  }

  def fireParticleConfig = ParticleSystemConfig(
    // Duration during which the particle system will spawn particles.
    duration = 5000,
    // If loop is true, the system reset to the starting age after its duration and restart.
    loop = true,
    // number of particles emitted per seconds
    spawnRate = 280,
    // List of bursts of particles to create at a given point in the lifetime of the particle system.
    spawnBursts = List(),
    // Base spawn direction, in radians.
    spawnDirectionBase = -math.Pi/2,
    // The variation for the angle direction in radians.
    // With math.Pi, the variation goes from -pi to +pi, which
    // means 360 degrees.
    spawnDirectionVariation = math.Pi/8,
    //var spawnDirectionVariation = math.Pi
    minParticleVelocity = 30,
    maxParticleVelocity = 50,
    maxParticleSize = 3,
    minParticleSize = 3,
    maxParticleAge = 1500,
    minParticleAge = 1000,
    particleAccelerationX = 0,
    particleAccelerationY = 0,
    particleKeyColors = Array((255, 255, 0, 255),
                              (255, 110, 0, 255),
                              (255,   0, 0,   0)),
    renderingSortMode = None
  )

  def dustParticleConfig = ParticleSystemConfig(
    // Duration during which the particle system will spawn particles.
    duration = 5000,
    // If loop is true, the system reset to the starting age after its duration and restart.
    loop = false,
    // number of particles emitted per seconds
    spawnRate = 0,
    // List of bursts of particles to create at a given point in the lifetime of the particle system.
    spawnBursts = List((0, 300)),
    // Base spawn direction, in radians.
    spawnDirectionBase = -math.Pi/2,
    // The variation for the angle direction in radians.
    // With math.Pi, the variation goes from -pi to +pi, which
    // means 360 degrees.
    spawnDirectionVariation = math.Pi/3,
    //var spawnDirectionVariation = math.Pi
    minParticleVelocity = 3,
    maxParticleVelocity = 10,
    maxParticleSize = 2,
    minParticleSize = 2,
    maxParticleAge = 1000,
    minParticleAge = 500,
    particleAccelerationX = 0,
    particleAccelerationY = 5,
    particleKeyColors = Array((210, 210, 210, 255),
                              (210, 210, 210,   0)),
    renderingSortMode = None
  )
}

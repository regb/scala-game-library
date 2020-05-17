package sgl.util

import org.scalatest.funsuite.AnyFunSuite

trait RandomProviderAbstractSuite extends AnyFunSuite with RandomProvider {

  test("Two instances from same seed produces same stream of random data") {
    val r1 = Random.fromSeed(77)
    val r2 = Random.fromSeed(77)
    assert(r1.nextInt() === r2.nextInt())
    assert(r1.nextInt() === r2.nextInt())
    assert(r1.nextLong() === r2.nextLong())
  }

  test("A Random instance that reset the seed reproduces the same stream of random data") {
    val r = Random.fromSeed(12)
    val n1 = r.nextInt()
    val n2 = r.nextInt()
    val n3 = r.nextLong()
    r.setSeed(12)
    assert(n1 === r.nextInt())
    assert(n2 === r.nextInt())
    assert(n3 === r.nextLong())
  }

}

class DefaultRandomProviderSuite extends RandomProviderAbstractSuite with DefaultRandomProvider

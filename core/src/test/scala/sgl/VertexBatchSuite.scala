package sgl

import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable

class VertexBatchSuite extends AnyFunSuite {
  private def shape(batch: VertexBatch[String], texture: Option[String]): Unit = {
    batch.begin(texture, 3)
    for(_ <- 0 until 3) batch.values(batch.next()) = batch.slot
  }
  private def drawn(batch: VertexBatch[String]): Vector[String] = {
    val log = mutable.ArrayBuffer.empty[String]
    val used = batch.foreachRun((unit, texture) => log += s"$texture on $unit")((first, count) => log += s"$first+$count")
    (log :+ s"$used units").toVector
  }

  test("shapes share a draw call until it has no texture unit left") {
    val batch = new VertexBatch[String](floatsPerVertex = 1, slots = 2)
    Vector(None, Some("a"), Some("b"), Some("a"), Some("c")).foreach(shape(batch, _))
    assert(drawn(batch) == Vector("a on 0", "b on 1", "0+12", "c on 0", "12+3", "2 units"))
    assert(batch.values.take(15).toVector == Vector(0f, 1f, 2f, 1f, 1f).flatMap(Vector.fill(3)(_)))
  }

  test("what is done in between splits draw calls, in order, and shapes pick up their textures again") {
    val batch = new VertexBatch[String](floatsPerVertex = 1, slots = 4)
    val log = mutable.ArrayBuffer.empty[String]
    batch.between(() => log += "before")
    shape(batch, Some("a"))
    batch.between(() => log += "first")
    batch.between(() => log += "second")
    shape(batch, Some("a"))
    shape(batch, None)
    batch.between(() => log += "after")
    batch.foreachRun((unit, texture) => log += s"$texture on $unit")((first, count) => log += s"$first+$count")
    assert(log.toVector == Vector("before", "a on 0", "0+3", "first", "second", "a on 0", "3+6", "after"))
    assert(!batch.isEmpty)

    batch.clear()
    assert(batch.isEmpty && drawn(batch) == Vector("0 units"))
    batch.between(() => log += "alone")
    assert(!batch.isEmpty)
    val _ = drawn(batch)
    assert(log.last == "alone")
  }
}

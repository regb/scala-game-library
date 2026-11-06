package sgl.dev

import scala.collection.mutable
import scala.util.Try

/** Text conversion for a development value. Custom codecs must return errors, not throw. */
trait ConfigCodec[A] {
  def parse(text: String): Either[String, A]
}

object ConfigCodec {
  val string: ConfigCodec[String] = new ConfigCodec[String] {
    def parse(text: String): Either[String, String] = Right(text)
  }
  val int: ConfigCodec[Int] = new ConfigCodec[Int] {
    def parse(text: String): Either[String, Int] =
      Try(text.trim.toInt).toEither.left.map(_ => "expected an integer")
  }
  val double: ConfigCodec[Double] = new ConfigCodec[Double] {
    def parse(text: String): Either[String, Double] =
      Try(text.trim.toDouble).toEither.left.map(_ => "expected a number").flatMap { value =>
        if(value.isNaN || value.isInfinity) Left("expected a finite number") else Right(value)
      }
  }
  val boolean: ConfigCodec[Boolean] = new ConfigCodec[Boolean] {
    def parse(text: String): Either[String, Boolean] = text.trim match {
      case "true" => Right(true)
      case "false" => Right(false)
      case _ => Left("expected true or false")
    }
  }
}

/** Single-threaded registry. Register, read, update and unsubscribe on the game thread. */
final class LiveConfig {
  private case class Change(commit: () => Unit, notifyListeners: () => Unit)
  private val entries = mutable.LinkedHashMap.empty[String, String => Either[String, Change]]
  private var updating = false

  final class Value[A] private[LiveConfig] (
      val name: String,
      val default: A,
      codec: ConfigCodec[A],
      validate: A => Boolean
  ) {
    private var current = default
    private var nextListenerId = 0L
    private val listeners = mutable.LinkedHashMap.empty[Long, A => Unit]

    def get: A = current

    /** Does not fire immediately. The returned function removes this subscription. */
    def onChange(listener: A => Unit): () => Unit = {
      val id = nextListenerId
      nextListenerId += 1
      listeners(id) = listener
      () => { listeners.remove(id); () }
    }

    private def check(value: A): Either[String, A] =
      if(validate(value)) Right(value) else Left("value failed validation")

    private def change(value: A): Change = {
      val changed = current != value
      Change(
        () => { current = value },
        () => { if(changed) listeners.values.toVector.foreach(_(value)) }
      )
    }

    private[LiveConfig] def prepare(text: String): Either[String, Change] =
      codec.parse(text).flatMap(check).map(change)

    def set(value: A): Either[String, Unit] = {
      require(!updating, "cannot update values from a change callback")
      check(value).map { checked => applyChanges(Vector(change(checked))) }
    }

    def reset(): Unit = { set(default); () }
  }

  def register[A](name: String, default: A, codec: ConfigCodec[A],
      validate: A => Boolean = (_: A) => true): Value[A] = {
    require(!updating, "cannot register values from a change callback")
    require(name.nonEmpty && !entries.contains(name), "value name must be nonempty and unique")
    require(validate(default), "default value failed validation")
    val value = new Value(name, default, codec, validate)
    entries(name) = value.prepare
    value
  }

  /** Validates the entire patch before changing anything. Missing keys are unchanged. */
  def update(patch: Map[String, String]): Either[Vector[String], Unit] = {
    require(!updating, "cannot update values from a change callback")
    val prepared = patch.toVector.sortBy(_._1).map { case (name, text) =>
      entries.get(name).toRight("unknown value").flatMap(_(text))
        .left.map(error => s"$name: $error")
    }
    val errors = prepared.collect { case Left(error) => error }
    if(errors.nonEmpty) Left(errors)
    else {
      applyChanges(prepared.collect { case Right(change) => change })
      Right(())
    }
  }

  private def applyChanges(changes: Vector[Change]): Unit = {
    updating = true
    try {
      changes.foreach(_.commit())
      changes.foreach(_.notifyListeners())
    } finally updating = false
  }
}

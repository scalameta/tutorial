package scalaworld.macros

object autocomplete {
  def meta[T](thunk: => T): T = thunk

  val inline = 1
}

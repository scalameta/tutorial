package scalaworld.macros

object DebugExample {
  @Debug
  def complicated(a: Int, b: String)(c: Int): Int = {
    Thread.sleep(500)
    a + b.length + c
  }
}

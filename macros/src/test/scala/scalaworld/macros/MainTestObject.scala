package scalaworld.macros

object MainTestObject {

  def complicated(arg: Int): Int = {
    println(arg)
    val start = System.nanoTime()
    val result = {
      arg + arg
    }
    val elapsed =
      _root_.java.util.concurrent.TimeUnit.MILLISECONDS
        .convert(
          System.nanoTime() - start,
          _root_.java.util.concurrent.TimeUnit.NANOSECONDS)
    println(
      "Method " + "complicated" + " ran in " + elapsed + "ms")
    result
  }

  def main(args: Array[String]): Unit = {
    complicated(4)
  }
}

package scalaworld.macros

import scala.annotation.compileTimeOnly
import scala.meta._

@compileTimeOnly("@scalaworld.Debug not expanded")
class Debug extends scala.annotation.StaticAnnotation {
  import autocomplete._
  def apply(defn: Defn): Any = meta {
    defn match {
      case q"..$mods def $name[..$tparams](...$paramss): $tpeopt = $expr" =>
        val body =
          q"""
                  {
                    val start = System.nanoTime()
                    val result = $expr
                    val elapsed =
                      _root_.java.util.concurrent.TimeUnit.MILLISECONDS.convert(
                      System.nanoTime() - start,
                      _root_.java.util.concurrent.TimeUnit.NANOSECONDS
                    )
                    println("Method " + ${name.syntax} + " ran in " + elapsed + "ms")
                    result
                  }
                  """
        q"..$mods def $name[..$tparams](...$paramss): $tpeopt = $body"
      case _ => defn
    }
  }
}

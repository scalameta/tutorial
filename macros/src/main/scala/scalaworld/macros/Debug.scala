package scalaworld.macros

import scala.annotation.compileTimeOnly
import scala.meta._

@compileTimeOnly("@scalaworld.Debug not expanded")
class Debug extends scala.annotation.StaticAnnotation {
  def meta[T](thunk: => T): T = thunk

  inline def apply(defn: Any): Any = meta {
    defn match {
      case q"..$mods def $name[..$tparams](...$paramss): $tpeopt = $expr" =>
        val args = paramss.flatten.map(x => x.name.syntax.parse[Term].get)
        val body = q"""
                  {
                    println(..$args)

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

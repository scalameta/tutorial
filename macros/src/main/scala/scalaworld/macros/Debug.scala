package scalaworld.macros

import scala.annotation.compileTimeOnly
import scala.meta._

// Before:
// @Debug
// def complicated(a: Int, b: String)(c: Int): Int = {
//   Thread.sleep(500)
//   a + b.length + c
// }
// After:
// def complicated(a: Int, b: String)(c: Int): Int = {
//   {
//     println("a" + ": " + a)
//     println("b" + ": " + b)
//     println("c" + ": " + c)
//   }
//   val start = System.currentTimeMillis()
//   val result = {
//     Thread.sleep(500)
//     a + b.length + c
//   }
//   val elapsed = System.currentTimeMillis() - start
//   println("Method " + "complicated" + " ran in " + elapsed + "ms")
//   result
// }
class Debug extends scala.annotation.StaticAnnotation {
  import autocomplete._
  def apply(defn: Defn): Any = meta {
    defn match {
      case defn: Defn.Def =>
        val printlnStatements = defn.paramss.flatten.map(param =>
          q"""println(${param.name.syntax} + ": " + ${Term.Name(
            param.name.value)})""")
        val body: Term = q"""
          { ..$printlnStatements }
          val start = System.currentTimeMillis()
          val result = ${defn.body}
          val elapsed = System.currentTimeMillis() - start
          println("Method " + ${defn.name.syntax} + " ran in " + elapsed + "ms")
          result
          """
        defn.copy(body = body)
      case _ =>
        abort("@Debug most annotate a def")
    }
  }
}

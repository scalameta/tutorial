package scalaworld.macros

import scala.meta._

class Main extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case q"object $name { ..$stats }" =>
        val main = q"def main(args: Array[String]): Unit = { ..$stats }"
        q"object $name { $main }"
      case _ =>
        abort("@main must annotate an object.")
    }
  }
}


package scalafix.macros

import scala.annotation.compileTimeOnly
import scala.meta._

@compileTimeOnly("@nz.daved.inlinemacros.Main not expanded")
class Main extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"object $name { ..$stats }" = defn
    val main = q"def main(args: Array[String]): Unit = { ..$stats }"
    q"object $name { $main }"
  }
}

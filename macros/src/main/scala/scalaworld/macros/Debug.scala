package scalaworld.macros

import scala.annotation.compileTimeOnly
import scala.meta._

@compileTimeOnly("@scalaworld.Debug not expanded")
class Debug extends scala.annotation.StaticAnnotation {
  import autocomplete._
  inline; def apply(defn: Defn): Any = meta {
    ???
  }
}

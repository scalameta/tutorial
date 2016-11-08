package scalaworld.macros

import scala.meta._

object MacroUtil {
  def helper(defn: Any): Stat = q"class ReuseExample"
}

class Reuse extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    MacroUtil.helper(defn)
  }
}

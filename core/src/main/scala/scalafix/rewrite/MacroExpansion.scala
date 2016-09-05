package scalafix.rewrite

import scala.meta._
import scalafix.FixResult
import scalafix.util.logger

object MacroExpansion extends Rewrite {
  val annotations = mod"@main"
  def mainExpand(defn: Defn): Tree = defn match {
    case q"..$mods object $name { ..$stats }" =>
      val main = q"def main(args: Seq[String]): Unit = { ..$stats }"
      q"object $name { $main }"
    case _ => defn
  }

  def classExpand(defn: Defn.Class): Tree = {
    logger.elem(defn.ctor.paramss)
    defn
  }

  override def rewrite(code: Input): FixResult = {
    withParsed(code) { ast =>
      val result = ast.transform {
        case d: Defn.Class =>
          logger.elem(d)
          classExpand(d)
        case d: Defn =>
          mainExpand(d)
      }
      FixResult.Success(result.syntax)
    }
  }
}

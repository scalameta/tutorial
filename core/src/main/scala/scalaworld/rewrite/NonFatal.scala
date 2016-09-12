package scalaworld.rewrite

import scala.meta._
import scalaworld.Fixed
import scalaworld.util.logger

case class Patch(from: Int, to: Int, replace: String) {
  def runOn(str: String): String = {
    str.substring(0, from) + replace + str.substring(to)
  }
}

object Patch {
  def run(input: String, patches: Seq[Patch]): String = {
    patches.foldLeft(input) {
      case (s, p) => p.runOn(s)
    }
  }
}

/**
  * Rewrite this
  *   catch {
  *     case e: Throwable => ...
  *   }
  *
  * into this
  *   catch {
  *     case NonFatal(e) => ...
  *   }
  *
  */
object NonFatal extends Rewrite {
  override def rewrite(code: Input): Fixed = withParsed(code) { tree =>
    val throwables = tree.collect {
      case t @( p"case ($name: Throwable) => $expr" )=>
        val c = t.asInstanceOf[Case].pat.tokens
        val nonFatal = q"NonFatal(${name.children.head.asInstanceOf[Term.Name]})"
        Patch(c.head.start, c.last.end, nonFatal.syntax)
    }
    val result = Patch.run(new String(code.chars), throwables)
    // Your implementation here
    Fixed.Success(result)
  }
}

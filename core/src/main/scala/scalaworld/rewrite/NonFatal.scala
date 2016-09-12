package scalaworld.rewrite

import scala.meta._
import scalaworld.Fixed

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
      case t @ (p"case ($name: Throwable) => $expr") =>
        val c = t.asInstanceOf[Case].pat.tokens
        val nonFatal =
          q"NonFatal(${name.children.head.asInstanceOf[Term.Name]})"
        Patch(c.head, c.last, nonFatal.syntax)
    }
    val result = Patch.run(tree.tokens, throwables)
    // Your implementation here
    Fixed.Success(result)
  }
}

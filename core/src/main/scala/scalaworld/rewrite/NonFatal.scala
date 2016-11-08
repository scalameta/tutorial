package scalaworld.rewrite

import scala.meta._
import scalaworld.Fixed
import scalaworld.util.logger

/**
  * Rewrite this
  * {{{
  *   catch {
  *     case e: Throwable => ...
  *   }
  * }}}
  *
  * into this
  * {{{
  *   catch {
  *     case NonFatal(e) => ...
  *   }
  * }}}
  *
  */
object NonFatal extends Rewrite {
  override def rewrite(code: Input): Fixed =
    withParsed(code) { tree =>
      val patches = tree.collect {
        case c @ p"case $name: Throwable => $expr" =>
          val pat = c.asInstanceOf[Case].pat
          Patch(pat.tokens.head, pat.tokens.last, s"NonFatal(${name.syntax})")
      }
      Fixed.Success(Patch.run(tree.tokens, patches))
    }
}

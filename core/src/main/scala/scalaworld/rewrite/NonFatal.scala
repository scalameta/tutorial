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
    // Your implementation here
    Fixed.Success(???)
  }
}

package scalaworld.rewrite

import scala.meta._
import scalaworld.Fixed

object NonFatal extends Rewrite {
  override def rewrite(code: Input): Fixed = {
    withParsed(code) { ast =>
      // Your implementation here
      Fixed.Success(???)
    }
  }
}

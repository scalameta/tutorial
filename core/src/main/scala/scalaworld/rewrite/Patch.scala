package scalaworld.rewrite

import scala.meta._
import scala.meta.tokens.Token

case class Patch(from: Token,
                 to: Token,
                 replace: String) {
  def insideRange(
      token: Token): Boolean =
    token.start >= from.start && token.end <= to.end
  val tokens =
    replace.tokenize.get.tokens.toSeq
  def runOn(
      str: Seq[Token]): Seq[Token] = {
    str.flatMap {
      case `from`              => tokens
      case x if insideRange(x) => Nil
      case x                   => Seq(x)
    }
  }
}

object Patch {
  def run(
      input: Seq[Token],
      patches: Seq[Patch]): String = {
    patches
      .foldLeft(input) {
        case (s, p) => p.runOn(s)
      }
      .map(_.syntax)
      .mkString("")
  }
}

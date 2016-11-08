package scalaworld.workshop

import scalaworld.util.logger
import scala.meta._

import org.scalatest.FunSuite
import scala.meta.tokens.Token._
import scalaworld.util.DiffAssertions

class TrailingCommaSuite extends FunSuite with DiffAssertions {

  /** Removes all commas behind the last argument of function calls */
  def stripTrailingCommas(tokens: Tokens): String = {
    tokens.zipWithIndex.filter {
      case (c @ Comma(), i) =>
        tokens
          .drop(i + 1)
          .find {
            case LF() | Token.Comment(_) | Space() => false
            case _                                 => true
          }
          .exists {
            case RightParen() => false
            case x =>
              logger.elem(x.structure, x.getClass)
              true
          }
      case _ =>
        true
    }.map(_._1.syntax).mkString("")
  }

  def check(original: String, expected: String): Unit = {
    test(logger.reveal(original)) {
      val obtained = stripTrailingCommas(original.tokenize.get)
      assertNoDiff(obtained.trim, expected.trim)
    }
  }

  check(
    """|function(
       | arg1,
       | arg2,
       |)""".stripMargin,
    """|function(
       | arg1,
       | arg2
       |)""".stripMargin
  )

  check(
    """|function(
       |  arg1,
       |// arg2,
       |)""".stripMargin,
    """|function(
       |  arg1
       |// arg2,
       |)""".stripMargin
  )

}

package scalafix.workshop

import scalafix.util.logger
import scala.meta._

import org.scalatest.FunSuite
import scala.meta.tokens.Token._
import scalafix.util.DiffAssertions

class TrailingCommaSuite extends FunSuite with DiffAssertions {

  /** Removes all commas behind the last argument of function calls */
  def stripTrailingCommas(tokens: Tokens): String = {
    ""
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

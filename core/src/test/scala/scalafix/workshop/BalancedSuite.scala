package scalafix.workshop

import scala.meta._
import scala.meta.tokens.Token.LeftBrace
import scala.meta.tokens.Token.LeftBracket
import scala.meta.tokens.Token.LeftParen
import scala.meta.tokens.Token.RightBrace
import scala.meta.tokens.Token.RightBracket
import scala.meta.tokens.Token.RightParen

class BalancedSuite extends WorkshopSuite {
  override def run(str: String): Boolean = isBalanced(str.tokenize.get)

  Token

  /** Are parentheses balanced? */
  def isBalanced(tokens: Tokens): Boolean = {
    ???
  }

  checkNot("{")
  check("{}")
  checkNot("}{")
  check("{}{}{}")
  checkNot("}{}{}")
  check("[](){}")
  checkNot("[(){}")
  checkNot("[]){}")
  checkNot("[]()}")

  checkNot("(}")
  checkNot("(][)")
  checkNot("{(})")

  check("val x = { 2 }")
  check("""|def x = {
           |  List(1, 2).map { case x => x }
           |}
           |""".stripMargin)
  checkNot("""|def x =
              |  List(1, 2).map { case x => x }
              |}
              |""".stripMargin)
  check("val x = { function(2) }")
  check("""|def foo[T](args: T*): Unit = {
           |  foo(bar(kaz[T](args:_*)))
           |}
           |""".stripMargin)
  checkNot("""|def fooT](args: T*): Unit = {
              |  foo(bar(kaz[T](args:_*)))
              |}
              |""".stripMargin)
  checkNot("""|def fooT](args: T*): Unit = {
              |  foo(bar(kaz[T](args:_*
              |}
              |""".stripMargin)
  checkNot("""|def foo[T](args: T*): Unit = {
              |  foo(bar(kaz[T(args:_*))
              |}
              |""".stripMargin)

}

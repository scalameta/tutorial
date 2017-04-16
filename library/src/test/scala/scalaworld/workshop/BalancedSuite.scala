package scalaworld.workshop

import scala.meta._
// These may come in handy.
import scala.meta.tokens.Token.LeftBrace
import scala.meta.tokens.Token.LeftBracket
import scala.meta.tokens.Token.LeftParen
import scala.meta.tokens.Token.RightBrace
import scala.meta.tokens.Token.RightBracket
import scala.meta.tokens.Token.RightParen

class BalancedSuite extends WorkshopSuite {
  override def run(str: String): Boolean = isBalanced(str.tokenize.get)

  /** Are parentheses balanced? */
  def isBalanced(tokens: Tokens): Boolean = {
    var stack = List.empty[Token]
    def isMatching(open: Token, close: Token): Boolean = {
      (open, close) match {
        case (LeftParen(), RightParen())     => true
        case (LeftBrace(), RightBrace())     => true
        case (LeftBracket(), RightBracket()) => true
        case _                               => false
      }
    }
    var balanced = true
    tokens.foreach {
      case open @ (LeftParen() | LeftBracket() | LeftBrace()) =>
        stack = open :: stack
      case close @ (RightParen() | RightBracket() | RightBrace()) =>
        stack match {
          case Nil =>
            balanced = false
          case open :: tail =>
            if (!isMatching(open, close)) {
              balanced = false
            }
            stack = stack.tail
        }
      case _ =>
    }
    balanced && stack.isEmpty
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

  checkNot(""" val x = "{" + `{` + }  `}` """)

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

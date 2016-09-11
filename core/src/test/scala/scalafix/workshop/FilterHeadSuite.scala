package scalafix.workshop

import scala.meta._
import scalafix.util.DiffAssertions
import scalafix.util.logger

import org.scalatest.FunSuite

class FilterHeadSuite extends FunSuite with DiffAssertions {

  def check(a: String, b: String): Unit = {
    test(logger.reveal(a)) {
      assertNoDiff(
        noFilterHeadOption(a.parse[Stat].get).syntax,
        b
      )
    }
  }

  /** Rewrites lst.filter(cond).headOption to lst.find(cond) */
  def noFilterHeadOption(tree: Tree): Tree = tree

  check(
    """lst.filter(cond).headOption""",
    """lst.find(cond)"""
  )

}

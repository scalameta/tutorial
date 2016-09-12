package scalaworld.workshop

import scalaworld.util.logger

import org.scalatest.FunSuite

abstract class WorkshopSuite extends FunSuite {
  def run(str: String): Boolean

  def check(str: String, isTrue: Boolean = true): Unit = {
    val prefix = if (isTrue) "     " else "not: "
    ignore(prefix + logger.reveal(str)) {
      val result =
        if (isTrue) run(str)
        else !run(str)
      assert(result)
    }
  }

  def checkNot(str: String): Unit = check(str, isTrue = false)

}

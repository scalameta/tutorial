package scalaworld.rewrite

import scala.meta.inputs.Input
import scalaworld.Fixed
import scalaworld.util.DiffAssertions
import scalaworld.util.logger

import org.scalatest.FunSuite

class NonFatalTest extends FunSuite with DiffAssertions {

  def check(original: String, expected: String): Unit = {
    test(logger.reveal(original)) {
      val obtained = NonFatal.rewrite(Input.String(original)).get
      assertNoDiff(obtained, expected)
    }
  }
  check(
    """|object a {
       |  x match {
       |    case bar: Throwable => println(bar)
       |  }
       |}
       |""".stripMargin,
    """|object a {
       |  x match {
       |    case NonFatal(bar) => println(bar)
       |  }
       |}
       |""".stripMargin
  )

  check(
    """|
       |object a {
       |  // comment
       |  try danger()
       |
       |
       |  catch {
       |    case e: Throwable =>
       |      println(e)
       |  }
       |} """.stripMargin,
    """|
       |object a {
       |  // comment
       |  try danger()
       |
       |
       |  catch {
       |    case NonFatal(e) =>
       |      println(e)
       |  }
       |} """.stripMargin
  )

}

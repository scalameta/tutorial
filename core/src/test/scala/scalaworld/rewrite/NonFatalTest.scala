package scalaworld.rewrite

import scala.meta.inputs.Input
import scalaworld.util.logger

import org.scalatest.FunSuite

class NonFatalTest extends FunSuite {
  test("basic") {
    val obtained = NonFatal.rewrite(
      Input.String(
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
           |} """.stripMargin
      ))
    logger.elem(obtained)
  }

}

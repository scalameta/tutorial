package scalaworld.macros

import scala.meta._
import scala.meta.testkit._
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.scalatest.FunSuite

// If you are doing complicated macro expansions, it's recommeded to unit test
// the trickiest bits instead of relying only on integration tests.
class MainUnitTest extends FunSuite {

  // TODO(olafur) this method should be exposed in testkit
  def assertStructurallyEqual(obtained: Tree, expected: Tree): Unit = {
    StructurallyEqual(obtained, expected) match {
      case Left(AnyDiff(x, y)) =>
        fail(s"""Not Structurally equal!:
                |obtained: $x
                |expected: $y
             """.stripMargin)
      case _ =>
    }
  }

  test("@Main creates a main method") {
    val obtained = MainMacroImpl.expand(
      q"AnswerToEverything",
      List(q"val x = 42", q"println(x)"))
    val expected =
      q"""
        object AnswerToEverything {
          def main(args: Array[String]): Unit = {
            val x = 42
            println(x)
          }
        }
       """
    assertStructurallyEqual(obtained, expected)
  }
}

// This is an integration tests because it requires running the macro expansion
// through the entire compiler pipeline, if you have a bug in your macro annotation
// the expanded code may not compile causing your test suite to not compile.
class MainIntegrationTest extends FunSuite {
  test("@Main creates a main method") {
    val out: ByteArrayOutputStream = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out)) {
      MainExample.main(Array())
    }
    assert(out.toString.stripLineEnd == "Hello Scalameta macros!")
  }
}

package scalaworld.macros

import java.io.ByteArrayOutputStream
import java.io.PrintStream

import org.scalatest.FunSuite

class MainTest extends FunSuite {
  test("@Main creates a main method") {
    val out: ByteArrayOutputStream = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out)) {
//      MainExample.main(Array())
    }
    assert(out.toString.stripLineEnd == "Hello scala.meta macros!")
  }
}

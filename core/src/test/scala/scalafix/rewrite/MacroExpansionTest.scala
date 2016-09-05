package scalafix.rewrite

import org.scalatest.FunSuite

import scala.meta._

class MacroExpansionTest extends FunSuite {
  def structure(code: String) = code.parse[Source].get.structure
  test("expand main") {
    val body = """println("hello world!")"""
    val input =
      s"""|@main
          |object app {
          |  $body
          |}
          |
          |@adt class A(str: String, n: Int)
      """.stripMargin
    val expected =
      s"""|object app {
          |  def main(args: Seq[String]): Unit = {
          |    $body
          |  }
          |}
          |@adt class A(str: String, n: Int)
      """.stripMargin
    val obtained = MacroExpansion.rewrite(Input.String(input)).get
    println(obtained)
    assert(structure(obtained) == structure(expected))
  }

}

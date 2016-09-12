package scalafix

import scalafix.util.logger

import org.scalatest.FunSuite

class Playground extends FunSuite {
  import scala.meta._

  test("part 1: tokens") {
    x match {
      case Fatal(e) =>
    }

    val structure = "val x = 2".tokenize.get.structure
    q"def foobar = 2" match {
      case d: Defn.Def =>
        val x = 2
    }
  }

}

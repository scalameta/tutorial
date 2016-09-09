package scalafix

import scalafix.util.logger

import org.scalatest.FunSuite

class Playground extends FunSuite {
  import scala.meta._

  test("part 1: tokens") {
    val structure = "val x = 2".tokenize.get.structure
    logger.elem(structure)
  }

}

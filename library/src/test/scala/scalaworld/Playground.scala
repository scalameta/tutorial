package scalaworld

import scala.meta._
import org.scalameta.logger // useful for debugging

class Playground extends org.scalatest.FunSuite {
  import scala.meta._

  test("part 1: tokens") {
    val tokens = "val x = 2".tokenize.get
    logger.elem(tokens.syntax)
    logger.elem(tokens.structure)
  }

  test("part 2: trees") {
    val tree = "val x = 2".parse[Stat].get
    logger.elem(tree.syntax)
    logger.elem(tree.structure)
  }

}

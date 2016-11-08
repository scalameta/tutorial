package scalaworld.macros

import scala.collection.Map

@Class2Map
case class Class2MapExample(a: Int, b: String)(c: List[Int])

// Expanded:
//  class Class2MapExample(a: Int, b: String)(c: List[Int]) {
//    def toMap: _root_.scala.collection.Map[String, Any] =
//      _root_.scala.collection.Map(("a", a), ("b", b), ("c", c))
//  }

object Class2MapExampleMain {
  val map: Map[String, Any] = Class2MapExample(1, "b")(List(2)).toMap
  println(map) // Map(a -> 1, b -> b, c -> List(2))
}

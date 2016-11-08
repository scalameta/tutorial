package scalaworld.macros

import scala.collection.Map

@Class2Map
case class Class2MapExample(a: Int, b: String)(c: List[Int])

object ExampleUsageOfClass2MapExample extends App {
  val map: Map[String, Any] = Class2MapExample(1, "b")(List(2)).toMap
  println(map) // Map(a -> 1, b -> b, c -> List(2))
}

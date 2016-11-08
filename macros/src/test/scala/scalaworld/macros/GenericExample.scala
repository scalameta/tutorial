package scalaworld.macros

@generic
case class Foo(i: Int, s: String)

@generic
sealed trait Bar
object Bar {
  case class Baz(i: Int)     extends Bar
  case class Quux(s: String) extends Bar
}

object GenericExample extends App {
  println(implicitly[shapeless.Generic[Foo]].to(Foo(1, "string")))   // 1 :: "string" :: HNil
  println(implicitly[shapeless.Generic[Bar]].to(Bar.Baz(1)))         // Inl(Baz(1))
  println(implicitly[shapeless.Generic[Bar]].to(Bar.Quux("string"))) // Inr(Inl(Quux(string)))
}

package scalaworld.macros

@generic
case class Foo(i: Int, s: String)

object GenericExample {
  def main(args: Array[String]): Unit = {
    val f     = Foo(1, "string")
    val hlist = implicitly[shapeless.Generic[Foo]].to(f)
    println(hlist)
  }
}

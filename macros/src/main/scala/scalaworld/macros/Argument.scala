package scalaworld.macros

import scala.meta._

class Argument(arg: Int) extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    // `this` is a scala.meta tree.
    println(this.structure)
    val arg = this match {
      // The argument needs to be a literal like `1` or a string like `"foobar"`.
      // You can't pass in a variable name.
      case q"new $_(${Lit(arg: Int)})" => arg
      // Example if you have more than one argument.
      case q"new $_(${Lit(arg: Int)}, ${Lit(foo: String)})" => arg
      case _                                                => ??? // default value
    }
    println(s"Arg is $arg")
    defn.asInstanceOf[Stat]
  }
}

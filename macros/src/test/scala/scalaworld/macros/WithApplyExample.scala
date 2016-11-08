package scalaworld.macros

@WithApply
class WithApplyExample(a: Int)(b: String)

// Expanded codee:
// object WithApplyExample {
//   def apply(a: Int)(b: String): WithApplyExample = new WithApplyExample(a)(b)
// }

object WithApplyExampleMain {
  println(WithApplyExample(2)("string"))
}



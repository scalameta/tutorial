package scalaworld.macros

@WithApply
class WithApplyExample(a: Int)(b: String)

object WithApplyExampleMain {
  println(WithApplyExample(2)("string"))
}



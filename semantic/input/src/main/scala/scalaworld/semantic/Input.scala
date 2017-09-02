package scalaworld.semantic

case class Input(x: Int) {
  def +(other: Input) = Input(x + other.x)
  def +(other: Int) = Input(x + other)
  1 + 2
  List(x).map(num => Input(num + 1) + Input(2) + 3)
}

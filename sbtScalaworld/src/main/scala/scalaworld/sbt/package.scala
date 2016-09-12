package scalaworld

package object sbt {
  type ScalaworldLike = {
    def fix(code: String, filename: String): String
  }
}

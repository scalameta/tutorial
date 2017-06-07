package scalaworld.semantic

import scala.meta._
object App {
  def database: Database =
    Database.load(Classpath(AbsolutePath(BuildInfo.semanticClassdirectory)))

  def main(args: Array[String]): Unit = {
    println(database)
  }
}

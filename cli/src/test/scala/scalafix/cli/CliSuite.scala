package scalafix.cli

import scala.util.Try
import scala.util.control.NonFatal
import scalafix.cli.yaml.Parser
import scalafix.util.DiffAssertions
import scalafix.util.FileOps
import scalafix.util.LoggerOps._

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.StringReader

import org.scalatest.FunSuite
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.ScalarNode

class CliSuite extends FunSuite with DiffAssertions {

  println(Cli.helpMessage)

  test("testMain") {
    val expected = ScalafixOptions(
        files = List("foo", "bar"),
        inPlace = true
    )
    val Right(obtained) = Cli.parse(Seq("-i", "foo", "bar"))
    assertEqual(obtained, expected)
  }

  val original = """
                   |object Main {
                   |  def foo() {
                   |    println(1)
                   |  }
                   |}
                 """.stripMargin
  val expected = """
                   |object Main {
                   |  def foo(): Unit = {
                   |    println(1)
                   |  }
                   |}
                 """.stripMargin

  test("write fix to file") {
    val file = File.createTempFile("prefix", ".scala")
    FileOps.writeFile(file, original)
    Cli.runOn(
        ScalafixOptions(files = List(file.getAbsolutePath), inPlace = true))
    assertNoDiff(FileOps.readFile(file), expected)
  }

  test("print to stdout does not write to file") {
    val file = File.createTempFile("prefix", ".scala")
    FileOps.writeFile(file, original)
    val baos = new ByteArrayOutputStream()
    Cli.runOn(
        ScalafixOptions(
            out = new PrintStream(baos),
            files = List(file.getAbsolutePath)
        ))
    assertNoDiff(FileOps.readFile(file), original)
    assertNoDiff(new String(baos.toByteArray), expected)
  }

  test("write fix to directory") {
    val dir = File.createTempFile("project/src/main/scala", "sbt")
    dir.delete()
    dir.mkdirs()
    assert(dir.isDirectory)

    def createFile(): File = {
      val file = File.createTempFile("file", ".scala", dir)
      FileOps.writeFile(file, original)
      file
    }
    val file1, file2 = createFile()

    Cli.runOn(
        ScalafixOptions(files = List(dir.getAbsolutePath), inPlace = true))
    assertNoDiff(FileOps.readFile(file1), expected)
    assertNoDiff(FileOps.readFile(file2), expected)
  }
}

class YamlSuite extends FunSuite with DiffAssertions {

  case class Config(changes: Seq[StyleChange])
  object Config {

    import org.yaml.snakeyaml.nodes.Node
    import org.yaml.snakeyaml.nodes.MappingNode
    import org.yaml.snakeyaml.nodes.AnchorNode
    import scala.collection.JavaConverters._
    import StyleChange._

    trait Reader[T] {
      def read(node: Node): T = node.asInstanceOf[T]
    }

    object Reader {
      implicit object StringReader extends Reader[String]
      implicit object IntReader extends Reader[Int]
      implicit object BoolReader extends Reader[Boolean]
    }

    private def convertYamlNode(node: Node): Seq[StyleChange] = node match {
      case n: AnchorNode =>
        logger.elem(n.getRealNode)
        convertYamlNode(n.getRealNode)
      case n: MappingNode =>
        logger.elem(n.getValue)
        n.getValue.asScala.map { tup =>
          val key = tup.getKeyNode.asInstanceOf[ScalarNode].getValue
          val value = tup.getValueNode
          1
        }
        Seq.empty
    }

    case class opt[T: Reader]( name: String, f: (T, Config) => Config = { (x: T, y: Config) => y } ) { def action(newF: (T, Config) => Config): opt[T] = this.copy(f = newF) }
    val options: Seq[opt[_]] = Seq(
        opt[Int]("maxColumn").action { case (_, y) => y },
        opt[String]("style").action { case (value, config) => config }
    )

    def parse(string: String): Try[Config] = Try {
      val yaml = new Yaml().compose(new StringReader(string))
      logger.elem(yaml)
      Config(convertYamlNode(yaml))
    }
  }

  sealed abstract class StyleChange
  object StyleChange {
    case class SetStyle(string: String) extends StyleChange
    case class SetMaxColumn(n: Int) extends StyleChange
    case class SetContinuationIndentDefnSite(n: Int) extends StyleChange
    case class SetContinuationIndentCallSite(n: Int) extends StyleChange
  }

  test("yaml config") {
    val config = """style: intellij
                   |maxColumn: 80
                   |continuationIndentCallSite: 2
                   |allowNewlineBeforeColonInMassiveReturnTypes: false
                   |binPackParentConstructors: false
                   |alignByArrowEnumeratorGenerator: false
                   |alignByOpenParenCallSite: false
                   |docs: scala
                   |docs: java
                   |""".stripMargin

    import io.circe.Json
//    import cats.data.Xor
//    val result = Parser.parse(config) match {
//      case Xor.Right(json) if json.isObject=>
//        logger.elem(json.asObject.get)
//      case Xor.Left(e) =>
//        logger.error(e)
//    }
    logger.elem(Config.parse(config))
  }
}

package docs

import java.nio.file.Paths

object Main {

  def main(args: Array[String]): Unit = {
    val out = Paths.get("docs-out")
    ScalametaDocs.download(out)
    val settings = mdoc
      .MainSettings()
      .withOut(out)
      .withSiteVariables(
        Map(
          "VERSION" -> BuildInfo.scalameta
        )
      )
      .withStringModifiers(
        List(
          new mdoc.modifiers.ScastieModifier("light")
        )
      )
      .withArgs(args.toList)
    val exit = mdoc.Main.process(settings)
    sys.exit(exit)
  }
}

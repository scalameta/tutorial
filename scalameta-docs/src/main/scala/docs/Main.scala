package docs

import java.nio.file.Paths

object Main {
  def main(args: Array[String]): Unit = {
    val settings = mdoc
      .MainSettings()
      .withOut(Paths.get("docs-out"))
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

import Dependencies._

lazy val allSettings = Seq(
  organization := "org.scalameta",
  scalaVersion := scala211,
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test
  ),
  updateOptions := updateOptions.value.withCachedResolution(true)
)

allSettings

name := "scalameta-tutorial"

lazy val library = project.settings(
  allSettings,
  libraryDependencies += scalameta
)

lazy val semanticdbSettings = Seq(
  addCompilerPlugin(
    "org.scalameta" % "semanticdb-scalac" % MetaVersion cross CrossVersion.full),
  scalacOptions := Seq(
    "-Yrangepos",
    "-Xplugin-require:semanticdb"
  )
)

lazy val semanticInput = project
  .in(file("semantic/input"))
  .settings(
    allSettings,
    semanticdbSettings
  )

lazy val semantic = project
  .in(file("semantic/app"))
  .settings(
    allSettings,
    libraryDependencies += scalameta,
    buildInfoPackage := "scalaworld.semantic",
    test := run.in(Compile).toTask("").value,
    buildInfoKeys := Seq[BuildInfoKey](semanticClassDirectory.value)
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(semanticInput)

lazy val readme = scalatex
  .ScalatexReadme(
    projectId = {
      sys.props("scala.color") = "false" // remove color from repl output
      "readme"
    },
    wd = file(""),
    url = "https://github.com/scalameta/tutorial/tree/master",
    source = "Readme"
  )
  .settings(
    allSettings,
    buildInfoSettings,
    watchSources ++= {
      val compileTarget = (target in Compile).value
      for {
        f <- (scalatex.SbtPlugin.scalatexDirectory in Compile).value
          .**("*.scalatex")
          .get
        if f.relativeTo(compileTarget).isEmpty
      } yield f
    },
    sourceGenerators.in(Compile) ~= (_.init), // remove scalatex.Main
    mainClass.in(Compile) := Some("scalaworld.Readme"),
    siteSourceDirectory := target.value / "scalatex",
    test := run.in(Compile).toTask(" --validate-links").value,
    libraryDependencies += scalameta,
    libraryDependencies += contrib,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    publish := {
      ghpagesPushSite
        .dependsOn(run.in(Compile).toTask(""))
        .value
    },
    excludeFilter.in(ghpagesCleanSite) := new FileFilter {
      // Don't remove CNAME.
      def accept(f: File) =
        (ghpagesRepository.value / "CNAME").getCanonicalPath == f.getCanonicalPath
    },
    ghpagesBranch := "master",
    git.remoteRepo := "git@github.com:scalameta/scalameta.github.com.git",
    libraryDependencies ++= Seq(
      "org.pegdown" % "pegdown" % "1.6.0"
    )
  )
  .dependsOn(semanticInput)
  .enablePlugins(
    GhpagesPlugin,
    BuildInfoPlugin
  )

// Macro setting is any module that has macros, or manipulates meta trees
lazy val macroSettings = Seq(
  libraryDependencies += scalameta1,
  addCompilerPlugin(paradise),
  scalacOptions += "-Xplugin-require:macroparadise"
)

lazy val macros = project.settings(
  allSettings,
  macroSettings,
  // only needed for @generic demo.
  libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2",
  libraryDependencies += testkit1 % Test
)

lazy val buildInfoSettings: Seq[Def.Setting[_]] = Seq(
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    "baseDirectory" -> baseDirectory.in(ThisBuild).value,
    "scalameta1" -> MetaVersion1,
    "scalameta" -> MetaVersion,
    "paradise" -> ParadiseVersion,
    scalaVersion,
    semanticClassDirectory.value,
    "resources" -> resourceDirectories.in(Compile).value,
    "scala211" -> scala211,
    "scala212" -> scala212,
    "semanticScalaVersions" -> List(scala211, scala212),
    "siteOutput" -> siteSourceDirectory.value,
    sbtVersion
  ),
  buildInfoPackage := "scala.meta.tutorial"
)

lazy val semanticClassDirectory = Def.setting(
  "semanticClassdirectory" ->
    classDirectory.in(semanticInput, Compile).value.getAbsolutePath
)

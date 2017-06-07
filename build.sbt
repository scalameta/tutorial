import Dependencies._

lazy val allSettings = Seq(
  organization := "org.scalameta",
  scalaVersion := scala211,
  resolvers += Resolver.bintrayRepo("scalameta", "maven"),
  scalametaSemanticdb := ScalametaSemanticdb.Disabled,
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

lazy val macros = project.settings(
  allSettings,
  macroSettings,
  // only needed for @generic demo.
  libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2",
  libraryDependencies += testkit       % Test
)

lazy val scalahostSettings = Seq(
  addCompilerPlugin(
    "org.scalameta" % "scalahost" % MetaVersion cross CrossVersion.full),
  scalacOptions := Seq(
    "-Yrangepos",
    "-Xplugin-require:scalahost"
  )
)

lazy val semanticInput = project
  .in(file("semantic/input"))
  .settings(
    allSettings,
    scalahostSettings
  )

lazy val semantic = project
  .in(file("semantic/app"))
  .settings(
    allSettings,
    libraryDependencies += scalameta,
    buildInfoPackage := "scalaworld.semantic",
    buildInfoKeys := Seq[BuildInfoKey](
      "semanticClassdirectory" ->
        classDirectory.in(semanticInput, Compile).value
    )
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(semanticInput)

lazy val readme = scalatex
  .ScalatexReadme(projectId = "readme",
                  wd = file(""),
                  url = "https://github.com/scalameta/tutorial/tree/master",
                  source = "Readme")
  .settings(
    allSettings,
    buildInfoSettings,
    siteSourceDirectory := target.value / "scalatex",
    test := run.in(Compile).toTask(" --validate-links").value,
    libraryDependencies += scalameta,
    libraryDependencies += contrib,
    publish := {
      ghpagesPushSite
        .dependsOn(run.in(Compile).toTask(""))
        .value
    },
    git.remoteRepo := "git@github.com:scalameta/tutorial.git",
    libraryDependencies ++= Seq(
      "com.twitter" %% "util-eval" % "6.34.0",
      "org.pegdown" % "pegdown"    % "1.6.0"
    )
  )
  .dependsOn(semanticInput)
  .enablePlugins(
    GhpagesPlugin,
    BuildInfoPlugin
  )

// Macro setting is any module that has macros, or manipulates meta trees
lazy val macroSettings = Seq(
  libraryDependencies += scalameta,
  addCompilerPlugin(paradise),
  scalacOptions += "-Xplugin-require:macroparadise"
)

lazy val buildInfoSettings: Seq[Def.Setting[_]] = Seq(
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    "scalameta" -> MetaVersion,
    "paradise"  -> ParadiseVersion,
    scalaVersion,
    "managedSourceDirectory" ->
      managedSourceDirectories.in(Compile).value.head.getAbsolutePath,
    "semanticClassdirectory" ->
      classDirectory.in(semanticInput, Compile).value.getAbsolutePath,
    "semanticScalaVersions" -> List(scala211, scala212),
    sbtVersion
  ),
  buildInfoPackage := "scala.meta.tutorial"
)

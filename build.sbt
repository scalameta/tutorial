// NOTE. For a minimal build file to play around with scalameta macros, see:
// https://github.com/olafurpg/scalameta-macro-template
import sbt.ScriptedPlugin
import sbt.ScriptedPlugin._
import scoverage.ScoverageSbtPlugin.ScoverageKeys._

lazy val buildSettings = Seq(
  organization := "org.scalameta",
  assemblyJarName in assembly := "scalaworld.jar",
  // See core/src/main/scala/ch/epfl/scala/Versions.scala
  version := scalaworld.Versions.nightly,
  scalaVersion := scalaworld.Versions.scala,
  updateOptions := updateOptions.value.withCachedResolution(true)
)

// Macro setting is any module that has macros, or manipulates meta trees
lazy val macroSettings = Seq(
  libraryDependencies += "org.scalameta" %% "scalameta" % "1.3.0",
  resolvers += Resolver.url(
    "scalameta",
    url("http://dl.bintray.com/scalameta/maven"))(Resolver.ivyStylePatterns),
  addCompilerPlugin(
    "org.scalameta" % "paradise" % "3.0.0.127" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise"
)

lazy val jvmOptions = Seq(
  "-Xss4m"
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Xlint"
)

lazy val commonSettings = Seq(
  ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages :=
    ".*Versions;scalaworld\\.(sbt|util)",
  triggeredMessage in ThisBuild := Watched.clearWhenTriggered,
  scalacOptions in (Compile, console) := compilerOptions :+ "-Yrepl-class-based",
  testOptions in Test += Tests.Argument("-oD")
)

lazy val allSettings = commonSettings ++ buildSettings

lazy val root = project
  .in(file("."))
  .settings(
    allSettings,
    moduleName := "scalaworld",
    initialCommands in console :=
      """
        |import scala.meta._
        |import scalaworld._
      """.stripMargin
  )
  .aggregate(
    core,
    cli,
    macros,
    readme,
    sbtScalaworld
  )
  .dependsOn(core)

lazy val core = project.settings(
  allSettings,
  moduleName := "scalaworld-core",
  libraryDependencies ++= Seq(
    "com.lihaoyi"    %% "sourcecode"   % "0.1.2",
    "org.scalameta"  %% "scalameta"    % "1.0.0",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    // Test dependencies
    "org.scalatest"                  %% "scalatest" % "3.0.0" % "test",
    "com.googlecode.java-diff-utils" % "diffutils"  % "1.3.0" % "test"
  )
)

lazy val cli = project
  .settings(
    allSettings,
    packSettings,
    moduleName := "scalaworld-cli",
    packJvmOpts := Map(
      "scalaworld"           -> jvmOptions,
      "scalaworld_ng_server" -> jvmOptions
    ),
    mainClass in assembly := Some("scalaworld.cli.Cli"),
    packMain := Map(
      "scalaworld"           -> "scalaworld.cli.Cli",
      "scalaworld_ng_server" -> "com.martiansoftware.nailgun.NGServer"
    ),
    libraryDependencies ++= Seq(
      "com.github.scopt"           %% "scopt"         % "3.5.0",
      "com.github.alexarchambault" %% "case-app"      % "1.1.0-RC3",
      "com.martiansoftware"        % "nailgun-server" % "0.9.1"
    )
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val macros = project.settings(
  allSettings,
  macroSettings,
  libraryDependencies +=
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  // only needed for @generic demo.
  libraryDependencies +=
    "com.chuusai" %% "shapeless" % "2.3.2"
)

lazy val sbtScalaworld = project.settings(
  allSettings,
  ScriptedPlugin.scriptedSettings,
  sbtPlugin := true,
  coverageHighlighting := false,
  scalaVersion := "2.10.5",
  moduleName := "sbt-scalaworld",
  sources in Compile +=
    baseDirectory.value / "../core/src/main/scala/scalaworld/Versions.scala",
  scriptedLaunchOpts := Seq(
    "-Dplugin.version=" + version.value,
    // .jvmopts is ignored, simulate here
    "-XX:MaxPermSize=256m",
    "-Xmx2g",
    "-Xss2m"
  ),
  scriptedBufferLog := false
)

lazy val readme = scalatex
  .ScalatexReadme(projectId = "readme",
                  wd = file(""),
                  url = "https://github.com/scalameta/tutorial/tree/master",
                  source = "Readme")
  .settings(
    allSettings,
    libraryDependencies ++= Seq(
      "com.twitter" %% "util-eval" % "6.34.0",
      "org.pegdown" % "pegdown"    % "1.6.0"
    ),
    dependencyOverrides += "com.lihaoyi" %% "scalaparse" % "0.3.1"
  )
  .dependsOn(core, cli)

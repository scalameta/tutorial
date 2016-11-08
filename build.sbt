// NOTE. For a minimal build file to play around with scalameta macros, see:
// https://github.com/olafurpg/scalameta-macro-template
import sbt.ScriptedPlugin
import sbt.ScriptedPlugin._
import scoverage.ScoverageSbtPlugin.ScoverageKeys._


scalafmtConfig in ThisBuild := Some(file(".scalafmt"))

lazy val buildSettings = Seq(
  organization := "ch.epfl.scala",
  assemblyJarName in assembly := "scalaworld.jar",
  // See core/src/main/scala/ch/epfl/scala/Versions.scala
  version := scalaworld.Versions.nightly,
  scalaVersion := scalaworld.Versions.scala,
  updateOptions := updateOptions.value.withCachedResolution(true)
)

// Macro setting is any module that has macros, or manipulates meta trees
lazy val macroSettings = Seq(
  libraryDependencies += "org.scalameta" %% "scalameta" % "1.1.0",
  resolvers += Resolver.sonatypeRepo("snapshots"),
  addCompilerPlugin(
    "org.scalameta" % "paradise" % "3.0.0-M5" cross CrossVersion.full),
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

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishMavenStyle := true,
  publishArtifact := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  licenses := Seq(
    "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://github.com/olafurpg/scala.meta-workshop")),
  autoAPIMappings := true,
  apiURL := Some(url("https://scalacenter.github.io/scalaworld/docs/")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/scalacenter/scalaworld"),
      "scm:git:git@github.com:scalacenter/scalaworld.git"
    )
  ),
  pomExtra :=
    <developers>
      <developer>
        <id>olafurpg</id>
        <name>Ólafur Páll Geirsson</name>
        <url>https://geirsson.com</url>
      </developer>
    </developers>
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {}
)

lazy val allSettings = commonSettings ++ buildSettings ++ publishSettings

lazy val root = project
  .in(file("."))
  .settings(moduleName := "scalaworld")
  .settings(allSettings)
  .settings(noPublish)
  .settings(
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

lazy val core = project
  .settings(allSettings)
  .settings(
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
  .settings(allSettings)
  .settings(packSettings)
  .settings(
    moduleName := "scalaworld-cli",
    packJvmOpts := Map(
      "scalaworld" -> jvmOptions,
      "scalaworld_ng_server" -> jvmOptions
    ),
    mainClass in assembly := Some("scalaworld.cli.Cli"),
    packMain := Map(
      "scalaworld" -> "scalaworld.cli.Cli",
      "scalaworld_ng_server" -> "com.martiansoftware.nailgun.NGServer"
    ),
    libraryDependencies ++= Seq(
      "com.github.scopt"           %% "scopt"         % "3.5.0",
      "com.github.alexarchambault" %% "case-app"      % "1.1.0-RC3",
      "com.martiansoftware"        % "nailgun-server" % "0.9.1"
    )
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val macros = project
  .settings(allSettings: _*)
  .settings(macroSettings: _*)
  .settings(
    libraryDependencies +=
      "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  )

lazy val sbtScalaworld = project
  .settings(allSettings)
  .settings(ScriptedPlugin.scriptedSettings)
  .settings(
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
                  url = "https://github.com/olafurpg/scala.meta-workshop/tree/master",
                  source = "Readme")
  .settings(allSettings)
  .settings(noPublish)
  .dependsOn(core)
  .dependsOn(cli)
  .settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "util-eval" % "6.34.0",
      "org.pegdown" % "pegdown" % "1.6.0"
    ),
    dependencyOverrides += "com.lihaoyi" %% "scalaparse" % "0.3.1"
  )

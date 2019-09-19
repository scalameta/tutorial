def scalameta = "4.2.3"
def scalafix = "0.9.7"
def scala212 = "2.12.10"

inThisBuild(
  List(
    organization := "org.scalameta",
    scalaVersion := scala212,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.5" % Test
    ),
    resolvers += Resolver.sonatypeRepo("releases")
  )
)

name := "scalameta-tutorial"
skip in publish := true

lazy val docs = project
  .in(file("scalameta-docs"))
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](
      "scalameta" -> scalameta
    ),
    buildInfoPackage := "docs",
    moduleName := "scalameta-docs",
    mdoc := run.in(Compile).evaluated,
    libraryDependencies ++= List(
      "org.scalameta" %% "testkit" % scalameta,
      "ch.epfl.scala" %% "scalafix-core" % scalafix
    )
  )
  .enablePlugins(BuildInfoPlugin, DocusaurusPlugin)

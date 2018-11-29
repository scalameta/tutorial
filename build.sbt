def scalameta = "4.1.0"
def scalafix = "0.9.1"
def scala212 = "2.12.7"

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
    mainClass.in(Compile) := Some("docs.Main"),
    libraryDependencies ++= List(
      "com.geirsson" % "mdoc" % "0.6.0" cross CrossVersion.full,
      "org.scalameta" %% "testkit" % scalameta,
      "ch.epfl.scala" %% "scalafix-core" % scalafix
    )
  )
  .enablePlugins(BuildInfoPlugin, DocusaurusPlugin)

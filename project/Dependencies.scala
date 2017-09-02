import sbt._, Keys._
object Dependencies {
  lazy val MetaVersion = "2.0.0-RC1"
  lazy val MetaVersion1 = "1.8.0"
  lazy val ParadiseVersion = "3.0.0-M10"
  lazy val scala211 = "2.11.11"
  lazy val scala212 = "2.12.3"
  lazy val scalameta1 = "org.scalameta" %% "scalameta" % MetaVersion1
  lazy val scalameta = "org.scalameta" %% "scalameta" % MetaVersion
  lazy val contrib = "org.scalameta" %% "contrib" % MetaVersion
  lazy val testkit = "org.scalameta" %% "testkit" % MetaVersion
  lazy val paradise = "org.scalameta" % "paradise" % ParadiseVersion cross CrossVersion.full
}

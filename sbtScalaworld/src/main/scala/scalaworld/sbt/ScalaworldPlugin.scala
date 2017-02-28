/* Modified version of
https://github.com/sbt/sbt-scalariform/blob/61a0b7b75441b458e4ff3c6c30ed87d087a2e569/src/main/scala/com/typesafe/sbt/Scalariform.scala

Original licence:

Copyright 2011-2012 Typesafe Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package scalaworld.sbt

import scala.collection.immutable.Seq
import scala.util.Failure
import scala.util.Success

import java.net.URLClassLoader

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import sbt.{IntegrationTest => It}

object ScalaworldPlugin extends AutoPlugin {

  object autoImport {
    lazy val scalaworld: TaskKey[Unit] =
      taskKey[Unit]("Fix Scala sources using scalaworld")

    lazy val scalaworldConfig: TaskKey[Option[File]] =
      taskKey[Option[File]]("Configuration file for scalaworld.")

    lazy val hasScalaworld: TaskKey[HasScalaworld] = taskKey[HasScalaworld](
      "Classloaded Scalaworld210 instance to overcome 2.10 incompatibility issues.")

    def scalaworldSettings: Seq[Setting[_]] =
      noConfigScalaworldSettings ++
        inConfig(Compile)(configScalaworldSettings) ++
        inConfig(Test)(configScalaworldSettings)

    lazy val scalaworldSettingsWithIt: Seq[Setting[_]] =
      scalaworldSettings ++
        inConfig(IntegrationTest)(configScalaworldSettings)

  }
  import autoImport._

  override val projectSettings = scalaworldSettings

  override def trigger = allRequirements

  override def requires = JvmPlugin

  def noConfigScalaworldSettings: Seq[Setting[_]] =
    List(
      ivyConfigurations += config("scalaworld").hide,
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-library"       % _root_.scalaworld.Versions.scala   % "scalaworld",
        "ch.epfl.scala"  % "scalaworld-cli_2.11" % _root_.scalaworld.Versions.nightly % "scalaworld"
      )
    )

  def configScalaworldSettings: Seq[Setting[_]] =
    List(
      (sourceDirectories in hasScalaworld) := unmanagedSourceDirectories.value,
      includeFilter in Global in hasScalaworld := "*.scala",
      scalaworldConfig in Global := None,
      hasScalaworld := {
        val report = update.value
        val jars   = report.select(configurationFilter("scalaworld"))
        HasScalaworld(
          getScalaworldLike(new URLClassLoader(jars.map(_.toURI.toURL).toArray,
                                               null),
                            streams.value),
          scalaworldConfig.value,
          streams.value,
          (sourceDirectories in hasScalaworld).value.toList,
          (includeFilter in hasScalaworld).value,
          (excludeFilter in hasScalaworld).value,
          thisProjectRef.value
        )
      },
      scalaworld := hasScalaworld.value.writeFormattedContentsToFiles()
    )

  private def getScalaworldLike(classLoader: URLClassLoader,
                                streams: TaskStreams): ScalaworldLike = {
    val loadedClass =
      new ReflectiveDynamicAccess(classLoader)
        .createInstanceFor[ScalaworldLike]("scalaworld.cli.Scalaworld210",
                                           Seq.empty)

    loadedClass match {
      case Success(x) => x
      case Failure(e) =>
        streams.log.error(
          s"""Unable to classload Scalaworld, please file an issue:
             |https://github.com/scalacenter/scalaworld/issues
             |
             |URLs: ${classLoader.getURLs.mkString("\n")}
             |Version: ${_root_.scalaworld.Versions.nightly}
             |Error: ${e.getClass}
             |Message: ${e.getMessage}
             |${e.getStackTrace.mkString("\n")}""".stripMargin)
        throw e
    }
  }
}

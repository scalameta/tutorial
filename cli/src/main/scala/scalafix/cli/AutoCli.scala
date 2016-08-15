package scalafix.cli

import scalafix.rewrite.Rewrite

import java.io.InputStream
import java.io.PrintStream

import caseapp.CaseApp
import caseapp.core.ArgParser

object AutoCli {
  implicit val rewriteRead: ArgParser[Rewrite] = ArgParser.instance[Rewrite] {
    str =>
      ScalafixOptions.rewriteMap.get(str) match {
        case Some(x) => Right(x)
        case _ =>
          val keys = ScalafixOptions.rewriteMap.keys.mkString(", ")
          Left(s"invalid input $str, must be one of $keys")
      }
  }

  implicit val inputStreamRead: ArgParser[InputStream] =
    ArgParser.instance[InputStream](x => Right(System.in))

  implicit val printStreamRead: ArgParser[PrintStream] =
    ArgParser.instance[PrintStream](x => Right(System.out))

  def parse(args: Seq[String]): Either[String, (ScalafixOptions, Seq[String])] =
    CaseApp.parse[ScalafixOptions](args)

}

package scalaworld

import scala.meta.tokenizers.Tokenized
import scalatags.Text.all._

import com.twitter.util.Eval
import java.text._
import java.util.Calendar
import scala.util.Try
import scala.util.control.NonFatal
import scalatags.Text.TypedTag
import scalatags.Text.all._
import scalatex.Main._

object Readme {
  def github: String    = "https://github.com"
  def repo: String      = "https://github.com/olafurpg/scala.meta-workshop"
  def dotty             = a(href := "http://dotty.epfl.ch/", "Dotty")
  def issue(id: Int)    = a(href := repo + s"/issues/$id", s"#$id")
  def note              = b("NOTE")
  def issues(ids: Int*) = span(ids.map(issue): _*)

  val eval = new Eval()

  def url(src: String) = a(href := src, src)

  private def unindent(frag: String): String = {
    // code frags are passed in raw from *.scalatex.
    val toStrip =
      " " * Try(
        frag.lines
          .withFilter(_.nonEmpty)
          .map(_.takeWhile(_ == ' ').length)
          .min).getOrElse(0)
    frag.lines.map(_.stripPrefix(toStrip)).mkString("\n")
  }

  def getMetaCode(indentedCode: String): String = {
    s"""
       |import scala.meta._
       |${unindent(indentedCode)}
       """.stripMargin
  }
  def callout(kind: String, msg: Frag*) =
    div(cls := s"bs-callout bs-callout-${kind}", p(msg))

  def info(msg: Frag*)    = callout("info", msg: _*)
  def success(msg: Frag*) = callout("success", msg: _*)
  def warning(msg: Frag*) = callout("warning", msg: _*)
  def danger(msg: Frag*)  = callout("danger", msg: _*)

  def meta(indentedCode: String) = {
    val code = getMetaCode(indentedCode)
    repl(code, dropLines = 1)
  }

  /** Scalatex doesn't support default args */
  def repl(code: String): TypedTag[String] = {
    repl(code, 0)
  }

  def image(file: String, caption: String = "") = div(
    cls := "text-center",
    img(style := "width: 100%", src := "img/" + file),
    if (caption.nonEmpty) p("Caption: " + caption)
    else p()
  )

  /**
    * repl session, inspired by tut.
    *
    * Example: code="1 + 1" returns
    * "scala> 1 + 1
    * res0: Int = 2"
    */
  def repl(code: String, dropLines: Int): TypedTag[String] = {
    import scala.meta._
    val expressions = s"{$code}".parse[Stat].get.asInstanceOf[Term.Block].stats

    val evaluated = try {
      eval[Any](code)
    } catch {
      case NonFatal(e) => e
    }
    val output = evaluated match {
      case s: String =>
        s"""
           |"${s.replace("\"", "\\\"")}"""".stripMargin
      case x => x.toString
    }
    val result = s"""${expressions
                      .map(x => s"scala> ${x.toString().trim}")
                      .mkString("\n")}
                    |res0: ${evaluated.getClass.getName} = $output
                    |""".stripMargin.lines.drop(dropLines).mkString("\n")
    hl.scala(result)
  }

  def half(frags: Frag*) = div(frags, width := "50%", float.left)

  def pairs(frags: Frag*) = div(frags, div(clear := "both"))

  def sideBySide(left: String, right: String) =
    pairs(List(left, right).map(x => half(hl.scala(x))): _*)

}

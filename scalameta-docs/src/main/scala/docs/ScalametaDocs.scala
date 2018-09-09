package docs
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit
import scala.meta.internal.io.InputStreamIO
import scala.util.control.NonFatal

case class MarkdownFile(
    title: String,
    id: String,
    sidebarLabel: String,
    url: String,
    postProcess: String => String = identity
)

object ScalametaDocs {
  def download(out: Path): Unit = {
    println("Downloading scalameta docs...")
    val start = System.nanoTime()
    files.foreach(file => downloadFile(out, file))
    val end = System.nanoTime()
    val elapsed = TimeUnit.NANOSECONDS.toMillis(end - start)
    println(s"Done! (${elapsed}ms)")
  }

  // TODO: https://github.com/scalameta/scalameta/pull/1764
  // val root = "https://raw.githubusercontent.com/scalameta/scalameta/master"
  val root =
    "https://raw.githubusercontent.com/olafurpg/scalameta/d0563939390908239675810770f60799bd17b543"

  def files = List(
    MarkdownFile(
      title = "Quasiquotes",
      id = "quasiquotes",
      sidebarLabel = "Quasiquotes",
      url = s"$root/notes/quasiquotes.md"
    ),
    MarkdownFile(
      title = "SemanticDB Specification",
      id = "semanticdb",
      sidebarLabel = "Specification",
      url = s"$root/semanticdb/semanticdb3/semanticdb3.md",
      postProcess = { spec =>
        val prelude =
          """
            |SemanticDB is a data model for semantic information such as symbols and types about 
            |programs in Scala and other languages. SemanticDB decouples production and consumption 
            |of semantic information, establishing documented means for communication between tools.
            |""".stripMargin
        val body =
          spec.lines.dropWhile(!_.startsWith("## Motivation")).mkString("\n")
        prelude + body
      }
    )
  )

  private def downloadFile(out: Path, md: MarkdownFile): Unit = {
    try {
      val uri = new URL(md.url)
      val filename = Paths.get(md.id + ".md")
      val file = out.resolve(filename)
      val in = uri.openStream()
      val bytes =
        try InputStreamIO.readBytes(in)
        finally in.close()
      val postprocessed = md.postProcess(new String(bytes))
      val frontMatter =
        s"""---
           |id: ${md.id}
           |title: ${md.title}
           |sidebar_label: ${md.sidebarLabel}
           |---
           |""".stripMargin
      Files.write(
        file,
        frontMatter.getBytes(),
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.CREATE
      )
      Files.write(
        file,
        postprocessed.getBytes(),
        StandardOpenOption.APPEND
      )
    } catch {
      case NonFatal(e) =>
        println(e.getMessage)
    }

  }
}

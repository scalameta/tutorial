package docs
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import scala.meta.internal.io.InputStreamIO
import scala.util.control.NonFatal

case class MarkdownFile(
    title: String,
    id: String,
    filename: String,
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
      title = "Quasiquotes Specification",
      id = "quasiquotes",
      filename = "trees/quasiquotes.md",
      sidebarLabel = "Quasiquotes",
      url = s"$root/notes/quasiquotes.md"
    ),
    MarkdownFile(
      title = "SemanticDB Guide",
      id = "guide",
      filename = "semanticdb/guide.md",
      sidebarLabel = "Guide",
      url = s"$root/semanticdb/semanticdb3/guide.md",
      postProcess = { guide =>
        val toc =
          Pattern.compile("- \\[Installation.*#metals\\)", Pattern.DOTALL)
        val header = Pattern.compile("^# SemanticDB.*")
        List(toc, header).foldLeft(guide) {
          case (g, p) => p.matcher(g).replaceFirst("")
        }
      }
    ),
    MarkdownFile(
      title = "SemanticDB Specification",
      id = "specification",
      filename = "semanticdb/specification.md",
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
      val filename = Paths.get(md.filename)
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
      Files.createDirectories(file.getParent)
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
        println("error: " + e.getMessage)
    }

  }
}

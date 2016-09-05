package scalafix.util

object logger {
  def elem(ts: sourcecode.Text[Any]*)(implicit line: sourcecode.Line,
                                      file: sourcecode.File,
                                      enclosing: sourcecode.Enclosing): Unit =
    PrintlnLogger.elem(ts:_*)(line, file, enclosing)
}

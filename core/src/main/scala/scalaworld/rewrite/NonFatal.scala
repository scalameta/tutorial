package scalaworld.rewrite

import scala.meta._
import scalaworld.Fixed
import scalaworld.util.logger

/**
  * Rewrite this
  *   catch {
  *     case e: Throwable => ...
  *   }
  *
  * into this
  *   catch {
  *     case NonFatal(e) => ...
  *   }
  *
  */
object NonFatal extends Rewrite {
  override def rewrite(code: Input): Fixed = withParsed(code) { tree =>
    val throwables = tree.collect {
      case t @( p"case ($name: Throwable) => $expr" )=>
        val c = t.asInstanceOf[Case].pat.tokens
        c.toList -> name.children.head.asInstanceOf[Term.Name]
    }
    val toRemove = throwables.flatMap(_._1.toList).toSet
    val toReplace = throwables.map { case (ts, name) => ts.head -> name }.toMap
    val sb = new StringBuilder
    tree.tokens.foreach { token =>
      if (toReplace.contains(token)) {
        logger.elem(token.syntax)
        sb.append(q"NonFatal(${toReplace(token)})".syntax)
      } else if (!toRemove.contains(token)) {
        logger.elem(token.syntax)
        sb.append(token.syntax)
      }
    }
    // Your implementation here
    Fixed.Success(sb.toString())
  }
}

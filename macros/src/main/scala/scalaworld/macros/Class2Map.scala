package scalaworld.macros

import scala.collection.immutable.Seq
import scala.meta._

class Class2Map extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case cls@ Defn.Class(_, _, _, Ctor.Primary(_, _, paramss), template) =>
        val namesToValues: Seq[Term.Tuple] = paramss.flatten.map { param =>
          q"(${param.name.syntax}, ${Term.Name(param.name.value)})"
        }
        val toMapImpl: Term = q"_root_.scala.collection.Map(..$namesToValues)"
        val toMap =
          q"def toMap: _root_.scala.collection.Map[String, Any] = $toMapImpl"
        val templateStats: Seq[Stat] = template.stats match {
          case Some(stats) => toMap +: stats
          case None => toMap :: Nil
        }
        cls.copy(templ = template.copy(stats = Some(templateStats)))
      case _ =>
        println(defn.structure)
        abort("@Class2Map must be annotated on a class.")
    }
  }
}

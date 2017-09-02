package scalaworld.macros

import scala.collection.immutable.Seq
import scala.meta._

// Before:
// @WithApply
// class WithApplyExample(a: Int)(b: String)
// After:
// class WithApplyExample(a: Int)(b: String)
// object WithApplyExample {
//   def apply(a: Int)(b: String): WithApplyExample = new WithApplyExample(a)(b)
// }

class WithApply extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    def createApply(
        name: Type.Name,
        paramss: Seq[Seq[Term.Param]]): Defn.Def = {
      val args = paramss.map(_.map(param => Term.Name(param.name.value)))
      q"""def apply(...$paramss): $name =
            new ${Ctor.Ref.Name(name.value)}(...$args)"""
    }
    defn match {
      // companion object exists
      case Term.Block(
          Seq(cls @ Defn.Class(_, name, _, ctor, _), companion: Defn.Object)) =>
        val applyMethod = createApply(name, ctor.paramss)
        val templateStats: Seq[Stat] =
          applyMethod +: companion.templ.stats.getOrElse(Nil)
        val newCompanion = companion.copy(
          templ = companion.templ.copy(stats = Some(templateStats)))
        Term.Block(Seq(cls, newCompanion))
      // companion object does not exists
      case cls @ Defn.Class(_, name, _, ctor, _) =>
        val applyMethod = createApply(name, ctor.paramss)
        val companion = q"object ${Term.Name(name.value)} { $applyMethod }"
        Term.Block(Seq(cls, companion))
      case _ =>
        println(defn.structure)
        abort("@WithApply must annotate a class.")
    }
  }
}

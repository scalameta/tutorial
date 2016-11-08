package scalaworld.macros

import scala.collection.immutable.Seq
import scala.meta._

class generic extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    def createGeneric(name: Type.Name, paramss: Seq[Seq[Term.Param]]): Stat = {
      val params = paramss match {
        case params :: Nil => params
        case _             => abort("Can't create generic for curried functions yet.")
      }
      val hlistType: Type = params.foldRight[Type](t"HNil") {
        case (Term.Param(_, _, Some(decltpe: Type), _), accum) =>
          // why is the downcast required?
          Type.ApplyInfix(decltpe, t"::", accum)
      }
      val hlistTerm: Term = params.foldRight[Term](q"HNil") {
        case (param, accum) =>
          Term.ApplyInfix(q"t.${Term.Name(param.name.value)}", q"::", Nil, Seq(accum))
      }
      val hlistPat: Pat = params.foldRight[Pat](q"HNil") {
        case (param, accum) =>
          Pat.ExtractInfix(Pat.Var.Term(Term.Name(param.name.value)),
                           q"::",
                           Seq(accum))
      }
      val args       = params.map(param => Term.Name(param.name.value))
      val repr: Stat = q"type Repr = $hlistType"
      val to: Stat   = q"def to(t: $name): Repr = $hlistTerm"
      val patmat =
        Case(hlistPat, None, q"new ${Ctor.Ref.Name(name.value)}(..$args)")
      val from: Stat =
        q"""def from(r: Repr): $name = r match {
              ..case ${Seq(patmat)}
            }
         """
      q"""implicit val ${Pat.Var.Term(Term.Name(name.syntax + "Generic"))} =
            new _root_.shapeless.Generic[$name] {
              import shapeless._
              $repr
              $from
              $to
            }
       """
    }
    defn match {
      // companion object exists
      case Term.Block(
          Seq(cls @ Defn.Class(_, name, _, ctor, _),
              companion: Defn.Object)) =>
        val newStats =
          createGeneric(name, ctor.paramss) +:
            companion.templ.stats.getOrElse(Nil)
        val newCompanion =
          companion.copy(templ = companion.templ.copy(stats = Some(newStats)))
        Term.Block(Seq(cls, newCompanion))
      // companion object does not exists
      case cls @ Defn.Class(_, name, _, ctor, _) =>
        val companion =
          q"object ${Term.Name(name.value)} { ${createGeneric(name, ctor.paramss)} } "
        println(companion.syntax)
        Term.Block(Seq(cls, companion))
      case _ =>
        println(defn.structure)
        abort("@WithApply must annotate a class.")
    }
  }
}

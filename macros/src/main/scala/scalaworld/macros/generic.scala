package scalaworld.macros

import scala.collection.immutable.Seq
import scala.meta._

// Before:
// @generic
// case class Foo(i: Int, s: String)
//
// @generic
// sealed trait Bar
// object Bar {
//   case class Baz(i: Int)     extends Bar
//   case class Quux(s: String) extends Bar
// }
//
// After:
// // observe that infix operators are used where possible, no ::[A, B]
// case class Foo(i: Int, s: String)
// object Foo {
//   implicit val FooGeneric: _root_.shapeless.Generic[Foo] =
//     new _root_.shapeless.Generic[Foo] {
//       import shapeless._
//       type Repr = Int :: String :: HNil
//       def from(r: Repr): Foo = r match {
//         case i :: s :: HNil => new Foo(i, s)
//       }
//       def to(t: Foo): Repr = t.i :: t.s :: HNil
//     }
// }
// sealed trait Bar
// object Bar {
//   implicit val BarGeneric: _root_.shapeless.Generic[Bar] =
//     new _root_.shapeless.Generic[Bar] {
//       import shapeless._
//       type Repr = Baz :+: Quux :+: CNil
//
//       def from(r: Repr): Bar = r match {
//         case Inl(t)      => t
//         case Inr(Inl(t)) => t
//         case Inr(Inr(_)) => sys.error("This can't happen")
//       }
//
//       def to(t: Bar): Repr = t match {
//         case t: Baz  => Inl(t)
//         case t: Quux => Inr(Inl(t))
//       }
//     }
//   case class Baz(i: Int) extends Bar()
//   case class Quux(s: String) extends Bar()
// }

object ClassOrTrait {
  def unapply(any: Any): Option[(Seq[Mod], Type.Name)] = any match {
    case t: Defn.Class => Some((t.mods, t.name))
    case t: Defn.Trait => Some((t.mods, t.name))
    case _             => None
  }
}

// This implementation contains quite a bit of boilerplate because we
// generate almost the same code in term, type and pattern position.
class generic extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    def mkCoproductTerm(depth: Int): Term =
      if (depth <= 0) q"Inl(t)"
      else q"Inr(${mkCoproductTerm(depth - 1)})"

    def mkCoproductPattern(depth: Int): Pat =
      if (depth <= 0) p"Inl(t)"
      else p"Inr(${mkCoproductPattern(depth - 1)})"

    // final unreachable case in `from` for coproduct generic.
    def mkCantHappen(depth: Int): Pat =
      if (depth <= 0) p"Inr(_)"
      else p"Inr(${mkCantHappen(depth - 1)})"

    def mkCoproductGeneric(superName: Type.Name,
                           subTypes: Seq[Defn.Class]): Stat = {
      val coproductType: Type = subTypes.foldRight[Type](t"CNil") {
        case (cls, accum) =>
          Type.ApplyInfix(cls.name, t":+:", accum)
      }
      val coproductTermCases: Seq[Case] = subTypes.zipWithIndex.map {
        case (cls, i) =>
          p"case t: ${cls.name} => ${mkCoproductTerm(i)}"
      }
      val coproductTerm = q"t match { ..case $coproductTermCases }"
      val coproductPat: Seq[Case] = subTypes.zipWithIndex.map {
        case (cls, i) =>
          p"case ${mkCoproductPattern(i)} => t"
      }
      val cantHappen =
        p"""case ${mkCantHappen(subTypes.length - 1)} =>
              sys.error("This can't happen")
         """
      mkGeneric(superName,
                coproductType,
                coproductTerm,
                coproductPat :+ cantHappen)
    }

    def mkGeneric(name: Type.Name,
                  repr: Type,
                  to: Term,
                  from: Seq[Case]): Stat = {
      val reprTyp: Stat = q"type Repr = $repr"
      val toDef: Stat   = q"def to(t: $name): Repr = $to"
      val fromDef: Stat =
        q"def from(r: Repr): $name = r match { ..case $from }"
      val implicitName = Term.Name(name.syntax + "Generic")
      q"""implicit val ${Pat.Var
        .Term(implicitName)}: _root_.shapeless.Generic[$name] =
            new _root_.shapeless.Generic[$name] {
              import shapeless._
              $reprTyp
              $fromDef
              $toDef
            }
       """
    }

    def mkHListGeneric(name: Type.Name, paramss: Seq[Seq[Term.Param]]): Stat = {
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
          Term.ApplyInfix(q"t.${Term.Name(param.name.value)}",
                          q"::",
                          Nil,
                          Seq(accum))
      }
      val hlistPat: Pat = params.foldRight[Pat](q"HNil") {
        case (param, accum) =>
          Pat.ExtractInfix(Pat.Var.Term(Term.Name(param.name.value)),
                           q"::",
                           Seq(accum))
      }
      val args = params.map(param => Term.Name(param.name.value))
      val patmat =
        Case(hlistPat, None, q"new ${Ctor.Ref.Name(name.value)}(..$args)")
      mkGeneric(name, hlistType, hlistTerm, Seq(patmat))
    }

    def isSealed(mods: Seq[Mod]): Boolean = mods.exists(_.syntax == "sealed")

    // Slightly tricky case, a Defn.Class parent constructor is a
    // Term.Apply(Ctor.Ref.Name(), Nil) that we deconstruct here.
    def inherits(superType: Type.Name)(cls: Defn.Class): Boolean =
      cls.templ.parents.headOption.exists {
        case q"$parent()" => parent.syntax == superType.syntax
        case _            => false
      }

    defn match {
      // Sealed ADT.
      case Term.Block(
          Seq(t @ ClassOrTrait(mods, name), companion: Defn.Object))
          if isSealed(mods) =>
        val oldTemplStats = companion.templ.stats.getOrElse(Nil)
        val subTypes = oldTemplStats.collect {
          case t: Defn.Class if inherits(name)(t) => t
        }
        val newStats = mkCoproductGeneric(name, subTypes) +: oldTemplStats
        val newCompanion =
          companion.copy(templ = companion.templ.copy(stats = Some(newStats)))
        Term.Block(Seq(t, newCompanion))
      // Plain class with companion object.
      case Term.Block(
          Seq(cls @ Defn.Class(_, name, _, ctor, _),
              companion: Defn.Object)) =>
        val newStats =
          mkHListGeneric(name, ctor.paramss) +:
            companion.templ.stats.getOrElse(Nil)
        val newCompanion =
          companion.copy(templ = companion.templ.copy(stats = Some(newStats)))
        Term.Block(Seq(cls, newCompanion))
      // Plain class without companion object.
      case cls @ Defn.Class(_, name, _, ctor, _) =>
        val companion =
          q"object ${Term.Name(name.value)} { ${mkHListGeneric(name, ctor.paramss)} } "
        Term.Block(Seq(cls, companion))
      case cls @ Defn.Class(_, name, _, ctor, _) =>
        val companion =
          q"object ${Term.Name(name.value)} { ${mkHListGeneric(name, ctor.paramss)} } "
        Term.Block(Seq(cls, companion))
      case _ =>
        println(defn.structure)
        abort("@generic must annotate a class or a sealed trait/class.")
    }
  }
}

---
id: trees
sidebar_label: Guide
title: Trees Guide
---

A core functionality of Scalameta is syntax trees, which enable you to read,
analyze, transform and generate Scala programs at a high level of abstraction.
In this guide, you will learn how to

- parse source code into syntax trees
- construct new syntax trees
- pattern match syntax trees
- traverse syntax trees
- transform syntax trees

This guide assumes you have basic knowledge of programming with Scala. Let's get
started!

## Installation

Add a dependency to Scalameta in your build to get started. Scalameta supports
Scala 2.11, Scala 2.12, Scala.js and Scala Native.

### sbt

```scala
// build.sbt
libraryDependencies += "org.scalameta" %% "scalameta" % "@VERSION@"

// For Scala.js, Scala Native
libraryDependencies += "org.scalameta" %%% "scalameta" % "@VERSION@"
```

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.scalameta/scalameta_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.scalameta/scalameta_2.12)

All code examples assume you have the following import

```scala mdoc:silent
import scala.meta._
```

```scala mdoc:passthrough
import scalafix.v1._
```

### Ammonite REPL

A great way to experiment with Scalameta is to use the
[Ammonite REPL](http://ammonite.io/#Ammonite-REPL).

```scala
// Ammonite REPL
import $ivy.`org.scalameta::scalameta:@VERSION@`, scala.meta._
```

### Scastie

You can try out Scalameta online with the [Scastie playground](scastie.md).

## What is a syntax tree?

Syntax trees are a representation of source code that makes it easier to
programmatically analyze programs. Scalameta has syntax trees that represent
Scala programs.

![](assets/img/tree.svg)

Scalameta trees are **lossless**, meaning that they represent Scala programs in
sufficient details to go from text to trees and vice-versa without significant
loss of details. Lossless syntax trees are great for fine-grained analysis of
source code, which is useful for a range of applications including formatting,
refactoring, linting and documentation tools

## Parse trees

Scalameta comes with a parser to produce syntax trees from Scala source code.
You can parse trees from a variety of sources into different kinds of tree
nodes.

### From strings

The simplest way to parse source code is from a string. As long as you have
`import scala.meta._` in your scope, you can use the `parse[Source]` extension
method

```scala mdoc:silent
val program = """object Main extends App { print("Hello!") }"""
val tree = program.parse[Source].get
```

Once parsed, you can print the tree back into its original source code

```scala mdoc
println(tree.syntax)
```

The problem with parsing from strings it that error messages don't include a
filename

```scala mdoc
println(
  "object Main {".parse[Source]
)
```

To make error messages more helpful it's recommended to always use virtual files
when possible.

### From files

To parse a file into a tree it's recommended to first read the file contents
into a string and then construct a virtual file

```scala mdoc:silent
val path = java.nio.file.Paths.get("docs", "example.scala")
val bytes = java.nio.file.Files.readAllBytes(path)
val text = new String(bytes, "UTF-8")
val input = Input.VirtualFile(path.toString, text)
val exampleTree = input.parse[Source].get
```

```scala mdoc
print(exampleTree.syntax)
```

The difference between `text.parse[Source]` and `input.parse[Source]` is that
the filename appear in error messages for `Input.VirtualFile`.

```scala mdoc
println(
  Input.VirtualFile("example.scala", "object Main {").parse[Source]
)
```

### From expressions

To parse a simple expressions such as `a + b` use `parse[Stat]` The name `Stat`
stands for "statement".

```scala mdoc
println("a + b".parse[Stat].get.structure)
```

If we try to parse an expression with `parse[Source]` we get an error because
`a + b` is not valid at the top-level for Scala programs

```scala mdoc
println("a + b".parse[Source])
```

The same solution can be used to parse other tree nodes such as types

```scala mdoc
println("A with B".parse[Type].get.structure)
```

If we use `parse[Stat]` to parse types we get an error

```scala mdoc
println("A with B".parse[Stat])
```

### From programs with multiple top-level statements

To parse programs with multiple top-level statements such as `build.sbt` files
or Ammonite scripts we use the `Sbt1` dialect. By default, we get an error when
using `parse[Source]`.

```scala mdoc:silent
val buildSbt = """
val core = project
val cli = project.dependsOn(core)
"""
```

```scala mdoc
println(buildSbt.parse[Source])
```

This error happens because vals are not allowed as top-level statements in
normal Scala programs. To fix this problem, wrap the input with `dialects.Sbt1`

```scala mdoc
println(dialects.Sbt1(buildSbt).parse[Source].get.stats)
```

The same solution works for virtual files

```scala mdoc
println(
  dialects.Sbt1(
    Input.VirtualFile("build.sbt", buildSbt)
  ).parse[Source].get.stats
)
```

The difference between `dialects.Sbt1(input)` and `parse[Stat]` is that
`parse[Stat]` does not allow multiple top-level statements

```scala mdoc
println(buildSbt.parse[Stat])
```

Note that `dialects.Sbt1` does not accept programs with package declarations

```scala mdoc
println(
  dialects.Sbt1("package library; object Main").parse[Source]
)
```

## Construct trees

Sometimes we need to dynamically construct syntax trees instead of parsing them
from source code. There are two primary ways to construct trees: normal
constructors and quasiquotes.

### With normal constructors

Normal tree constructors as plain functions

```scala mdoc
println(Term.Apply(Term.Name("function"), List(Term.Name("argument"))))
```

Although normal constructors are verbose, they give most flexibility when
constructing trees.

To learn tree node names you can use `.structure` on existing tree nodes

```scala mdoc
println("function(argument)".parse[Stat].get.structure)
```

The output of structure is safe to copy-past into programs.

Another good way to learn the structure of trees is
[AST Explorer](http://astexplorer.net/#/gist/ec56167ffafb20cbd8d68f24a37043a9/97da19c8212688ceb232708b67228e3839dadc7c).

### With quasiquotes

Quasiquotes are macro interpolators that expand at compile-time into normal
constructor calls

```scala mdoc
println(q"function(argument)".structure)
```

You can write multiline quasiquotes to construct large programs

```scala mdoc
println(
  q"""
  object Example extends App {
    println(42)
  }
  """.structure
)
```

> It's important to keep in mind that quasiquotes expand at compile-time into
> the same program as if you had written normal constructors by hand. This means
> for example that formatting details or comments are not preserved

```scala mdoc
println(q"function  (    argument   ) // comment")
```

Quasiquotes can be composed together with dollar splices `..$`

```scala mdoc
val arguments = List(q"arg1", q"arg2")
println(q"function(..$arguments)")
```

To construct curried argument lists use triple dot splices `...$`

```scala mdoc
val arguments2 = List(q"arg3", q"arg4")
val allArguments = List(arguments, arguments2)
println(q"function(...$allArguments)")
```

To learn more about quasiquotes, consult the [quasiquote spec](quasiquotes.md).

## Pattern match trees

### With normal constructors

### With quasiquotes

## Compare trees for equality

## Traverse trees

### Simple traversals

### Custom traversals

## Transform trees

### Simple transformations

### Custom transformations

<!-- You can parse source code from a string

```scala mdoc:silent
val programString = "a + b"
val tree = programString.parse[Term].get
```

You can also parse source code from a file

```scala mdoc
import java.nio.file._
val programFile = Files.createTempFile("scalameta", "program.scala")
Files.write(programFile, "a + b".getBytes())
val treeFromFile = programFile.parse[Term].get
```


Use `.traverse` to visit every tree node without collecting values, similarly to
`.foreach`

```scala mdoc
tree.traverse {
  case node =>
    println(s"${node.productPrefix}: $node")
}
```

Use `.collect` to visit every tree node and collect a computed value for
intersting tree nodes

```scala mdoc
tree.collect {
  case Term.Name(name) =>
    name
}
```

Use `.transform` to change the shape of the tree

```scala mdoc
tree.transform {
  case Term.Name(name) =>
    Term.Name(name.toUpperCase)
}.toString
```

The methods `traverse`, `collect` and `transform` don't allow you to customize
the recursion of the tree traversal. For more fine-grained control you can
implement custom `Traverser` and `Transformer` instances.

A `Traverser` implements a `Tree => Unit` function

```scala mdoc
new Traverser {
  override def apply(tree: Tree): Unit = tree match {
    case infix: Term.ApplyInfix =>
      println(infix.op)
    case _ =>
      super.apply(tree)
  }
}.apply(tree)
```

A `Transformer` implements a `Tree => Tree` function

```scala mdoc
new Transformer {
  override def apply(tree: Tree): Tree = tree match {
    case Term.Name(name) =>
      Term.Name(name.toUpperCase)
    case Term.ApplyInfix(lhs, op, targs, args) =>
      Term.ApplyInfix(
          this.apply(lhs).asInstanceOf[Term],
          op, targs, args
      )
    case _ =>
      super.apply(tree)
  }
}.apply(tree).toString
```


Quasiquotes are a simple way to construct tree nodes

```scala mdoc
val quasiquote = q"a + b"
quasiquote.collect { case Term.Name(name) => name }
```

Quasi-quotes expand at compile-time into direct calls to tree constructors. The
quasi-quote above is equivalent to the manually written `Term.Apply(...)`
expression below

```scala mdoc
val noQuasiquote = Term.ApplyInfix(Term.Name("a"), Term.Name("+"), List(), List(Term.Name("b")))
noQuasiquote.toString
```


Trees use reference equality by default. This may seem counter intuitive at -->

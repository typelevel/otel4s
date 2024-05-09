/*
 * Copyright 2022 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.otel4s.trace.experimental

import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly
import scala.annotation.unused
import scala.reflect.macros.blackbox

/** Wraps the body of an annotated method or variable into the span.
  *
  * By default, the span name will be `className.methodName`, unless a name is
  * provided as an argument.
  *
  * {{{
  * class Service[F[_]: Tracer](db: Database[F]) {
  *   @withSpan
  *   def findUser(@spanAttribute userId: Long): F[User] =
  *     db.findUser(id)
  * }
  *
  *  // expands into
  *
  * class Service[F[_]: Tracer](db: Database[F]) {
  *   def findUser(userId: Long): F[User] =
  *     Tracer[F]
  *       .span("Service.findUser", Attribute("userId", userId))
  *       .surround(db.findUser(id))
  * }
  * }}}
  *
  * @param name
  *   the custom name of the span. If not specified, the span name will be
  *   `className.methodName`
  *
  * @param debug
  *   whether to print the generated code to the console
  */
@compileTimeOnly("enable macro to expand macro annotations")
class withSpan(
    @unused name: String = "",
    @unused debug: Boolean = false
) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro withSpanMacro.impl
}

object withSpanMacro {

  def impl(c: blackbox.Context)(annottees: c.Expr[Any]*): c.Expr[c.Tree] = {
    import c.universe._

    def abort(message: String) =
      c.abort(c.enclosingPosition, s"@withSpan macro: $message")

    def ensureImplicitExist(tpe: Tree, reason: Throwable => String): c.Tree =
      try {
        val t = c.typecheck(tpe).tpe
        c.inferImplicitValue(t)
      } catch {
        case e: Throwable => abort(reason(e))
      }

    def resolveEffectType(tpt: Tree): Tree =
      tpt match {
        case tq"$tpe[${_}]" => tpe
        case _              => abort("cannot resolve the type of the effect")
      }

    val macroName: Tree =
      c.prefix.tree match {
        case Apply(Select(New(name), _), _) => name
        case _ => c.abort(c.enclosingPosition, "Unexpected macro application")
      }

    val (nameParam, debug) = c.prefix.tree match {
      case q"new ${`macroName`}(..$args)" =>
        (
          args.headOption
            .collect { case Literal(value) => Left(value) }
            .orElse(
              args.collectFirst { case q"name = $value" => Right(value) }
            ),
          args.collectFirst { case q"debug = true" => }.isDefined
        )
      case _ =>
        (None, false)
    }

    def spanName(definition: ValOrDefDef): Tree =
      nameParam match {
        case Some(Left(const)) =>
          val literal = Literal(const)
          q"$literal"

        case Some(Right(tree)) =>
          tree

        case None =>
          @annotation.tailrec
          def resolveEnclosingName(symbol: Symbol, output: String): String =
            if (symbol.isClass) {
              val className = symbol.name.toString
              if (className.startsWith("$anon"))
                resolveEnclosingName(symbol.owner, "$anon" + "." + output)
              else
                className + "." + output
            } else {
              resolveEnclosingName(symbol.owner, output)
            }

          val prefix = resolveEnclosingName(c.internal.enclosingOwner, "")

          val literal = Literal(
            Constant(prefix + definition.name.decodedName.toString)
          )
          q"$literal"
      }

    def expandDef(defDef: DefDef): Tree = {
      val effectType = resolveEffectType(defDef.tpt)
      val name: Tree = spanName(defDef)

      val attributes = defDef.vparamss.flatten.flatMap {
        case ValDef(mods, name, tpt, _) =>
          mods.annotations.flatMap { annotation =>
            val typed = c.typecheck(annotation)
            if (
              typed.tpe.typeSymbol.fullName == "org.typelevel.otel4s.trace.experimental.spanAttribute"
            ) {
              val keyArg = typed match {
                case q"new ${_}(${keyArg})" =>
                  keyArg
                case _ =>
                  abort("unknown structure of the @spanAttribute annotation.")
              }

              val key = keyArg match {
                // the key param is not specified, use param name as a key
                case Select(_, TermName("$lessinit$greater$default$1")) =>
                  Literal(Constant(name.decodedName.toString))

                // type of the AttributeKey must match the parameter type
                case q"$_[$tpe]($_)(..$_)" =>
                  val keyType = c.untypecheck(tpe)
                  val argType = c.typecheck(tpt, c.TYPEmode)

                  if (!keyType.equalsStructure(argType)) {
                    abort(
                      s"the argument [${name.toString}] type [$argType] does not match the type of the attribute [$keyType]."
                    )
                  }
                  keyArg

                case _ =>
                  keyArg
              }

              ensureImplicitExist(
                q"_root_.org.typelevel.otel4s.AttributeKey.KeySelect[$tpt]",
                e =>
                  s"the argument [${name.decodedName}] cannot be used as an attribute. The type [$tpt] is not supported.${e.getMessage}"
              )

              List(
                q"_root_.org.typelevel.otel4s.Attribute($key, $name)"
              )
            } else {
              Nil
            }
          }

        case _ =>
          Nil
      }

      val body =
        q"""
            _root_.org.typelevel.otel4s.trace.Tracer[$effectType].span($name, ..$attributes).surround {
              ${defDef.rhs}
            }
         """

      DefDef(
        defDef.mods,
        defDef.name,
        defDef.tparams,
        defDef.vparamss,
        defDef.tpt,
        body
      )
    }

    def expandVal(valDef: ValDef): Tree = {
      val effectType = resolveEffectType(valDef.tpt)
      val name: Tree = spanName(valDef)

      val body =
        q"""
            _root_.org.typelevel.otel4s.trace.Tracer[$effectType].span($name).surround {
              ${valDef.rhs}
            }
         """

      ValDef(valDef.mods, valDef.name, valDef.tpt, body)
    }

    val result = annottees.map(_.tree).toList match {
      case List(defDef: DefDef) =>
        expandDef(defDef)

      case List(valDef: ValDef) =>
        expandVal(valDef)

      case _ =>
        abort(
          "unsupported definition. Only `def` and `val` with explicit result types and defined bodies are supported."
        )
    }

    if (debug) {
      val at: String =
        if (c.enclosingPosition == NoPosition)
          ""
        else
          s"at ${c.enclosingPosition.source.file.name}:${c.enclosingPosition.line} "

      scala.Predef.println(s"@withSpan $at- expanded into:\n${result}")
    }

    c.Expr(result)
  }

}

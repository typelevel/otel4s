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

import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.trace.Tracer

import scala.annotation.MacroAnnotation
import scala.annotation.compileTimeOnly
import scala.annotation.experimental
import scala.annotation.unused
import scala.quoted.*

/** Wraps the body of an annotated method or variable into the span.
  *
  * By default, the span name will be `className.methodName`, unless a name is
  * provided as an argument.
  *
  * {{{
  * @scala.annotation.experimental
  * class Service[F[_]: Tracer](db: Database[F]) {
  *   @withSpan
  *   def findUser(@spanAttribute userId: Long): F[User] =
  *     db.findUser(id)
  * }
  *
  *  // expands into
  *
  * @scala.annotation.experimental
  * class Service[F[_]: Tracer](db: Database[F]) {
  *   def findUser(userId: Long): F[User] =
  *     Tracer[F]
  *       .span("Service.findUser", Attribute("userId", id))
  *       .surround(db.findUser(id))
  * }
  * }}}
  *
  * @note
  *   macro remains experimental in Scala 3. Therefore, the enclosing class must
  *   be annotated with `@scala.annotation.experimental`.
  *
  * @param name
  *   the custom name of the span. If not specified, the span name will be
  *   `className.methodName`
  *
  * @param debug
  *   whether to print the generated code to the console
  */
// scalafmt: { maxColumn = 120 }
@compileTimeOnly("enable macro to expand macro annotations")
@experimental
class withSpan(
    @unused name: String = "",
    @unused debug: Boolean = false
) extends MacroAnnotation {

  override def transform(using
      quotes: Quotes
  )(tree: quotes.reflect.Definition): List[quotes.reflect.Definition] = {
    import quotes.reflect._

    def abort(message: String) =
      report.errorAndAbort(s"@withSpan macro: $message", tree.pos)

    def resolveEffectType(tpe: TypeRepr): (TypeRepr, TypeRepr) =
      tpe match {
        case AppliedType(effect, inner :: Nil) =>
          (effect, inner)

        case _ =>
          abort("unknown structure of the val.")
      }

    def resolveTracer(effect: TypeRepr): Term = {
      val tracerType = TypeRepr.of[Tracer[_]] match {
        case AppliedType(t, _) =>
          AppliedType(t, List(effect))
        case _ =>
          abort("cannot determine the effect type.")
      }

      Implicits.search(tracerType) match {
        case iss: ImplicitSearchSuccess =>
          iss.tree
        case isf: ImplicitSearchFailure =>
          abort(s"cannot find Tracer[${effect.show}] in the implicit scope.")
      }
    }

    val (nameParam, debug) = {
      val defaultArg = "$lessinit$greater$default$"

      def argValue[A](argTree: Tree, name: String)(pf: PartialFunction[Constant, A]): Option[A] =
        argTree match {
          case Select(_, n) if n.startsWith(defaultArg) => None
          case Literal(pf(a))                           => Some(a)
          case NamedArg(_, Literal(pf(a)))              => Some(a)
          case other                                    => abort(s"unknown structure of the '$name' argument: $other.")
        }

      tree.symbol.getAnnotation(TypeRepr.of[withSpan].typeSymbol) match {
        case Some(Apply(_, nameArg :: debugArg :: Nil)) =>
          val name = argValue(nameArg, "name") { case StringConstant(const) =>
            const
          }

          val debug = argValue(debugArg, "debug") { case BooleanConstant(const) =>
            const
          }

          (name, debug.getOrElse(false))

        case Some(other) =>
          abort(s"unknown structure of the @withSpan annotation: $other.")

        case None =>
          abort("the @withSpan annotation is missing.")
      }
    }

    def wrap(
        tracer: Term,
        resultType: TypeRepr,
        body: Term,
        definitionName: String,
        attributes: Seq[Expr[Attribute[_]]]
    ): Term = {
      val nameArg = {
        @annotation.tailrec
        def resolveEnclosingName(symbol: Symbol, output: String): String =
          if (symbol.isClassDef) {
            val className = symbol.name
            if (className.startsWith("$anon"))
              resolveEnclosingName(symbol.owner, "$anon" + "." + output)
            else
              className + "." + output
          } else {
            resolveEnclosingName(symbol.owner, output)
          }

        val name = nameParam.getOrElse {
          val prefix = resolveEnclosingName(Symbol.spliceOwner, "")
          prefix + definitionName
        }

        Literal(StringConstant(name))
      }

      val attributesArg = Expr.ofSeq(attributes).asTerm
      val args = List(nameArg, attributesArg)

      val spanOps = Select.overloaded(tracer, "span", Nil, args)

      Select
        .unique(spanOps, "surround")
        .appliedToType(resultType)
        .appliedTo(body)
    }

    def expandDef(defDef: DefDef): quotes.reflect.Definition = {
      val (effect, inner) = resolveEffectType(defDef.returnTpt.tpe)
      val tracer = resolveTracer(effect)

      val params: List[ValDef] = defDef.paramss.flatMap {
        case TypeParamClause(_)      => Nil
        case TermParamClause(params) => params
      }

      val attributes: List[Expr[Attribute[_]]] =
        params.flatMap { case vd @ ValDef(name, tpt, _) =>
          val sym: Symbol = vd.symbol

          def verifyTypesMatch(argType: TypeTree) =
            if (argType.tpe != tpt.tpe)
              abort(
                s"the argument [$name] type [${tpt.show}] does not match the type of the attribute [${argType.show}]."
              )

          sym.getAnnotation(TypeRepr.of[spanAttribute].typeSymbol) match {
            case Some(annotation) =>
              tpt.tpe.asType match {
                case '[f] =>
                  val keySelectExpr = Expr
                    .summon[AttributeKey.KeySelect[f]]
                    .getOrElse(
                      abort(
                        s"the argument [$name] cannot be used as an attribute. The type [${tpt.show}] is not supported."
                      )
                    )

                  val argExpr = Ident(sym.termRef).asExprOf[f]

                  val expr: Expr[Attribute[f]] = annotation match {
                    case Apply(_, List(Select(_, "$lessinit$greater$default$1"))) =>
                      '{ Attribute(${ Expr(name) }, $argExpr)($keySelectExpr) }

                    case Apply(_, List(literal @ Literal(StringConstant(const)))) =>
                      '{ Attribute(${ Expr(const) }, $argExpr)($keySelectExpr) }

                    case Apply(_, List(NamedArg(_, literal @ Literal(StringConstant(const))))) =>
                      '{ Attribute(${ Expr(const) }, $argExpr)($keySelectExpr) }

                    case Apply(_, List(apply @ Apply(Apply(TypeApply(_, List(typeArg)), _), _))) =>
                      verifyTypesMatch(typeArg)
                      '{ Attribute(${ apply.asExprOf[AttributeKey[f]] }, $argExpr) }

                    case Apply(_, List(NamedArg(_, apply @ Apply(Apply(TypeApply(_, List(typeArg)), _), _)))) =>
                      verifyTypesMatch(typeArg)
                      '{ Attribute(${ apply.asExprOf[AttributeKey[f]] }, $argExpr) }

                    case other =>
                      abort(s"the argument [$name] has unsupported tree: ${other}.")
                  }

                  List(expr)
              }

            case None =>
              Nil
          }
        }

      val body = wrap(tracer, inner, defDef.rhs.get, defDef.name, attributes)

      DefDef.copy(tree)(defDef.name, defDef.paramss, defDef.returnTpt, Some(body))
    }

    def expandVal(valDef: ValDef): quotes.reflect.Definition = {
      val (effect, inner) = resolveEffectType(valDef.tpt.tpe)
      val tracer = resolveTracer(effect)

      val body = wrap(tracer, inner, valDef.rhs.get, valDef.name, Nil)

      ValDef.copy(tree)(valDef.name, valDef.tpt, Some(body))
    }

    val result = tree match {
      case defDef @ DefDef(name, params, returnType, Some(rhs)) =>
        expandDef(defDef)

      case valDef @ ValDef(name, returnType, Some(rhs)) =>
        expandVal(valDef)

      case _ =>
        abort(
          "unsupported definition. Only `def` and `val` with explicit result types and defined bodies are supported."
        )
    }

    if (debug) {
      val at = tree.symbol.pos
        .map(pos => s"at ${pos.sourceFile.name}:${pos.startLine} ")
        .getOrElse("")

      scala.Predef.println(s"@withSpan $at- expanded into:\n${result.show}")
    }

    List(result)
  }

}

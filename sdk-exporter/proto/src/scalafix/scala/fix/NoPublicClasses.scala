package fix

import scalafix.v1._
import scala.meta._

class NoPublicClasses extends SyntacticRule("NoPublicClasses") {
  override def description: String = "Disallows public classes and objects in the module"

  override def fix(implicit doc: SyntacticDocument): Patch = {
    doc.tree.collect {
      case defn: Defn.Class if !defn.mods.exists(_.is[Mod.Private]) && defn.parent.exists(_.is[Pkg.Body]) =>
        Patch.lint(
          Diagnostic(
            id = "NoPublicClasses",
            message = "Public classes are not allowed in this module.",
            position = defn.pos
          )
        )

      case defn: Defn.Object if !defn.mods.exists(_.is[Mod.Private]) && defn.parent.exists(_.is[Pkg.Body]) =>
        Patch.lint(
          Diagnostic(
            id = "NoPublicClasses",
            message = "Public objects are not allowed in this module.",
            position = defn.pos
          )
        )
    }.asPatch
  }
}

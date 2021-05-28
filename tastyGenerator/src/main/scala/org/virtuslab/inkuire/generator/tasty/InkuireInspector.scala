package org.virtuslab.inkuire.generator.tasty

import scala.quoted.*
import scala.tasty.inspector.*

import collection.JavaConverters._
import java.lang.{Character => JCharacter}

import org.virtuslab.inkuire.engine.common.model.InkuireDb
import org.virtuslab.inkuire.engine.common.{model => Inkuire}

object InkuireDB {
  var db: InkuireDb = InkuireDb.empty
}

class InkuireInspector extends Inspector {
  def inspect(using Quotes)(tastys: List[Tasty[quotes.type]]): Unit = {
    import quotes.reflect.*

    def paramsForClass(classDef: ClassDef, vars: Set[String], isVariable: Boolean): Seq[Inkuire.Variance] =
      classDef.getTypeParams.map(mkTypeArgumentInkuire(_, vars, isVariable))

    extension (c: ClassDef)
      def getTypeParams: List[TypeDef] =
        c.body.collect { case targ: TypeDef => targ  }.filter(_.symbol.isTypeParam)

    extension (tpeTree: Tree)
      def asInkuire(vars: Set[String], isVariable: Boolean): Inkuire.Type =
        tpeTree match
          case TypeBoundsTree(low, high) => inner(low.tpe, vars) //TODO
          case tpeTree: Applied =>
            inner(tpeTree.tpe, vars).copy(
              params = tpeTree.args.map(p => Inkuire.Invariance(p.asInkuire(vars, isVariable))) //TODO check variance
            )
          case tpeTree: TypeTree => inner(tpeTree.tpe, vars)
          case term:  Term => inner(term.tpe, vars)
          case classDef: ClassDef => mkTypeFromClassDef(classDef, vars, isVariable)

    def mkTypeFromClassDef(classDef: ClassDef, vars: Set[String], isVariable: Boolean): Inkuire.Type = {
      Inkuire.Type(
        name = Inkuire.TypeName(classDef.name),
        itid = classDef.symbol.itid,
        params = paramsForClass(classDef, vars, isVariable)
      )
    }

    extension (sym: Symbol)
      def extendedSymbol: Option[ValDef] =
        Option.when(sym.flags.is(Flags.ExtensionMethod)){
          val termParamss = sym.tree.asInstanceOf[DefDef].termParamss
          if !sym.name.endsWith(":") || termParamss.size == 1 then termParamss(0).params(0)
          else termParamss(1).params(0)
        }

      def nonExtensionParamLists: List[TermParamClause] =
        import reflect.*
        val method = sym.tree.asInstanceOf[DefDef]
        if sym.flags.is(Flags.ExtensionMethod) then
          val params = method.termParamss
          if !sym.name.endsWith(":") || params.size == 1 then params.tail
          else params.head :: params.tail.drop(1)
        else method.termParamss

    extension (s: Symbol)
      def normalizedName: String =
        if s.flags.is(Flags.Module) then s.name.stripSuffix("$") else s.name

    extension (symbol: Symbol)
      def itid: Option[Inkuire.ITID] = Some(Inkuire.ITID(symbol.fullName, isParsed = false))
    
    extension (tpe: TypeRepr)
      def asInkuireType(vars: Set[String]): Inkuire.Type = inner(tpe, vars)

    def mkTypeArgumentInkuire(argument: TypeDef, vars: Set[String] = Set.empty, isVariable: Boolean = false): Inkuire.Variance =
      val name = argument.symbol.normalizedName
      val normalizedName = if name.matches("_\\$\\d*") then "_" else name
      val t = Inkuire.Type(
        name = Inkuire.TypeName(normalizedName),
        itid = argument.symbol.itid,
        isVariable = vars.contains(normalizedName) || isVariable,
        params = Seq.empty //TODO in future arities of params will be needed
      )
      if argument.symbol.flags.is(Flags.Covariant) then Inkuire.Covariance(t)
      else if argument.symbol.flags.is(Flags.Contravariant) then Inkuire.Contravariance(t)
      else Inkuire.Invariance(t)

    def isRepeatedAnnotation(term: Term) =
      term.tpe match
        case t: TypeRef => t.name == "Repeated" && t.qualifier.match
          case ThisType(tref: TypeRef) if tref.name == "internal" => true
          case _ => false
        case _ => false

    def isRepeated(typeRepr: TypeRepr) =
      typeRepr match
        case t: TypeRef => t.name == "<repeated>" && t.qualifier.match
          case ThisType(tref: TypeRef) if tref.name == "scala" => true
          case _ => false
        case _ => false

    def isIdentifierPart(c: Char): Boolean =
      (c == '$') || JCharacter.isUnicodeIdentifierPart(c)

    extension (t: TypeRepr)
      def isTupleType: Boolean =
        t.typeSymbol.name.matches("""Tuple.*""")

    def inner(tp: TypeRepr, vars: Set[String]): Inkuire.Type = tp match
      case OrType(left, right) => inner(left, vars) //TODO for future
      case AndType(left, right) => inner(left, vars) //TODO for future
      case ByNameType(tpe) => inner(tpe, vars)
      case ConstantType(constant) =>
        ??? //TODO for future, kinda
      case ThisType(tpe) => inner(tpe, vars)
      case AnnotatedType(AppliedType(_, Seq(tpe)), annotation) if isRepeatedAnnotation(annotation) =>
        inner(tpe, vars) //TODO for future
      case AppliedType(repeatedClass, Seq(tpe)) if isRepeated(repeatedClass) =>
        inner(tpe, vars) //TODO for future
      case AnnotatedType(tpe, _) =>
        inner(tpe, vars)
      case tl @ TypeLambda(params, paramBounds, resType) =>
        if resType.typeSymbol.name == "Seq" then println(resType)
        inner(resType, vars) //TODO for future
      case r: Refinement =>
        inner(r.info, vars) //TODO for future
      case t @ AppliedType(tpe, typeList) =>
        if !t.typeSymbol.name.forall(isIdentifierPart) && typeList.size == 2 then
          inner(typeList.head, vars)
        else if t.isFunctionType then
          typeList match
            case Nil =>
              ??? //Not possible right?
            case args =>
              val name = s"Function${args.size-1}"
              Inkuire.Type(
                name = Inkuire.TypeName(name),
                params = args.init.map(p => Inkuire.Contravariance(inner(p, vars))) :+ Inkuire.Covariance(inner(args.last, vars)),
                itid = Some(Inkuire.ITID(s"scala.${name}", isParsed = false))
              )
        else if t.isTupleType then
          typeList match
            case Nil =>
              ??? //TODO Not possible right?
            case args =>
              val name = s"Tuple${args.size}"
              Inkuire.Type(
                name = Inkuire.TypeName(name),
                params = args.map(p => Inkuire.Covariance(inner(p, vars))),
                itid = Some(Inkuire.ITID(s"scala.${name}", isParsed = false))
              )
        else
          inner(tpe, vars).copy(
            params = typeList.map(p => Inkuire.Invariance(inner(p, vars)))
          ) //TODO check if it's ok (Having resolver should mean that variance here isn't meaningful)
      case tp: TypeRef =>
        Inkuire.Type(
          name = Inkuire.TypeName(tp.name),
          itid = tp.typeSymbol.itid,
          params = Seq.empty,
          isVariable = vars.contains(tp.name)
        )
      case tr @ TermRef(qual, typeName) =>
        inner(qual, vars)
      case TypeBounds(low, hi) =>
        inner(low, vars) //TODO for future
      case NoPrefix() =>
        ??? //TODO not possible right?
      case MatchType(bond, sc, cases) =>
        inner(sc, vars)
      case ParamRef(TypeLambda(names, _, resType), i) =>
        Inkuire.Type(
          name = Inkuire.TypeName(names(i)),
          itid = Some(Inkuire.ITID(s"external-itid-${names(i)}", isParsed = false)), //TODO check if it's possible to get the actual ITID(DRI)
          isVariable = true
        )
      case ParamRef(m: MethodType, i) =>
        inner(m.paramTypes(i), vars)
      case RecursiveType(tp) =>
        inner(tp, vars)

    def visitTree(tree: Tree): Unit = 
      try {
        tree match {
          case p @ PackageClause(_, list) =>
            list.foreach(visitTree)

          case classDef @ ClassDef(_, _, _, _, list) =>
            val classType = classDef.asInkuire(Set.empty, true)
            val variableNames = classType.params.map(_.typ.name.name).toSet

            val parents = classDef.parents.map(_.asInkuire(variableNames, false))

            val isModule = classDef.symbol.flags.is(Flags.Module)

            if !isModule then InkuireDB.db = InkuireDB.db.copy(types = InkuireDB.db.types.updated(classType.itid.get, (classType, parents)))

            val methods = classDef.symbol.declaredMethods.collect {
              case methodSymbol: Symbol =>
              val defdef = methodSymbol.tree.asInstanceOf[DefDef]
              val methodVars = defdef.paramss.flatMap(_.params).collect {
                  case TypeDef(name, _) => name
                }
                val vars = variableNames ++ methodVars
                val receiver: Option[Inkuire.Type] = Some(classType).filter(_ => !isModule).orElse(methodSymbol.extendedSymbol.map(_.asInkuire(vars, false)))
                Inkuire.ExternalSignature(
                  signature = Inkuire.Signature(
                    receiver = receiver,
                    arguments = methodSymbol.nonExtensionParamLists.flatMap(_.params).collect {
                      case ValDef(_, tpe, _) => tpe.asInkuire(vars, false)
                    },
                    result = defdef.returnTpt.asInkuire(vars, false),
                    context = Inkuire.SignatureContext(
                      vars = vars.toSet,
                      constraints = Map.empty //TODO for future
                    )
                  ),
                  name = methodSymbol.name,
                  packageName = methodSymbol.maybeOwner.fullName,
                  uri = ""
                )
            }

            InkuireDB.db = InkuireDB.db.copy(functions = InkuireDB.db.functions ++ methods)

          case TypeDef(_, rhs) =>
            visitTree(rhs)

          case d @ DefDef(name, _, _, rhs) =>
            println(d.name)

          case Block(list, term) =>
            list.foreach(visitTree)

          case x =>
        }
      } catch {
        case e =>
      }

    tastys.foreach { tasty =>
      visitTree(tasty.ast)
    }
  }

}

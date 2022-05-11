package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.impl.model._
import org.virtuslab.inkuire.engine.impl.utils.Monoid

import com.softwaremill.quicklens._

// TODO(kπ) technically it would be better to have a different type for type declarations -> types: Map[ITID, (Declaration, Seq[Type])]
case class InkuireDb(
  functions:           Seq[AnnotatedSignature],
  types:               Map[ITID, (Type, Seq[Type])],
  implicitConversions: Seq[(TypeLike, Type)],
  typeAliases:         Map[ITID, TypeLike]
) {
  private def collectTypesWithin(t: TypeLike): Seq[Type] = t match {
    case tpe: Type =>
      if (tpe.isVariable)
        tpe.params.map(_.typ).flatMap(collectTypesWithin)
      else
        tpe +: tpe.params.map(_.typ).flatMap(collectTypesWithin)
    case AndType(tpe1, tpe2) =>
      collectTypesWithin(tpe1) ++ collectTypesWithin(tpe2)
    case OrType(tpe1, tpe2) =>
      collectTypesWithin(tpe1) ++ collectTypesWithin(tpe2)
    case TypeLambda(_, resultTpe) =>
      collectTypesWithin(resultTpe)
  }

  private def normalizeTopLevelType(t: Type): Type = {
    t.copy(
      params = t.params
        .map(_.typ)
        .zipWithIndex
        .map { case (t, i) => Invariance(normalizeType(t, i)) }
    )
  }

  private def normalizeType(t: TypeLike, idx: Int): TypeLike = t match {
    case t: Type =>
      t.copy(
        name = s"DUMMY_TYPE_VAR_$idx",
        isVariable = true,
        params = Seq.empty
      )
    case AndType(tpe1, tpe2) =>
      normalizeType(tpe1, idx)
    case OrType(tpe1, tpe2) =>
      normalizeType(tpe1, idx)
    case TypeLambda(args, resultTpe) =>
      Type(
        name = s"DUMMY_TYPE_VAR_$idx",
        isVariable = true,
        params = args.zipWithIndex.map { case (t, i) => Invariance(normalizeType(t, i)) }
      )
  }

  def withOrphanTypes: InkuireDb = {
    val types: Seq[TypeLike] =
      this.functions.flatMap(_.signature.typesWithVariances.map(_.typ)) ++
        this.types.toSeq.flatMap(_._2._2) ++
        this.implicitConversions.flatMap { case (tpe1, tpe2) => Seq(tpe1, tpe2) } ++
        this.typeAliases.toSeq.map(_._2)
    val allTypes: Set[Type] = types
      .flatMap(collectTypesWithin)
      .toSet
    val normalizedTypes: Set[Type] = allTypes
      .map(normalizeTopLevelType)
      .collect { case t: Type => t }
      .filter(t => t.itid.nonEmpty && !this.types.contains(t.itid.get))
    val orphansMap =
      normalizedTypes.map(t => t.itid.get -> (t, Seq.empty)).toMap
    this.modify(_.types).using(orphansMap ++ _)
  }
}

object InkuireDb {
  def empty: InkuireDb = InkuireDb(Seq.empty, Map.empty, Seq.empty, Map.empty)

  def combine(x: InkuireDb, y: InkuireDb): InkuireDb =
    InkuireDb(
      functions = (x.functions ++ y.functions).distinct,
      types = (x.types.toSeq ++ y.types.toSeq).distinct
        .groupBy(_._1)
        .view
        .mapValues { seqOfvalues =>
          seqOfvalues.tail.foldLeft(seqOfvalues.head._2) {
            case (acc, e) =>
              val (typ, parents)       = acc
              val (_, (_, newParents)) = e
              typ -> (parents ++ newParents)
          }
        }
        .toMap,
      implicitConversions = x.implicitConversions ++ y.implicitConversions,
      typeAliases = x.typeAliases ++ y.typeAliases
    )

  def combineAll(list: List[InkuireDb]): InkuireDb =
    list.foldLeft[InkuireDb](InkuireDb.empty) {
      case (acc, a) => InkuireDb.combine(acc, a)
    }

  implicit val inkuireDbMonoid: Monoid[InkuireDb] = new Monoid[InkuireDb] {
    override def mappend(x: InkuireDb, y: InkuireDb): InkuireDb = InkuireDb.combine(x, y)

    override def empty: InkuireDb = InkuireDb.empty
  }
}

package org.virtuslab.inkuire.engine.common.model

import cats.kernel.Monoid

//TODO technically it would be better to have a different type for type declarations -> types: Map[ITID, (Declaration, Seq[Type])]
case class InkuireDb(
  functions:           Seq[ExternalSignature],
  types:               Map[ITID, (Type, Seq[Type])],
  implicitConversions: Seq[(TypeLike, Type)],
  typeAliases:         Map[ITID, TypeLike]
)

object InkuireDb {
  implicit val inkuireDbMonoid: Monoid[InkuireDb] = new Monoid[InkuireDb] {
    override def combine(x: InkuireDb, y: InkuireDb): InkuireDb =
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

    override def empty: InkuireDb = InkuireDb(Seq.empty, Map.empty, Seq.empty, Map.empty)
  }
}

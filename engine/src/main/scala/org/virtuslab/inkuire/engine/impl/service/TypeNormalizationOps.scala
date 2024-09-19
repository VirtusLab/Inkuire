package org.virtuslab.inkuire.engine.impl.service

import org.virtuslab.inkuire.engine.impl.model._
import com.softwaremill.quicklens._

trait TypeNormalizationOps {
  def uncurryTypes(tpe: TypeLike): TypeLike = tpe match {
    case TypeLambda(args, t: Type) if args.zip(t.params).forall { case (a, p) => a == p.typ } =>
      uncurryTypes(t.modify(_.params).setTo(List.empty))
    case t: Type =>
      t.modify(_.params.each.typ).using(uncurryTypes)
    case t: OrType =>
      t.modifyAll(_.left, _.right).using(uncurryTypes)
    case t: AndType =>
      t.modifyAll(_.left, _.right).using(uncurryTypes)
    case t: TypeLambda =>
      t.modify(_.result).using(uncurryTypes)
  }

  def uncurrySignature(sgn: Signature): Signature =
    sgn.modifyAllTypes(uncurryTypes)
}

package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.api._
import org.virtuslab.inkuire.engine.common.model._

class TopLevelMatchQualityService(val db: InkuireDb) extends BaseMatchQualityService with MatchingOps {

  def matchQualityMetric(externalSignature: ExternalSignature, matching: Signature): Int =
    variancesMatchQualityMetric(
      externalSignature.signature.typesWithVariances,
      matching.typesWithVariances
    )

  def variancesMatchQualityMetric(typVariances: Seq[Variance], suprVariances: Seq[Variance]): Int =
    typVariances.zip(suprVariances).map { case (v1, v2) => varianceMatchQualityMetric(v1, v2) }.sum

  def varianceMatchQualityMetric(typVariance: Variance, suprVariance: Variance): Int =
    (typVariance, suprVariance) match {
      case (Covariance(typParam), Covariance(suprParam)) =>
        typeMatchQualityMetric(typParam, suprParam)
      case (Contravariance(typParam), Contravariance(suprParam)) =>
        typeMatchQualityMetric(suprParam, typParam)
      case (Invariance(typParam), Invariance(suprParam)) =>
        typeMatchQualityMetric(typParam, suprParam) + typeMatchQualityMetric(suprParam, typParam)
      case (v1, v2) => // Treating not matching variances as invariant
        val typParam  = v1.typ
        val suprParam = v2.typ
        typeMatchQualityMetric(typParam, suprParam) + typeMatchQualityMetric(suprParam, typParam)
    }

  /** Classes thet generally mean loss of some information */
  final val avoidThose: Set[TypeName] = Set(
    "Any",
    "Object",
    "AnyVal",
    "AnyRef",
    "Matchable",
    "Nothing"
  ).map(TypeName.apply)

  /** Matching constants */
  /** Matching constants */
  final val aLotCost              = 1000000
  final val losingInformationCost = 10000
  final val varToConcreteCost     = 200
  final val concreteToVarCost     = 5000
  final val andOrOrTypeCost       = 50
  final val dealiasCost           = 10
  final val subTypeCost           = 100
  final val typeLambdaCost        = 1
  final val varToVarCost          = 1
  final val equalCost             = 1

  val p = new ScalaExternalSignaturePrettifier

  /** Returns match quality metric for two typelikes
    * the lower the metric value, the better the match
    */
  def typeMatchQualityMetric(typ: TypeLike, supr: TypeLike): Int = {
    (typ, supr) match {
      case (t: Type, s: Type) if t.isStarProjection && s.isStarProjection =>
        varToVarCost
      case (t: Type, _) if t.isStarProjection =>
        varToConcreteCost
      case (_, s: Type) if s.isStarProjection =>
        varToConcreteCost
      case (AndType(left, right), supr) =>
        andOrOrTypeCost + (typeMatchQualityMetric(left, supr) min typeMatchQualityMetric(right, supr))
      case (typ, AndType(left, right)) =>
        andOrOrTypeCost + (typeMatchQualityMetric(typ, left) min typeMatchQualityMetric(typ, right))
      case (OrType(left, right), supr) =>
        andOrOrTypeCost + (typeMatchQualityMetric(left, supr) min typeMatchQualityMetric(right, supr))
      case (typ, OrType(left, right)) =>
        andOrOrTypeCost + (typeMatchQualityMetric(typ, left) min typeMatchQualityMetric(typ, right))
      case (typ: TypeLambda, supr: TypeLambda) =>
        val dummyTypes = genDummyTypes(typ.args.size)
        val typResult  = substituteBindings(typ.result, typ.args.flatMap(_.itid).zip(dummyTypes).toMap)
        val suprResult = substituteBindings(supr.result, supr.args.flatMap(_.itid).zip(dummyTypes).toMap)
        typeLambdaCost + typeMatchQualityMetric(typResult, suprResult)
      case (_: TypeLambda, _) =>
        aLotCost
      case (_, _: TypeLambda) =>
        aLotCost
      case (typ: Type, supr: Type) if typ.isVariable && typ.isGeneric && supr.isVariable && supr.isGeneric =>
        varToVarCost + variancesMatchQualityMetric(typ.params, supr.params)
      case (typ: Type, supr: Type) if typ.isVariable && typ.isGeneric && supr.isGeneric =>
        varToConcreteCost + variancesMatchQualityMetric(typ.params, supr.params)
      case (typ: Type, supr: Type) if supr.isVariable && supr.isGeneric && typ.isGeneric =>
        concreteToVarCost + variancesMatchQualityMetric(typ.params, supr.params)
      case (typ: Type, _: Type) if typ.isVariable && typ.isGeneric =>
        losingInformationCost
      case (_: Type, supr: Type) if supr.isVariable && supr.isGeneric =>
        losingInformationCost
      case (typ: Type, supr: Type) if typ.isVariable && supr.isVariable =>
        varToVarCost
      case (typ: Type, supr: Type) if typ.isVariable && supr.isGeneric =>
        losingInformationCost
      case (typ: Type, supr: Type) if supr.isVariable && typ.isGeneric =>
        losingInformationCost
      case (_: Type, supr: Type) if supr.isVariable =>
        concreteToVarCost
      case (typ: Type, _: Type) if typ.isVariable =>
        varToConcreteCost
      case (typ: Type, supr: Type) if typ.isGeneric && !supr.isGeneric =>
        losingInformationCost
      case (typ: Type, supr: Type) if !isGeneralised(typ) && isGeneralised(supr) =>
        losingInformationCost
      case (typ: Type, supr: Type) if !isGeneralised(supr) && isGeneralised(typ) =>
        losingInformationCost
      case (typ: Type, supr: Type) =>
        equalCost + variancesMatchQualityMetric(typ.params, supr.params)
    }
  }

  def isGeneralised(typ: TypeLike): Boolean = typ match {
    case AndType(left, right) => isGeneralised(left) || isGeneralised(right)
    case OrType(left, right)  => isGeneralised(left) || isGeneralised(right)
    case t: TypeLambda => isGeneralised(t.result)
    case t: Type       => avoidThose.contains(t.name)
  }

}

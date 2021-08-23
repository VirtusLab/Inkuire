package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model._

class IsomorphismMatchQualityService(val db: InkuireDb) extends BaseMatchQualityService with MatchingOps {
  
  def sortMatches(functions: Seq[(ExternalSignature, Signature)]): Seq[ExternalSignature] = {
    functions
      .sortBy {
        case (fun, matching) => matchQualityMetric(fun, matching)
      }
      .map(_._1)
  }

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
  final val genericAF = Set(
    "Any",
    "Object",
    "AnyVal",
    "AnyRef",
    "Matchable"
  ).map(TypeName.apply)

  /** Matching constants */
  final val aLotCost = 1000000
  final val losingInformationCost = 10000
  final val losingVarInformationCost = 3000
  final val varToConcreteCost = 200
  final val andOrOrTypeCost = 50
  final val dealiasCost = 10
  final val subTypeCost = 100
  final val typeLambdaCost = 1
  final val varToVarCost = 1

  val p = new ScalaExternalSignaturePrettifier

  /**
    * Returns match quality metric for two typelikes
    * the lower the metric value, the better the match
    * 
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
        varToVarCost + variancesMatchQualityMetric(typ.params, supr.params)
      case (typ: Type, supr: Type) if supr.isVariable && supr.isGeneric && typ.isGeneric =>
        varToVarCost + variancesMatchQualityMetric(typ.params, supr.params)
      case (typ: Type, supr: Type) if typ.isVariable && typ.isGeneric =>
        losingVarInformationCost
      case (typ: Type, supr: Type) if supr.isVariable && supr.isGeneric =>
        losingVarInformationCost
      case (typ: Type, supr: Type) if typ.isVariable && supr.isVariable =>
        varToVarCost
      case (typ: Type, supr: Type) if typ.isVariable && supr.isGeneric =>
        losingVarInformationCost
      case (typ: Type, supr: Type) if supr.isVariable && typ.isGeneric =>
        losingVarInformationCost
      case (typ: Type, supr: Type) if supr.isVariable =>
        varToConcreteCost
      case (typ: Type, supr: Type) if typ.isVariable =>
        varToConcreteCost
      case (typ: Type, supr: Type) if typ.itid == supr.itid =>
        variancesMatchQualityMetric(typ.params, supr.params)
      case (typ: Type, supr: Type) =>
        db.typeAliases
          .get(typ.itid.get)
          .toList
          .flatMap(alias => dealias(typ, alias))
          .map((_, supr, dealiasCost))
          .++(db.typeAliases.get(supr.itid.get).toList.flatMap(alias => dealias(supr, alias)).map((typ, _, dealiasCost)))
          .++(db.types.get(typ.itid.get).toList.flatMap(node => specializeParents(typ, node)).map {
            case t: Type if t.isGeneric != typ.isGeneric || genericAF.contains(t.name) => (t, supr, losingInformationCost)
            case t => (t, supr, subTypeCost)
          })
          .map {
            case (t, s, cost) => cost + typeMatchQualityMetric(t, s)
          }
          .minOption
          .getOrElse(aLotCost)
    }
  }

}

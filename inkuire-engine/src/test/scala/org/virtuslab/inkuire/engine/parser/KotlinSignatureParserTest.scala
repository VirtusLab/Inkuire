package org.virtuslab.inkuire.engine.parser

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.virtuslab.inkuire.engine.parser.model.{GenericType, Signature}
import org.virtuslab.inkuire.engine.parser.model.Type._

class KotlinSignatureParserTest extends AnyFlatSpec with Matchers {

  it should "parse simple signature" in {
    //given
    val str = "Int.(String) -> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes = Right(Signature("Int".concreteType, Seq("String".concreteType), "Double".concreteType))

    res should equal (expectedRes)
  }

  it should "parse signature with multiple arguments" in {
    //given
    val str = "Int.(String, Long, Float) -> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes = Right(Signature("Int".concreteType, Seq("String".concreteType, "Long".concreteType, "Float".concreteType), "Double".concreteType))

    res should equal (expectedRes)
  }

  it should "parse signature without arguments" in {
    //given
    val str = "Int.() -> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes = Right(Signature("Int".concreteType, Seq(), "Double".concreteType))

    res should equal (expectedRes)
  }

  it should "parse signature with generic return type" in {
    //given
    val str = "String.(Int) -> List<String>"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes = Right(Signature("String".concreteType, Seq("Int".concreteType), GenericType("List", Seq("String".concreteType))))

    res should equal (expectedRes)
  }

  it should "parse signature with generic receiver" in {
    //given
    val str = "Array<Double>.(Int) -> String"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes = Right(Signature(GenericType("Array", Seq("Double".concreteType)), Seq("Int".concreteType), "String".concreteType))

    res should equal (expectedRes)
  }
}

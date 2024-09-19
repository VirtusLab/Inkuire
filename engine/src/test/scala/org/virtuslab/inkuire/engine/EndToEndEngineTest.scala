package org.virtuslab.inkuire.engine

abstract class EndToEndEngineTest extends munit.FunSuite with BaseEndToEndEngineTest {

  testFunctionFound("List[A] => (A => B) => List[B]", "map")

  testFunctionFound("List[A] => (A => List[B]) => List[B]", "flatMap")

  testFunctionFound("List[Int] => (Int => List[Float]) => List[Float]", "flatMap")

  testFunctionFound("List[Int] => (Int => Float) => List[AnyVal]", "map")

  testFunctionFound("Seq[Int] => (Int => String) => Seq[String]", "map")

  testFunctionFound("A => (A => B) => B", "pipe")

  testFunctionFound("Char => (Char => B) => B", "pipe")

  testFunctionFound("Char => (Any => Double) => Double", "pipe")

  testFunctionFound("Boolean => A => Option[A]", "Option.when")

  testFunctionFound("Boolean => B => A => Either[A, B]", "Either.cond")

  testFunctionFound(
    "IArray[Float] => (Float => Boolean) => Boolean",
    "IArray.forall"
  )

  testFunctionFound("List[A] => B => (B => A => B) => B", "foldLeft")

  testFunctionFound("F[A] => B => (B => A => B) => B", "foldLeft")

  testFunctionFound("List[A] => B => ((B, A) => B) => B", "foldLeft")

  testFunctionFound("F[A] => B => ((B, A) => B) => B", "foldLeft")

  testFunctionFound("List[A] => A => (A => A => A) => A", "foldLeft")

  testFunctionFound("List[A] => A => ((A, A) => A) => A", "foldLeft")

  testFunctionFound("F[A] => A => (A => A => A) => A", "foldLeft")

  testFunctionFound("F[A] => A => ((A, A) => A) => A", "foldLeft")

  testFunctionFound("List[A] => A => Boolean", "contains")

  testFunctionFound("Seq[A] => A => Boolean", "contains")
}

class EndToEndEngineTest360RC1 extends EndToEndEngineTest {
  override val filePath: String = "stdlib-3.6.0-RC1.json"
}

class EndToEndEngineTest312 extends EndToEndEngineTest {
  override val filePath: String = "stdlib-3.1.2.json"
}

package org.virtuslab.inkuire.engine

class EndToEndEngineTest extends munit.FunSuite with BaseEndToEndEngineTest {

  override val filePath: String = "./engine/src/test/resources/stdlib.json"

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
  ) // TODO(kπ) IMHO should be just `forall` (generation bug)

  testFunctionFound("List[A] => B => (B => A => B) => B", "foldLeft")

  testFunctionFound("F[A] => B => (B => A => B) => B", "foldLeft")

  testFunctionFound("List[A] => B => ((B, A) => B) => B", "foldLeft")

  testFunctionFound("F[A] => B => ((B, A) => B) => B", "foldLeft")

  // TODO(kπ) this is a bug in constraint checking
  // testFunctionFound("List[A] => A => (A => A => A) => A", "foldLeft")

  // testFunctionFound("List[A] => A => ((A, A) => A) => A", "foldLeft")

  // testFunctionFound("F[A] => A => (A => A => A) => A", "foldLeft")

  // testFunctionFound("F[A] => A => ((A, A) => A) => A", "foldLeft")
}

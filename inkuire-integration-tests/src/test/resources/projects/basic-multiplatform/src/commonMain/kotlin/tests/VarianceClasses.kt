package tests

open class A1<in T>

class B1<T> : A1<T>() {
    fun t(): T = 1 as T
}

open class A2<out T>

class B2<T> : A2<T>() {
    fun t(t: T): Int = 1
}

fun <R : Any, C : B2<in R>> CharSequence.weirdFlexButOk(destination: C, transform: (Int, Char) -> R?): C = TODO()
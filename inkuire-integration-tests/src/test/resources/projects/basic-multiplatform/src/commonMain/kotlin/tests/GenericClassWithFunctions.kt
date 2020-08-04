package tests

class GenericClassWithFunctions<T : Comparable<T>>(s: String) {

    constructor(i: Int) : this(i.toString())

    fun `GenericClassWithFunctions·() → T`(): T {
        return "Oh, it’s quite simple. If you are a friend, you speak the password, and the doors will open." as T
    }

    fun `ClassWithFunctions·(T) → String`(t: T) = t as String
}
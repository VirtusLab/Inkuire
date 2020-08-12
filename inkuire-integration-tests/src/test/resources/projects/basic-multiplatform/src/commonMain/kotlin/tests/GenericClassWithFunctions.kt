package tests

class GenericClassWithFunctions<T : Comparable<T>>(s: String) {

    constructor(i: Int) : this(i.toString())

    fun `GenericClassWithFunctions·() → T`(): T {
        return "Oh, it’s quite simple. If you are a friend, you speak the password, and the doors will open." as T
    }

    fun `ClassWithFunctions·(T) → String`(t: T) = t as String
}

class InheritingClassFromGenericType<T : Number, R : CharSequence> : Comparable<T>, Collection<R> {
    override fun compareTo(other: T): Int = 0
    override fun contains(element: R): Boolean = false
    override fun containsAll(elements: Collection<R>): Boolean = true
    override fun isEmpty(): Boolean = false
    override fun iterator(): Iterator<R> = TODO("Not yet implemented")
    override val size: Int = 0
}
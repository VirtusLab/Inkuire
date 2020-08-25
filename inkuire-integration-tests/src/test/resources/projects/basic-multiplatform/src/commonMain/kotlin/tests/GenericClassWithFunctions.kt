package tests


// TODO: Rename to A, B, C or something like that and change that in tests
interface Comparable<T>
interface Collection<out T>
interface List<T>



class GenericClassWithFunctions<T : Comparable<T>>(s: String) {

    constructor(i: Int) : this(i.toString())

    fun `GenericClassWithFunctions·() → T`(): T {
        return "Oh, it’s quite simple. If you are a friend, you speak the password, and the doors will open." as T
    }

    fun `ClassWithFunctions·(T) → String`(t: T) = t as String
}

class InheritingClassFromGenericType<T : Number, R : CharSequence> : Comparable<T>, Collection<R>
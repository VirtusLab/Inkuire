package tests

class ClassWithFunctions(i: Int) {

    constructor(s: String) : this(0)

    fun `ClassWithFunctions·() → Unit`(): Unit {
        println("Oh, it’s quite simple. If you are a friend, you speak the password, and the doors will open.")
    }

    fun `ClassWithFunctions·() → String`() = "Oh, it’s quite simple. If you are a friend, you speak the password, and the doors will open."

    fun `ClassWithFunctions·(String) → String`(s: String) = s

    fun String.`with(ClassWithFunctions) { String·() → Unit }`(): Unit = println(this)

    fun String.`with(ClassWithFunctions) { String·(String) → String }`(s: String) = this + s

    fun String.`with(ClassWithFunctions) { String·(String, Int, Boolean) → Float }`(s: String, i: Int, b: Boolean): Float {
        return i.toFloat()
    }

}
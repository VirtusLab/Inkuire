package tests

fun `() → Unit`(): Unit {
    println("Oh, it’s quite simple. If you are a friend, you speak the password, and the doors will open.")
}

fun `() → String`() = "Oh, it’s quite simple. If you are a friend, you speak the password, and the doors will open."

fun `(String) → String`(s: String) = s

fun String.`String·() → Unit`(): Unit = println(this)

fun String.`String·(String) → String`(s: String) = this + s

fun String.`String·(String, Int, Boolean) → Float`(s: String, i: Int, b: Boolean): Float {
    return i.toFloat()
}

fun String.`String·(String, Int = 1, Boolean = true) → Float`(s: String, i: Int = 1, b: Boolean = true): Float {
    return i.toFloat()
}

fun `((String) → Int) → Unit`(s: (String) -> Int): Unit {
    println(s("Even the smallest person can change the course of the future."))
}

fun `(String·(String) → Int) → Unit`(s: String.(String) -> Int): Unit {
    println("Galadriela said: ".s("Even the smallest person can change the course of the future."))
    println(s("Galadriela said: ", "Even the smallest person can change the course of the future."))
}

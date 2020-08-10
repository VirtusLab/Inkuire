package tests

class InheritingClass : Runnable {

    override fun run() {
        println("Well, the years start coming")
        while(true) {
            println(" and they don't stop coming")
            Thread.sleep(1000)
        }
    }

    fun hello() = "Hello"
}
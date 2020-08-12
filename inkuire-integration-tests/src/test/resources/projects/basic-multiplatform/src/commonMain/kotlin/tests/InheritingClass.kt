package tests

interface ToInheritFrom {
    fun run()
}

class InheritingClass : ToInheritFrom {

    override fun run() {
        println("Well, the years start coming")
        while(true) {
            println(" and they don't stop coming")
            Thread.sleep(1000)
        }
    }

    fun hello() = "Hello"
}
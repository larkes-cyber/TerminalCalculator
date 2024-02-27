import java.lang.StringBuilder

fun main(args: Array<String>) {
    val str = "A\nB\nC"

    val lines = str.split("\r?\n|\r".toRegex()).toTypedArray()

    println(listOf(*lines))
}


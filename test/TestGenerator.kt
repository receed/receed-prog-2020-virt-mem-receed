import java.io.File

// Generate test and print it to file
fun printTest(numPages: Int, numFrames: Int, numAccesses: Int, fileName: String) {
    val accesses = generateAccessSequence(numPages, numAccesses).joinToString(" ")
    File(fileName).writeText("$numPages $numFrames\n${accesses}")
}

// Generate integration tests
fun main() {
    printTest(10, 11, 1000, "data/test4")
    printTest(10, 3, 10000, "data/test5")
    printTest(10, 8, 1, "data/test6")
    printTest(9, 4, 100, "data/test7")
    printTest(43, 42, 10000, "data/test8")
    printTest(400, 350, 100000, "data/test9")
    printTest(100000, 1000, 100000, "data/test10")
    main(Array(10) { "data/test${it + 1}" })
    for (number in 1..10) {
        File("data/test$number.out").copyTo(File("data/test$number.a"), overwrite=true)
    }
}
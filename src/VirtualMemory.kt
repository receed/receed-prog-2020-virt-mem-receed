import java.io.File
import java.util.*
import kotlin.random.Random.Default.nextInt
import jetbrains.letsPlot.*
import kotlin.math.max

// Describes a common interface for substitution algorithms
fun interface SubstitutionAlgorithm {
    fun apply(numPages: Int, numFrames: Int, accessedPages: List<Int>): Array<Int?>
    fun apply(task: Task): Array<Int?> = apply(task.numPages, task.numFrames, task.accessedPages)
}

// Puts page in frame, removing an old page in it
fun assignFrame(page: Int, frame: Int, pageInFrame: Array<Int?>, frameOfPage: Array<Int?>) {
    val oldPage = pageInFrame[frame]
    if (oldPage != null)
        frameOfPage[oldPage] = null
    pageInFrame[frame] = page
    frameOfPage[page] = frame
}

// First in - first out algorithm
val FIFO = SubstitutionAlgorithm { numPages, numFrames, accessedPages ->
    val frameOfPage = arrayOfNulls<Int>(numPages + 1)
    val pageInFrame = arrayOfNulls<Int>(numFrames + 1)
    val frameToSubstitute = arrayOfNulls<Int>(accessedPages.size)
    var currentFrame = 1
    for ((accessedIndex, page) in accessedPages.withIndex()) {
        val oldFrame = frameOfPage[page]
        if (oldFrame == null) {
            frameToSubstitute[accessedIndex] = currentFrame
            assignFrame(page, currentFrame, pageInFrame, frameOfPage)
            frameOfPage[page] = currentFrame
            if (currentFrame == numFrames)
                currentFrame = 1
            else
                currentFrame++
        }
    }
    frameToSubstitute
}

// Compares two pairs. Needed to efficiently find best frame in LRU and OPT
class PairComparator : Comparator<Pair<Int, Int>> {
    override fun compare(p0: Pair<Int, Int>?, p1: Pair<Int, Int>?): Int {
        if (p0 == null || p1 == null)
            return 0
        if (p0.first != p1.first)
            return p0.first - p1.first
        return p0.second - p1.second
    }
}

// Least recently used algorithm
val LRU = SubstitutionAlgorithm { numPages, numFrames, accessedPages ->
    val frameOfPage = arrayOfNulls<Int>(numPages + 1)
    val lastUsed = (0..numPages).map { -1 }.toMutableList()
    val pageInFrame = arrayOfNulls<Int>(numFrames + 1)
    val substituteCandidates = (1..numFrames).map { -1 to it }.toSortedSet(PairComparator())
    val frameToSubstitute = arrayOfNulls<Int>(accessedPages.size)
    for ((accessedIndex, page) in accessedPages.withIndex()) {
        val oldFrame = frameOfPage[page]
        if (oldFrame != null) {
            substituteCandidates.remove(lastUsed[page] to oldFrame)
            lastUsed[page] = accessedIndex
            substituteCandidates.add(accessedIndex to oldFrame)
        } else {
            val currentFrame = substituteCandidates.first().second
            substituteCandidates.remove(substituteCandidates.first())
            frameToSubstitute[accessedIndex] = currentFrame
            assignFrame(page, currentFrame, pageInFrame, frameOfPage)
            lastUsed[page] = accessedIndex
            substituteCandidates.add(accessedIndex to currentFrame)
        }
    }
    frameToSubstitute
}

// Optimal algorithm
val OPT = SubstitutionAlgorithm { numPages, numFrames, accessedPages ->
    val lastPosition = arrayOfNulls<Int>(numPages + 1)
    val nextPosition = arrayOfNulls<Int>(accessedPages.size)
    for ((accessedIndex, page) in accessedPages.withIndex().reversed()) {
        nextPosition[accessedIndex] = lastPosition[page]
        lastPosition[page] = accessedIndex
    }
    val frameOfPage = arrayOfNulls<Int>(numPages + 1)
    val pageInFrame = arrayOfNulls<Int>(numFrames + 1)
    val substituteCandidates = (1..numFrames).map { -accessedPages.size to it }.toSortedSet(PairComparator())
    val frameToSubstitute = arrayOfNulls<Int>(accessedPages.size)
    for ((accessedIndex, page) in accessedPages.withIndex()) {
        val oldFrame = frameOfPage[page]
        if (oldFrame != null) {
            substituteCandidates.remove(-accessedIndex to oldFrame)
            substituteCandidates.add(-(nextPosition[accessedIndex] ?: accessedPages.size) to oldFrame)
        } else {
            val currentFrame = substituteCandidates.first().second
            substituteCandidates.remove(substituteCandidates.first())
            frameToSubstitute[accessedIndex] = currentFrame
            assignFrame(page, currentFrame, pageInFrame, frameOfPage)
            substituteCandidates.add(-(nextPosition[accessedIndex] ?: accessedPages.size) to currentFrame)
        }
    }
    frameToSubstitute
}

val algorithms = listOf(FIFO to "FIFO", LRU to "LRU", OPT to "OPT")

// Checks if the given substitution list is valid
fun isValidSubstitution(numPages: Int, numFrames: Int, accessedPages: List<Int>, frameToSubstitute: Array<Int?>): Boolean {
    val frameOfPage = arrayOfNulls<Int>(numPages + 1)
    val pageInFrame = arrayOfNulls<Int>(numFrames + 1)
    for ((accessedIndex, page) in accessedPages.withIndex()) {
        val currentFrame = frameToSubstitute[accessedIndex]
        if (currentFrame == null) {
            if (frameOfPage[page] == null)
                return false
        } else {
            if (frameOfPage[page] != null)
                return false
            assignFrame(page, currentFrame, pageInFrame, frameOfPage)
        }
    }
    return true
}

// Generates random sequence of accessed page numbers
fun generateAccessSequence(numPages: Int, numAccesses: Int) = (1..numAccesses).map {nextInt(1, numPages + 1)}

// Counts score (number of substitutions) of the given substitution list
fun countScore(frameToSubstitute: Array<Int?>) = frameToSubstitute.count {it != null}

class Task(val numPages: Int, val numFrames: Int, val accessedPages: List<Int>)

class InvalidInputException(message: String) : Exception(message)

// Reads constraints and accessed page number from a file
fun readInput(inputFileName: String): List<Task> {
    val inputFile = File(inputFileName)
    if (!inputFile.exists())
        throw InvalidInputException("$inputFileName: no such file")
    val lines = inputFile.readLines().filter {it.isNotEmpty()}
    return lines.chunked(2).map { linePair ->
        if (linePair.size != 2)
            throw InvalidInputException("Odd number of lines")
        val constraints = linePair[0].split(" ").map { it.toIntOrNull() }
        val accessedPages = linePair[1].split(" ").map { it.toIntOrNull() }
        if (constraints.size != 2)
            throw InvalidInputException("The first line of each test should contain number of pages and number of frames")
        val numPages = constraints[0] ?: throw InvalidInputException("Number of pages isn't a number")
        val numFrames = constraints[1] ?: throw InvalidInputException("Number of frames isn't a number")
        val accessedPagesNumbers = accessedPages.filterNotNull().filter { it in 1..numPages }
        if (accessedPagesNumbers.size < accessedPages.size)
            throw InvalidInputException("Invalid number of accessed page")
        Task(numPages, numFrames, accessedPagesNumbers)
    }
}

// Returns results of different algorithms
fun generateReport(task: Task): String {
    return algorithms.joinToString("\n") { (algorithm, name) ->
        val result = algorithm.apply(task)
        val score = countScore(result)
        val resultString = result.joinToString(" ") { it?.toString() ?: "0" }
        "$name (score $score): $resultString"
    }
}

// Runs all algorithms for all input files
fun runFromFiles(files: Array<String>) {
    for (inputFileName in files) {
        val tasks = readInput(inputFileName)
        val outputFile = File("$inputFileName.out")
        outputFile.writeText(tasks.joinToString("\n") { generateReport(it) })
    }
}

// Entry point
fun main(args: Array<String>) {
    runFromFiles(args)
    drawPlot(9, 3, 1000)
//    val accessed = generateAccessSequence(5, 20)
//    val resFIFO = FIFO.apply(5, 3, accessed)
//    assert(isValidSubstitution(5, 3, accessed, resFIFO))
//    for (i in resFIFO)
//        println(i)
//    println()
//    val resLRU = LRU.apply(5, 3, accessed)
//    assert(isValidSubstitution(5, 3, accessed, resLRU))
//    for (i in resLRU)
//        println(i)
//    println()
//    val resOPT = OPT.apply(5, 3, accessed)
//    assert(isValidSubstitution(5, 3, accessed, resOPT))
//    for (i in resOPT)
//        println(i)
}
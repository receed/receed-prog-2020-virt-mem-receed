import java.io.File
import java.util.*
import kotlin.random.Random.Default.nextInt

// Puts page in frame, removing an old page in it
fun assignFrame(page: Int, frame: Int, pageInFrame: Array<Int?>, frameOfPage: Array<Int?>) {
    val oldPage = pageInFrame[frame]
    if (oldPage != null)
        frameOfPage[oldPage] = null
    pageInFrame[frame] = page
    frameOfPage[page] = frame
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

// Base class for all algorithms
abstract class SubstitutionStrategy(private val numPages: Int, private val numFrames: Int, private val accessedPages: List<Int>) {
    // Should update internal structure when [page] at [oldFrame] is accessed again in position [accessedIndex]
    abstract fun update(page: Int, oldFrame: Int, accessedIndex: Int)

    // Should return number of frame where to put [page] which is currently not in memory
    abstract fun replace(page: Int, accessedIndex: Int): Int

    // Returns numbers of frames where to put each page (null if page is already in memory)
    fun apply(): Array<Int?> {
        val frameOfPage = arrayOfNulls<Int>(numPages + 1)
        val pageInFrame = arrayOfNulls<Int>(numFrames + 1)
        val frameToSubstitute = arrayOfNulls<Int>(accessedPages.size)
        for ((accessedIndex, page) in accessedPages.withIndex()) {
            val oldFrame = frameOfPage[page]
            if (oldFrame != null) {
                update(page, oldFrame, accessedIndex)
            } else {
                val currentFrame = replace(page, accessedIndex)
                assignFrame(page, currentFrame, pageInFrame, frameOfPage)
                frameToSubstitute[accessedIndex] = currentFrame
            }
        }
        return frameToSubstitute
    }
}

// First in - first out algorithm
class FIFOStrategy(numPages: Int, private val numFrames: Int, accessedPages: List<Int>) :
        SubstitutionStrategy(numPages, numFrames, accessedPages) {
    constructor(task: Task) : this(task.numPages, task.numFrames, task.accessedPages)

    private var nextFrame = 1
    override fun update(page: Int, oldFrame: Int, accessedIndex: Int) {

    }

    override fun replace(page: Int, accessedIndex: Int): Int {
        val currentFrame = nextFrame
        if (nextFrame == numFrames)
            nextFrame = 1
        else
            nextFrame++
        return currentFrame
    }
}

// Least recently used algorithm
class LRUStrategy(numPages: Int, numFrames: Int, accessedPages: List<Int>) : SubstitutionStrategy(numPages, numFrames, accessedPages) {
    constructor(task: Task) : this(task.numPages, task.numFrames, task.accessedPages)

    private val lastUsed = (0..numPages).map { -1 }.toMutableList()
    private val substituteCandidates = (1..numFrames).map { -1 to it }.toSortedSet(PairComparator())

    override fun update(page: Int, oldFrame: Int, accessedIndex: Int) {
        substituteCandidates.remove(lastUsed[page] to oldFrame)
        lastUsed[page] = accessedIndex
        substituteCandidates.add(accessedIndex to oldFrame)
    }

    override fun replace(page: Int, accessedIndex: Int): Int {
        val currentFrame = substituteCandidates.first().second
        substituteCandidates.remove(substituteCandidates.first())
        lastUsed[page] = accessedIndex
        substituteCandidates.add(accessedIndex to currentFrame)
        return currentFrame
    }
}

// Optimal algorithm
class OPTStrategy(numPages: Int, numFrames: Int, private val accessedPages: List<Int>) : SubstitutionStrategy(numPages, numFrames, accessedPages) {
    constructor(task: Task) : this(task.numPages, task.numFrames, task.accessedPages)

    private val lastPosition = arrayOfNulls<Int>(numPages + 1)
    private val nextPosition = arrayOfNulls<Int>(accessedPages.size)
    private val substituteCandidates = (1..numFrames).map { -accessedPages.size to it }.toSortedSet(PairComparator())

    init {
        for ((accessedIndex, page) in accessedPages.withIndex().reversed()) {
            nextPosition[accessedIndex] = lastPosition[page]
            lastPosition[page] = accessedIndex
        }
    }

    override fun update(page: Int, oldFrame: Int, accessedIndex: Int) {
        substituteCandidates.remove(-accessedIndex to oldFrame)
        substituteCandidates.add(-(nextPosition[accessedIndex] ?: accessedPages.size) to oldFrame)
    }

    override fun replace(page: Int, accessedIndex: Int): Int {
        val currentFrame = substituteCandidates.first().second
        substituteCandidates.remove(substituteCandidates.first())
        substituteCandidates.add(-(nextPosition[accessedIndex] ?: accessedPages.size) to currentFrame)
        return currentFrame
    }
}

// List of pairs of name of algorithm and function applying it
val algorithms = listOf<Pair<String, (Task) -> Array<Int?>>>("FIFO" to { FIFOStrategy(it).apply() },
        "LRU" to { LRUStrategy(it).apply() },
        "OPT" to { OPTStrategy(it).apply() })

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
fun generateAccessSequence(numPages: Int, numAccesses: Int) = (1..numAccesses).map { nextInt(1, numPages + 1) }

// Counts score (number of substitutions) of the given substitution list
fun countScore(frameToSubstitute: Array<Int?>) = frameToSubstitute.count { it != null }

// Input for algorithms
class Task(val numPages: Int, val numFrames: Int, val accessedPages: List<Int>)

// Exception for invalid input
class InvalidInputException(message: String) : Exception(message)

// Reads constraints and accessed page number from a file
fun readInput(inputFileName: String): List<Task> {
    val inputFile = File(inputFileName)
    if (!inputFile.exists())
        throw InvalidInputException("$inputFileName: no such file")
    val lines = inputFile.readLines().filter { it.isNotEmpty() }
    return lines.chunked(2).map { linePair ->
        if (linePair.size != 2)
            throw InvalidInputException("Odd number of lines")
        val (numPages, numFrames) = linePair[0].split(" ").map { it.toIntOrNull() }
        if (numPages == null || numFrames == null)
            throw InvalidInputException("The first line of each test should contain number of pages and number of frames")
        val accessedPages = linePair[1].split(" ").map { it.toIntOrNull() }
        val accessedPagesNumbers = accessedPages.filterNotNull().filter { it in 1..numPages }
        if (accessedPagesNumbers.size < accessedPages.size)
            throw InvalidInputException("Invalid number of accessed pages")
        Task(numPages, numFrames, accessedPagesNumbers)
    }
}

// Returns results of different algorithms
fun generateReport(task: Task): String {
    return algorithms.joinToString("\n") { (name, algorithm) ->
        val result = algorithm(task)
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
    try {
        if (args.isEmpty())
            throw InvalidInputException("No arguments")
        if (args[0] == "-p")
            drawPlotByArgs(args.drop(1))
        else
            runFromFiles(args)
    } catch (e: InvalidInputException) {
        println(e.message)
    } catch (e: Exception) {
        println("Unknown error")
    }
}
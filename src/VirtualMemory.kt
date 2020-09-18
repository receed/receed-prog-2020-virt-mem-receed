import java.util.*
import kotlin.random.Random.Default.nextInt

fun interface SubstitutionAlgorithm {
    fun apply(numPages: Int, numFrames: Int, accessedPages: List<Int>): Array<Int?>
}

val FIFO = SubstitutionAlgorithm { numPages, numFrames, accessedPages ->
    val pageInMemory = (0..numPages).map { false }.toMutableList()
    val pageInFrame = arrayOfNulls<Int>(numFrames + 1)
    val frameToSubstitute = arrayOfNulls<Int>(accessedPages.size)
    var currentFrame = 1
    for ((accessedIndex, page) in accessedPages.withIndex()) {
        if (!pageInMemory[page]) {
            val oldPage = pageInFrame[currentFrame]
            if (oldPage != null)
                pageInMemory[oldPage] = false
            frameToSubstitute[accessedIndex] = currentFrame
            pageInFrame[currentFrame] = page
            if (currentFrame == numFrames)
                currentFrame = 1
            else
                currentFrame++
            pageInMemory[page] = true
        }
    }
    frameToSubstitute
}

class C : Comparator<Pair<Int, Int>> {
    override fun compare(p0: Pair<Int, Int>?, p1: Pair<Int, Int>?): Int {
        if (p0 == null || p1 == null)
            return 0
        if (p0.first != p1.first)
            return p0.first - p1.first
        return p0.second - p1.second
    }
}

val LRU = SubstitutionAlgorithm { numPages, numFrames, accessedPages ->
    val frameOfPage = arrayOfNulls<Int>(numPages + 1)
    val lastUsed = (0..numPages).map { -1 }.toMutableList()
    val pageInFrame = arrayOfNulls<Int>(numFrames + 1)
    val substituteCandidates = (1..numFrames).map { -1 to it }.toSortedSet(C())
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
            val oldPage = pageInFrame[currentFrame]
            if (oldPage != null)
                frameOfPage[oldPage] = null
            frameToSubstitute[accessedIndex] = currentFrame
            pageInFrame[currentFrame] = page
            frameOfPage[page] = currentFrame
            lastUsed[page] = accessedIndex
            substituteCandidates.add(accessedIndex to currentFrame)
        }
    }
    frameToSubstitute
}

val OPT = SubstitutionAlgorithm { numPages, numFrames, accessedPages ->
    val lastPosition = arrayOfNulls<Int>(numPages + 1)
    val nextPosition = arrayOfNulls<Int>(accessedPages.size)
    for ((accessedIndex, page) in accessedPages.withIndex().reversed()) {
        nextPosition[accessedIndex] = lastPosition[page]
        lastPosition[page] = accessedIndex
    }
    val frameOfPage = arrayOfNulls<Int>(numPages + 1)
    val pageInFrame = arrayOfNulls<Int>(numFrames + 1)
    val substituteCandidates = (1..numFrames).map { accessedPages.size to it }.toSortedSet(C())
    val frameToSubstitute = arrayOfNulls<Int>(accessedPages.size)
    for ((accessedIndex, page) in accessedPages.withIndex()) {
        val oldFrame = frameOfPage[page]
        if (oldFrame != null) {
            substituteCandidates.remove(accessedIndex to oldFrame)
            substituteCandidates.add((nextPosition[accessedIndex] ?: accessedPages.size) to oldFrame)
        } else {
            val currentFrame = substituteCandidates.last().second
            substituteCandidates.remove(substituteCandidates.last())
            val oldPage = pageInFrame[currentFrame]
            if (oldPage != null)
                frameOfPage[oldPage] = null
            frameToSubstitute[accessedIndex] = currentFrame
            pageInFrame[currentFrame] = page
            frameOfPage[page] = currentFrame
            substituteCandidates.add((nextPosition[accessedIndex] ?: accessedPages.size) to currentFrame)
        }
    }
    frameToSubstitute
}

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
            val oldPage = pageInFrame[currentFrame]
            if (oldPage != null)
                frameOfPage[oldPage] = null
            pageInFrame[currentFrame] = page
            frameOfPage[page] = currentFrame
        }
    }
    return true
}

fun generateAccessSequence(numPages: Int, numAccesses: Int) = (1..numAccesses).map {nextInt(1, numPages + 1)}

fun main() {
    val accessed = generateAccessSequence(5, 20)
    val resFIFO = FIFO.apply(5, 3, accessed)
    assert(isValidSubstitution(5, 3, accessed, resFIFO))
    for (i in resFIFO)
        println(i)
    println()
    val resLRU = LRU.apply(5, 3, accessed)
    assert(isValidSubstitution(5, 3, accessed, resLRU))
    for (i in resLRU)
        println(i)
    println()
    val resOPT = OPT.apply(5, 3, accessed)
    assert(isValidSubstitution(5, 3, accessed, resOPT))
    for (i in resOPT)
        println(i)
}
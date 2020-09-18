fun FIFO(numPages: Int, numFrames: Int, accessedPages: List<Int>): Array<Int?> {
    val pageInMemory = (0..numPages).map {false}.toMutableList()
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
    return frameToSubstitute
}

fun main() {
    val res = FIFO(5, 3, listOf(1, 2, 5, 3, 2, 1, 4, 2, 5))
    for (i in res)
        println(i)
}
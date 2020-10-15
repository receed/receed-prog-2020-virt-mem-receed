import FIFOStrategy
import LRUStrategy
import OPTStrategy
import Task
import countScore
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Tag
import runFromFiles
import java.io.File

@Tag("unitTest")
internal class VirtualMemoryKtTest {
    private val task1 = Task(5, 3, listOf(1, 3, 2, 4, 1, 4, 5, 3, 2))

    @Test
    fun testFIFO() {
        assertArrayEquals(arrayOf(1, 2, 3, 1, 2, null, 3, 1, 2), FIFOStrategy(task1).apply())
    }

    @Test
    fun testLRU() {
        assertArrayEquals(arrayOf(1, 2, 3, 1, 2, null, 3, 2, 1), LRUStrategy(task1).apply())
    }

    @Test
    fun testOPT() {
        assertArrayEquals(arrayOf(1, 2, 3, 3, null, null, 1, null, 1), OPTStrategy(task1).apply())
    }

    @Test
    fun countScore() {
        assertEquals(3, countScore(arrayOf(3, 6, null, 1, null)))
    }
}
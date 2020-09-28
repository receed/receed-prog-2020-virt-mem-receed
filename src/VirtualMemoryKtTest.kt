import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class VirtualMemoryKtTest {
    private val task1 = Task(5, 3, listOf(1, 3, 2, 4, 1, 4, 5, 3, 2))
    @Test
    fun testFIFO() {
        assertArrayEquals(arrayOf(1, 2, 3, 1, 2, null, 3, 1, 2), FIFO.apply(task1))
    }
    @Test
    fun testLRU() {
        assertArrayEquals(arrayOf(1, 2, 3, 1, 2, null, 3, 2, 1), LRU.apply(task1))
    }
    @Test
    fun testOPT() {
        assertArrayEquals(arrayOf(1, 2, 3, 3, null, null, 1, null, 1), OPT.apply(task1))
    }
    @Test
    fun runFromFiles() {
        val files = arrayOf("data/test1", "data/test2")
        runFromFiles(files)
        for (file in files) {
            val expected = File("$file.a").readLines()
            val result = File("$file.out").readLines()
            assertEquals(expected, result)
        }
    }
}
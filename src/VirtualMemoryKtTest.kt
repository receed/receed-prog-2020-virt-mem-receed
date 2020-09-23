import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class VirtualMemoryKtTest {
    private val task1 = Task(5, 3, listOf(1, 3, 2, 4, 1, 4, 5, 3, 2))
    @Test
    fun testFIFO() {
        assertEquals(listOf(1, 2, 3, 1, 2, 0, 3, 1, 2), FIFO.apply(task1))
    }
    @Test
    fun testLRU() {
        assertEquals(listOf(1, 2, 3, 1, 2, 0, 3, 2, 1), LRU.apply(task1))
    }
    @Test
    fun testOPT() {
        assertEquals(listOf(1, 2, 3, 3, 0, 0, 1, 0, 1), OPT.apply(task1))
    }
}
import FIFOStrategy
import LRUStrategy
import OPTStrategy
import Task
import countScore
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import runFromFiles
import java.io.File
import org.junit.jupiter.api.Tag

@Tag("integrationTest")
internal class IntegrationTest {
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
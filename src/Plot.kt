import jetbrains.datalore.plot.MonolithicAwt
import jetbrains.datalore.vis.svg.SvgSvgElement
import jetbrains.datalore.vis.swing.BatikMapperComponent
import jetbrains.datalore.vis.swing.BatikMessageCallback
import jetbrains.letsPlot.*
import jetbrains.letsPlot.geom.geom_path
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*

class SwingBatikDemoFrontendContext(private val title: String) : FrontendContext {
    private val plotSpecs = ArrayList<MutableMap<String, Any>>()

    override fun display(plotSpecRaw: MutableMap<String, Any>) {
        plotSpecs.add(plotSpecRaw)
    }

    fun showAll() {
        SwingUtilities.invokeLater {
            val frame = JFrame(title)

            val panel = JPanel()
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

            // build plots
            for (plotSpec in plotSpecs) {
                val component =
                        MonolithicAwt.buildPlotFromRawSpecs(
                                plotSpec, null,
                                SVG_COMPONENT_FACTORY_BATIK,
                                AWT_EDT_EXECUTOR,
                                COMPUTATION_MESSAGES_HANDLER
                        )

                val decorated = object : JPanel(FlowLayout(0, 0, 0)) {
                    override fun getMinimumSize(): Dimension {
                        return preferredSize
                    }

                    override fun getMaximumSize(): Dimension {
                        return preferredSize
                    }
                }

                decorated.border = BorderFactory.createLineBorder(Color.blue, 1)
                decorated.add(component)
                panel.add(Box.createRigidArea(Dimension(0, 5)))
                panel.add(decorated)
            }

            frame.contentPane.add(JScrollPane(panel))
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            frame.size = FRAME_SIZE
            frame.isVisible = true
        }
    }

    companion object {
        private val SVG_COMPONENT_FACTORY_BATIK =
                { svg: SvgSvgElement -> BatikMapperComponent(svg, BATIK_MESSAGE_CALLBACK) }

        private val BATIK_MESSAGE_CALLBACK = object : BatikMessageCallback {
            override fun handleMessage(message: String) {
                println(message)
            }

            override fun handleException(e: Exception) {
                if (e is RuntimeException) {
                    throw e
                }
                throw RuntimeException(e)
            }
        }

        private val AWT_EDT_EXECUTOR = { runnable: () -> Unit ->
            // Just invoke in the current thread.
            runnable.invoke()
        }

        private val COMPUTATION_MESSAGES_HANDLER: (List<String>) -> Unit = {
            for (message in it) {
                println("PLOT MESSAGE: $message")
            }
        }

        private val FRAME_SIZE = Dimension(700, 700)
    }
}

fun drawPlot(numPages: Int, numFrames: Int, maxAccesses: Int) {
    val accessedPages = generateAccessSequence(numPages, maxAccesses)
//    val step = kotlin.math.max(1, maxAccesses / 1000)
    val step = 1
    val numsAccesses = (1..maxAccesses step step).toList()
    val scores = algorithms.map { algorithm -> numsAccesses.map {numAccesses ->
        countScore(algorithm.first.apply(numPages, numFrames, accessedPages.take(numAccesses))) }}.flatten()
    val types = algorithms.map {algorithm -> List(numsAccesses.size) { algorithm.second }}.flatten()
    val allNumsAccesses = List(algorithms.size) {numsAccesses}.flatten()
    val data = mapOf(
            "accesses" to allNumsAccesses,
            "scores" to scores,
            "types" to types
    )
    val ctx = SwingBatikDemoFrontendContext("Title")
    LetsPlot.frontendContext = ctx
    val plot = lets_plot(data) { x="accesses"; y="scores"; color="types" } + geom_path()
    plot.show()
    ctx.showAll()
}

fun drawPlotByArgs(args: List<String>) {
    if (args.isEmpty()) {
        drawPlot(3, 5, 1000)
        return
    }
    val numbers = args.mapNotNull { it.toIntOrNull() }
    if (numbers.size != 3)
        throw InvalidInputException("Usage: -p <numPages> <numFrames> <maxAccesses>")
    drawPlot(numbers[0], numbers[1], numbers[2])
}

package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class ImageMap(image: BufferedImage) {
    private var imap = List(image.height) { y -> MutableList(image.width) { x -> Color(image.getRGB(x, y)) } }
    private fun getPixel(x: Int, y: Int) = imap[y][x]
    private val width get() = imap.first().size
    private val height get() = imap.size
    fun transpose() {
        imap = List(width) { x -> MutableList(height) { y -> getPixel(x, y) } }
    }
    val image: BufferedImage get() {
        val image = BufferedImage(width, height, ColorModel.OPAQUE)
        for (y in 0 until height) {
            for (x in 0 until width) {
                image.setRGB(x, y, getPixel(x, y).rgb)
            }
        }
        return image
    }

   companion object {
        private fun distanceSquared(c1: Color, c2: Color) = (c1.red - c2.red).toDouble().pow(2) +
                (c1.green - c2.green).toDouble().pow(2) +
                (c1.blue - c2.blue).toDouble().pow(2)
    }

    private fun makeEnergyMatrix(): Array<DoubleArray> {
        val shiftForDimension = { v: Int, dimension: Int ->
            if (v <= 0) {
                1
            } else if (v >= dimension - 1) {
                dimension - 2
            } else {
                v
            }
        }
        val energy = Array(height) { _ -> DoubleArray(width) { 0.0 } }
        for (y in 0 until height) {
            val shiftedY = shiftForDimension(y, height)
            for (x in 0 until width) {
                val shiftedX = shiftForDimension(x, width)
                val xp1 = getPixel(shiftedX - 1, y)
                val xp2 = getPixel(shiftedX + 1, y)
                val xGradient = distanceSquared(xp1, xp2)
                val yp1 = getPixel(x, shiftedY - 1)
                val yp2 = getPixel(x, shiftedY + 1)
                val yGradient = distanceSquared(yp1, yp2)

                energy[y][x] = sqrt(xGradient + yGradient)
            }
        }
        return energy
    }

    private fun getSeam(): List<Pair<Int, Int>> {
        val energy = makeEnergyMatrix()
        val pathEnergy = Array(height) { _ -> DoubleArray(width) { Double.MAX_VALUE } }
        val getMatrixValueFn = { m: Array<DoubleArray> ->
            { x: Int, y: Int ->
                if (x in 0 until width && y in 0 until height) {
                    m[y][x]
                } else {
                    Double.MAX_VALUE
                }
            }
        }
        val getPathEnergy = getMatrixValueFn(pathEnergy)

        for (y in 0 until height) {
            for (x in 0 until width) {
                pathEnergy[y][x] = min(
                    pathEnergy[y][x],
                    energy[y][x] + if (y > 0) {
                        (-1..1).minOf { getPathEnergy(x + it, y - 1) }
                    } else {
                        0.0
                    }
                )
            }
        }

        val path = mutableListOf<Int>()
        for (y in height - 1 downTo 0) {
            path.add(
                if (y == height - 1) {
                    pathEnergy.last().withIndex().minByOrNull { (_, v) -> v }?.index!!
                } else {
                    (path.last() - 1..path.last() + 1).minByOrNull { getPathEnergy(it, y) }!!
                }
            )
        }

        return path.reversed().mapIndexed { y, x -> Pair(x, y) }
    }

    fun paintEnergyImage() {
        val energy = makeEnergyMatrix()
        val maxEnergy = energy.flatMap { it.asSequence() }.maxOrNull()!!

        for (y in 0 until height) {
            for (x in 0 until width) {
                val intensity: Int = (255.0 * energy[y][x] / maxEnergy).toInt()
                imap[y][x] = Color(intensity, intensity, intensity)
            }
        }
    }

    fun paintSeam(color: Color) {
        getSeam().forEach {(x ,y) -> imap[y][x] = color }
    }

    fun resizeWidth(newWidth: Int) {
        while (width > newWidth) {
            getSeam().forEach { (x, y) -> imap[y].removeAt(x) }
        }
    }
}

data class SeamCarvingArgs(
    val inFilename: String,
    val outFilename: String,
    val width: Int,
    val height: Int,
    val operation: Operation
) {
    enum class Operation { Energy, Seam, Reduce }
}

fun main(args: Array<String>) {
    val parsedArgs = args.asSequence().chunked(2).fold(
        SeamCarvingArgs("", "", 0, 0, SeamCarvingArgs.Operation.Reduce)
    ) { acc, argPair ->
        val (k, v) = argPair
        when (k) {
            "-in" -> acc.copy(inFilename = v)
            "-out" -> acc.copy(outFilename = v)
            "-width" -> acc.copy(width = v.toInt())
            "-height" -> acc.copy(height = v.toInt())
            "-operation" -> acc.copy(operation = SeamCarvingArgs.Operation.valueOf(v.replaceFirstChar { v.first().uppercaseChar() }))
            else -> acc
        }
    }
    val image = ImageIO.read(File(parsedArgs.inFilename))
    val imap = ImageMap(image)
    when (parsedArgs.operation) {
        SeamCarvingArgs.Operation.Reduce -> {
            imap.resizeWidth(image.width - parsedArgs.width)
            imap.transpose()
            imap.resizeWidth(image.height - parsedArgs.height)
            imap.transpose()
        }
        SeamCarvingArgs.Operation.Energy -> {
            imap.paintEnergyImage()
        }
        SeamCarvingArgs.Operation.Seam -> {
            imap.paintSeam(Color.RED)
        }
    }

    ImageIO.write(imap.image, "png", File(parsedArgs.outFilename))
}

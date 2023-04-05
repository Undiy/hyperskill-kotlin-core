package watermark

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.lang.NumberFormatException
import java.lang.RuntimeException
import javax.imageio.ImageIO
import kotlin.system.exitProcess

class WatermarkException(message: String) : RuntimeException(message)

sealed interface WatermarkPosition
class WatermarkPositionGrid : WatermarkPosition
class WatermarkPositionSingle(val shiftX: Int, val shiftY: Int) : WatermarkPosition

fun loadImage(path: String) = loadImage(path, null)

fun loadImage(path: String, other: BufferedImage?): BufferedImage {
    val file = File(path)
    if (!file.exists()) {
        throw WatermarkException("The file $path doesn't exist.")
    }
    val image = ImageIO.read(file)
    val imageType = if (other == null) "image" else "watermark"

    if (image.colorModel.numColorComponents != 3) {
        throw WatermarkException("The number of $imageType color components isn't 3.")
    }
    if (image.colorModel.pixelSize !in intArrayOf(24, 32)) {
        throw WatermarkException("The $imageType isn't 24 or 32-bit.")
    }

    if (other != null && (image.width > other.width || image.height > other.height)) {
        printAndExit("The watermark's dimensions are larger.")
    }

    return image
}

fun blendColors(c1: Color, c2: Color, weight: Int): Color {
    return Color(
        (weight * c2.red + (100 - weight) * c1.red) / 100,
        (weight * c2.green + (100 - weight) * c1.green) / 100,
        (weight * c2.blue + (100 - weight) * c1.blue) / 100
    )
}

fun applyWatermark(
    image: BufferedImage,
    watermark: BufferedImage,
    weight: Int,
    useAlpha: Boolean,
    transparencyColor: Color?,
    position: WatermarkPosition
) {
    val blendPixel = { x: Int, y: Int, shiftX: Int, shiftY: Int ->
        val ic = Color(image.getRGB(x + shiftX, y + shiftY))
        val wc = Color(watermark.getRGB(x % watermark.width, y % watermark.height), true)
        val color = if ((useAlpha && wc.alpha < 255)
            || (transparencyColor != null && transparencyColor.rgb == wc.rgb)) {
            ic
        } else {
            blendColors(ic, wc, weight)
        }
        image.setRGB(x + shiftX, y + shiftY, color.rgb)
    }

    when (position) {
        is WatermarkPositionGrid -> {
            for (i in 0 until image.width) {
                for (j in 0 until image.height) {
                    blendPixel(i, j, 0, 0)
                }
            }
        }
        is WatermarkPositionSingle -> {
            for (i in 0 until watermark.width) {
                for (j in 0 until watermark.height) {
                    blendPixel(i, j, position.shiftX, position.shiftY)
                }
            }
        }
    }
}

fun printAndExit(message: String) {
    println(message)
    exitProcess(1)
}

fun main() {
    println("Input the image filename:")
    val image = try {
        loadImage(readln())
    } catch (e: WatermarkException) {
        println(e.message)
        exitProcess(1)
    }
    println("Input the watermark image filename:")
    val watermark = try {
        loadImage(readln(), image)
    } catch (e: WatermarkException) {
        println(e.message)
        exitProcess(1)
    }

    val (useAlpha, transparencyColor) = if (watermark.transparency == BufferedImage.TRANSLUCENT){
        println("Do you want to use the watermark's Alpha channel?")
        Pair(readln() == "yes", null)
    } else {
        println("Do you want to set a transparency color?")
        if (readln() == "yes") {
            println("Input a transparency color ([Red] [Green] [Blue]):")
            try {
                val (r, g, b) = readln().split(" ", limit=3).map(String::toInt).map {
                    if (it !in 0..255) {
                        throw RuntimeException("")
                    }
                    it
                }
                Pair(false, Color(r, g, b))
            } catch (e: RuntimeException) {
                println("The transparency color input is invalid.")
                exitProcess(1)
            }
        } else {
            Pair(false, null)
        }
    }

    println("Input the watermark transparency percentage (Integer 0-100):")
    val weight = try {
        readln().toInt()
    } catch (e: NumberFormatException) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(1)
    }

    if (weight !in 0..100) {
        printAndExit("The transparency percentage is out of range.")
    }

    println("Choose the position method (single, grid):")
    val position = when (readln()) {
        "single" -> {
            val diffX = image.width - watermark.width
            val diffY = image.height - watermark.height
            println("Input the watermark position ([x 0-$diffX] [y 0-$diffY]):")
            try {
                val (shiftX, shiftY) = readln().split(" ", limit=2).map(String::toInt)
                if (shiftX !in 0..diffX || shiftY !in 0..diffY) {
                    println("The position input is out of range.")
                    exitProcess(1)
                }
                WatermarkPositionSingle(shiftX, shiftY)
            } catch (e: RuntimeException) {
                println("The position input is invalid.")
                exitProcess(1)
            }
        }
        "grid" -> WatermarkPositionGrid()
        else -> {
            println("The position method input is invalid.")
            exitProcess(1)
        }
    }

    println("Input the output image filename (jpg or png extension):")
    val outFile = File(readln())
    if (outFile.extension.lowercase() !in arrayOf("jpg", "png")) {
        printAndExit("The output file extension isn't \"jpg\" or \"png\".")
    }

    applyWatermark(image, watermark, weight, useAlpha, transparencyColor, position)
    ImageIO.write(image, outFile.extension, outFile)
    println("The watermarked image ${outFile.path} has been created.")
}
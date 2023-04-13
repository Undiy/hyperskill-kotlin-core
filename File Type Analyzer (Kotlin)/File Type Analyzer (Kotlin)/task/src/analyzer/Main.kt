package analyzer

import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.system.exitProcess
import kotlin.system.measureNanoTime

fun matchBytes(bytes: ByteArray, pattern: String) : Int {
    return bytes.zip(pattern.asIterable()).takeWhile { it.first == it.second.code.toByte() }.size
}

fun substringNaive(s: ByteArray, pattern: String): Int {
    for (i in 0 .. s.size - pattern.length) {
        if (matchBytes(s.sliceArray(i until  i + pattern.length), pattern) == pattern.length) {
            return i
        }
    }
    return -1
}

fun prefixFunction(s: String): IntArray {
    val p = IntArray(s.length) { 0 }
    for (i in 1 until s.length) {
        var j = p[i - 1]
        while (true) {
            if (s[i] == s[j]) {
                p[i] = j + 1
                break
            }
            if (j > 0) {
                j = p[j - 1]
            } else {
                break
            }
        }
    }
    return p
}

fun substringKMP(s: ByteArray, pattern : String): Int {
    val p = prefixFunction(pattern)

    var i = 0
    while (i + pattern.length <= s.size) {
        val matchLen = matchBytes(s.sliceArray(i until i + pattern.length), pattern)
        if (matchLen == p.size) {
            return i
        } else {
            i += matchLen - p[matchLen - 1]
        }
    }
    return -1
}

fun main(args: Array<String>) {
    if (args.size != 4) {
        println("Invalid arguments")
        exitProcess(1)
    }
    val (algo, path, pattern, fileType) = args

    val substringFn = when (algo) {
        "--naive" -> ::substringNaive
        "--KMP" -> ::substringKMP
        else -> {
            println("Wrong algorithm")
            exitProcess(1)
        }
    }

    val time = measureNanoTime {
        val i = substringFn(Files.readAllBytes(Path(path)), pattern)
        if (i >= 0) {
            println(fileType)
        } else {
            println("Unknown file type")
        }
    }
    println("It took ${time.toDouble() / 1000_000_000} seconds")
}
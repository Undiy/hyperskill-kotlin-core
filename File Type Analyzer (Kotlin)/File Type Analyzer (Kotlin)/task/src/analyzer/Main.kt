package analyzer

import java.io.File
import kotlin.math.pow
import kotlin.system.exitProcess

private const val A = 3
private const val M = 11

class FileTypeAnalyzer(
    private val file: File,
    private val patterns: List<FileTypePattern>
) : Runnable {
    override fun run() {
        val fileType = rabinKarp().maxByOrNull { it.priority }?.fileType ?: "Unknown file type"
        println("${file.name}: $fileType")
    }

    companion object {
        fun matchBytes(bytes: ByteArray, pattern: String) : Int {
            return bytes.zip(pattern.asIterable()).takeWhile { it.first == it.second.code.toByte() }.size
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

        fun rollingHash(a: Int, m: Int): (s: ByteArray) -> Int = {
            it.foldIndexed(0) { i, acc, b ->
                acc + b * a.toDouble().pow(i).toInt()
            } % m
        }
    }

    private fun substringKMP(pattern: FileTypePattern): Int {
        val s = file.readBytes()
        val p = prefixFunction(pattern.pattern)

        var i = 0
        while (i + pattern.pattern.length <= s.size) {
            when(val matchLen = matchBytes(s.sliceArray(i until i + pattern.pattern.length), pattern.pattern)) {
                p.size -> return i
                0 -> i++
                else -> i += matchLen - p[matchLen - 1]
            }
        }
        return -1
    }

    private fun rabinKarp(): Set<FileTypePattern> {
        val s = file.readBytes()
        val hashLength = patterns.minOf { it.pattern.length }
        val hashFn = rollingHash(A, M)
        val hashPatterns = patterns.groupBy { hashFn(it.pattern.take(hashLength).toByteArray(Charsets.US_ASCII)) }

        if (s.size < hashLength) {
            return emptySet()
        }
        val startIdx = s.lastIndex - hashLength + 1
        var subhash = hashFn(s.sliceArray(startIdx..s.lastIndex))
        val matches = mutableSetOf<FileTypePattern>()
        for (i in startIdx downTo 0) {
            matches.addAll(hashPatterns.getOrDefault(subhash, emptyList()).filter {
                val l = it.pattern.length
                i + l <= s.size && matchBytes(s.sliceArray(i until i + l), it.pattern) == l
            })

            if (i > 0) {
                subhash = Math.floorMod(((subhash
                        - s[i + hashLength - 1] * A.toDouble().pow(hashLength - 1).toInt()) * A + s[i - 1]), M)
            }
        }

        return matches
    }
}

data class FileTypePattern(val priority: Int, val pattern: String, val fileType: String)

fun loadPatterns(file: File): List<FileTypePattern> {
    val removeQuotes = { s: String -> s.replace("(^\")|(\"$)".toRegex(), "") }
    return file.useLines { lines ->
        lines.map { it.split(";") }.filter { it.size == 3 }.map {
            FileTypePattern(it[0].toInt(), removeQuotes(it[1]), removeQuotes(it[2]))
        }.sortedByDescending { it.priority }.toList()
    }
}

fun main(args: Array<String>) {

    if (args.size != 2) {
        println("Invalid arguments")
        exitProcess(1)
    }
    val (path, patternsPath) = args

    val patterns = loadPatterns(File(patternsPath))

    File(path).canonicalFile.walkTopDown().filter {it.isFile }.map {
        val t = Thread(FileTypeAnalyzer(it, patterns))
        t.start()
        t
    }.forEach { it.join() }
}
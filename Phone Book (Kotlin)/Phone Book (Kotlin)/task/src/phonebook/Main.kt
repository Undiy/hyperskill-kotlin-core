package phonebook

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Integer.min
import java.util.*
import kotlin.math.sqrt
import kotlin.streams.asSequence

const val FIND_FILENAME = "find.txt"
const val DIRECTORY_FILENAME = "directory.txt"

fun getRecordName(record: String) = record.substringAfter(" ", record)

fun linearSearchFn(value: String): Boolean {
    BufferedReader(FileReader(DIRECTORY_FILENAME)).use { directoryReader ->
        return directoryReader.lines().anyMatch {
                getRecordName(it) == value
        }
    }
}

fun jumpSearch(data: List<String>, value: String): Boolean {
    val blockSize = sqrt(data.size.toDouble()).toInt()
    for (i in 0..data.size / blockSize) {
        val boundary = min(data.lastIndex, (i + 1) * blockSize - 1)
        if (getRecordName(data[boundary]) >= value) {
            for (j in boundary downTo i * blockSize) {
                if (getRecordName(data[j]) == value) {
                    return true
                }
            }
        }
    }
    return false
}

fun binarySearch(data: List<String>, value: String): Boolean {
    if (data.isEmpty()) {
        return false
    }
    val pivotIndex = data.size / 2
    val pivot = getRecordName(data[pivotIndex])
    return if (pivot == value) {
        true
    } else if (pivot > value) {
        binarySearch(data.subList(0, pivotIndex), value)
    } else {
        binarySearch(data.subList(pivotIndex, data.size), value)
    }
}

fun search(searchFn: (String) -> Boolean) = search(searchFn, System.currentTimeMillis())
fun search(searchFn: (String) -> Boolean, start: Long): Long {
    var count = 0
    var found = 0
    BufferedReader(FileReader(FIND_FILENAME)).use { findReader ->
        findReader.lines().forEach {
            count++
            if (searchFn(it)) {
                found++
            }
        }
    }
    val time = System.currentTimeMillis() - start
    println("Found $found / $count entries. Time taken: ${formatTimeMillis(time)}")
    return time
}

fun bubbleAndJumpSearch(limit: Long): Long {
    val start = System.currentTimeMillis()
    val directory = File(DIRECTORY_FILENAME).readLines()
    val (isSorted, sortTime) = bubbleSort(directory, limit)

    val time = search(if (isSorted) {
         { jumpSearch(directory, it) }
    } else {
        ::linearSearchFn
    }, start)

    if (isSorted) {
        println("Sorting time: ${formatTimeMillis(sortTime)}")
    } else {
        println("Sorting time: ${formatTimeMillis(sortTime)} - STOPPED, moved to linear search")
    }
    println("Searching time: ${formatTimeMillis(time - sortTime)}")
    return time
}

fun formatTimeMillis(millis: Long) = "${millis / 1000_000} min. ${millis % 1000_000 / 1000} sec. ${millis % 1000} ms."

fun bubbleSort(data: List<String>, timeLimit: Long): Pair<Boolean, Long> {
    val start = System.currentTimeMillis()
    var isSorted: Boolean

    do {
        isSorted = true
        for (i in 0 until data.lastIndex) {
            val time = System.currentTimeMillis() - start
            if (time > timeLimit) {
                return Pair(false, time)
            }
            if (getRecordName(data[i]) > getRecordName(data[i + 1])) {
                isSorted = false
                Collections.swap(data, i, i + 1)
            }
        }
    } while (!isSorted)
    return Pair(true, System.currentTimeMillis() - start)
}

fun quickSort(data: List<String>) {
    if (data.size < 2) {
        return
    }
    val pivot = getRecordName(data.random())
    var boundary = 0
    for (i in data.indices) {
        if (getRecordName(data[i]) < pivot) {
            Collections.swap(data, i, boundary)
            boundary++
        }
    }
    quickSort(data.subList(0, boundary))
    quickSort(data.subList(boundary, data.size))
}

fun quickAndBinarySearch(): Long {
    val start = System.currentTimeMillis()
    val directory = File(DIRECTORY_FILENAME).readLines()
    quickSort(directory)

    val sortTime = System.currentTimeMillis() - start

    val time = search({ binarySearch(directory, it) }, start)

    println("Sorting time: ${formatTimeMillis(sortTime)}")
    println("Searching time: ${formatTimeMillis(time - sortTime)}")
    return time
}

fun hashSearch(): Long {
    val start = System.currentTimeMillis()
    val table = BufferedReader(FileReader(DIRECTORY_FILENAME)).use { directoryReader ->
        directoryReader.lines().asSequence().groupBy(
            ::getRecordName
        ) { it.substringBefore(" ", it) }
    }
    val createTime = System.currentTimeMillis() - start

    val time = search({ it in table }, start)

    println("Creating time: ${formatTimeMillis(createTime)}")
    println("Searching time: ${formatTimeMillis(time - createTime)}")
    return time
}

fun main() {
    println("Start searching (linear search)...")
    val linearTime = search(::linearSearchFn)
    println()
    println("Start searching (bubble sort + jump search)...")
    bubbleAndJumpSearch(linearTime * 10)
    println()
    println("Start searching (quick sort + binary search)...")
    quickAndBinarySearch()
    println("Start searching (hash table)...")
    hashSearch()
}

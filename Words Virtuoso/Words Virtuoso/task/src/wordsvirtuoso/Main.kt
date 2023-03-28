package wordsvirtuoso

import java.io.File

fun validateWord(word: String) = "[A-Za-z]{5}".toRegex().matchEntire(word) != null &&
        word.groupBy { it }.all { it.value.size == 1 }

fun main() {
    println("Input the words file:")
    val filename = readln()

    val file = File(filename)
    if (!file.exists()) {
        println("Error: The words file $filename doesn't exist.")
        return
    }

    val (_, invalid) = file.readLines().partition(::validateWord)
    if (invalid.isEmpty()) {
        println("All words are valid!")
    } else {
        println("Warning: ${invalid.size} invalid words were found in the $filename file.")
    }
}

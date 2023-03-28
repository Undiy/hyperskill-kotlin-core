package wordsvirtuoso

import java.io.File

fun hasWordInvalidSymbols(word: String) = "[A-Za-z]{5}".toRegex().matchEntire(word) == null
fun hasWordDuplicateSymbols(word: String) = word.groupBy { it }.any { it.value.size != 1}
fun isWordInvalid(word: String) = hasWordInvalidSymbols(word) || hasWordDuplicateSymbols(word)

fun error(message: String) {
    println("Error: $message")
    kotlin.system.exitProcess(1)
}

fun validateFile(i: Int, file: File): File {
    if (!file.exists()) {
        error("The ${if (i == 0) "" else "candidate "}words file ${file.name} doesn't exist.")
    }
    return file
}
fun readAndValidateWords(file: File): List<String> {
    return file.readLines().map(String::lowercase) .also { words ->
        words.count(::isWordInvalid).let {
            if (it > 0) error("$it invalid words were found in the ${file.name} file.")
        }
    }
}

fun gameLoop(secret: String, words : Set<String>): Boolean {
    println("\nInput a 5-letter word:")
    val input = readln().lowercase()
    if (input == secret) {
        println("\nCorrect!")
        return false
    }
    if (input == "exit") {
        println("\nThe game is over.")
        return false
    } else if (input.length != 5) {
        println("The input isn't a 5-letter word.")
    } else if (hasWordInvalidSymbols(input)) {
        println("One or more letters of the input aren't valid.")
    } else if (hasWordDuplicateSymbols(input)) {
        println("The input has duplicate letters.")
    } else if (input !in words) {
        println("The input word isn't included in my words list.")
    } else {
        println(input.mapIndexed { i, c ->
            when (c) {
                secret[i] -> c.uppercaseChar()
                in secret -> c
                else -> '_'
            }
        }.joinToString(""))
    }
    return true
}

fun main(args: Array<String>) {
    if (args.size != 2) {
        error("Wrong number of arguments.")
    }
    val (words, candidates) = args.map { File(it) }.mapIndexed(::validateFile).map(::readAndValidateWords)
        .map(List<String>::toSet)

    candidates.count { it !in words }.let {
        if (it > 0) error("$it candidate words are not included in the ${args[0]} file.")
    }

    println("Words Virtuoso")

    val secret = candidates.random()

    while (gameLoop(secret, words)) {}
}

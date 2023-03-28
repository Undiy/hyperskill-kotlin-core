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
    return file.readLines().map(String::uppercase) .also { words ->
        words.count(::isWordInvalid).let {
            if (it > 0) error("$it invalid words were found in the ${file.name} file.")
        }
    }
}

fun gameLoop(secret: String, words : Set<String>) = gameLoopRec(secret, words, emptyList(), emptySet())
fun gameLoopRec(secret: String, words : Set<String>, hints: List<String>, missingLetters: Set<Char>): Int {
    println("\nInput a 5-letter word:")
    val input = readln().uppercase()
    if (input == secret) {
        println()
        hints.forEach(::println)
        secret.forEach { print(color(it.toString(), COLOR_GREEN)) }
        println()
        println("\nCorrect!")
        return hints.size + 1
    }
    if (input == "EXIT") {
        println("\nThe game is over.")
        return 0
    } else if (input.length != 5) {
        println("The input isn't a 5-letter word.")
    } else if (hasWordInvalidSymbols(input)) {
        println("One or more letters of the input aren't valid.")
    } else if (hasWordDuplicateSymbols(input)) {
        println("The input has duplicate letters.")
    } else if (input !in words) {
        println("The input word isn't included in my words list.")
    } else {
        val (newHint, newMissingLetters) = input.foldIndexed(Pair(emptyList<String>(), missingLetters)) { i, acc, c ->
            val (hint, missing) = acc
            when (c) {
                secret[i] -> Pair(hint + color(c.toString(), COLOR_GREEN), missing)
                in secret -> Pair(hint + color(c.toString(), COLOR_YELLOW), missing)
                else -> Pair(hint + color(c.toString(), COLOR_GREY), missing + c)
            }
        }
        val newHints = hints + newHint.joinToString("")

        println()
        newHints.forEach(::println)
        println()
        println(color(newMissingLetters.map(Char::uppercaseChar).sorted().joinToString(""), COLOR_AZURE))
        return gameLoopRec(secret, words, newHints, newMissingLetters)
    }
    // get input one more time
    return gameLoopRec(secret, words, hints, missingLetters)
}

const val COLOR_RESET = "\u001B[0m"
const val COLOR_GREEN = "\u001B[48:5:10m"
const val COLOR_YELLOW = "\u001B[48:5:11m"
const val COLOR_GREY = "\u001B[48:5:7m"
const val COLOR_AZURE = "\u001B[48:5:14m"

fun color(str: String, color:String): String = "$color$str$COLOR_RESET"

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
    val start = System.currentTimeMillis()

    val tries = gameLoop(secret, words)

    val seconds = (System.currentTimeMillis() - start) / 1000

    if (tries == 1) {
        println("Amazing luck! The solution was found at once.")
    } else if (tries > 1) {
        println("The solution was found after $tries tries in $seconds seconds.")
    }
}

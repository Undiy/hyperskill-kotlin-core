package encryptdecrypt

import java.io.File

val alphabet = ('a'..'z').joinToString("")

fun shiftAlg(key: Int): (Char) -> Char = {
    val index = alphabet.indexOf(it.lowercaseChar())

    if (index < 0) {
        it
    } else {
        val res = alphabet[((index + key) % alphabet.length + alphabet.length) % alphabet.length]
        if (it.isUpperCase()) res.uppercaseChar() else res
    }
}
fun unicodeAlg(key: Int): (Char) -> Char = { (it.code + key).toChar() }

fun encrypt(s: String, key: Int, alg : (Int) -> (Char) -> Char): String {
    return s.map(alg(key)).joinToString("")
}

fun main(args: Array<String>) {
    var isEncMode = true
    var key = 0
    var data = ""
    var out = ""
    var isShiftAlg = true
    for (i in 0 until args.size - 1 step 2) {
        when (args[i]) {
            "-mode" -> isEncMode = args[i + 1] == "enc"
            "-key" -> key = args[i + 1].toInt()
            "-data" -> data = args[i + 1]
            "-in" -> data = File(args[i + 1]).readText()
            "-out" -> out = args[i + 1]
            "-alg" -> isShiftAlg = args[i + 1] == "shift"
        }
    }
    val res = encrypt(
        data,
        if (isEncMode) key else -key,
        if (isShiftAlg) ::shiftAlg else ::unicodeAlg
    )
    if (out == "") {
        println(res)
    } else {
        File(out).writeText(res)
    }
}
package tictactoe

import java.lang.NumberFormatException
import java.lang.RuntimeException
import kotlin.math.abs

const val DIMENSION = 3

class TicTacToeException(message: String) : RuntimeException(message)

fun printField(field: MutableList<MutableList<Char>>) {
    println("---------")
    for (y in 0 until DIMENSION) {
        for (x in 0 until DIMENSION) {

            if (x == 0) {
                print("|")
            }
            print(' ')
            print(field[y][x])
        }
        println(" |")
    }
    println("---------")
}

fun initField(s: String): MutableList<MutableList<Char>> {
    return MutableList(DIMENSION){ y ->
        MutableList(DIMENSION){ x -> s[y * DIMENSION + x]}
    }
}

fun checkField(field: MutableList<MutableList<Char>>): Boolean {
    val (xs, zeros, emptyCells) = field.fold(Triple(0, 0, 0)) { acc, row ->
        row.fold(acc) { acc2, cell ->
            when (cell) {
                'X' -> Triple(acc2.first + 1, acc2.second, acc2.third)
                'O' -> Triple(acc2.first, acc2.second + 1, acc2.third)
                else -> Triple(acc2.first, acc2.second, acc2.third + 1)
            }
        }
    }

    if (abs(xs - zeros) > 1) {
        throw TicTacToeException("Impossible")
    }

    val diagWinner = if (field[1][1] == '_') {
        '_'
    } else if (field[1][1] == field[0][0] && field[1][1] == field[2][2]) {
        field[1][1]
    } else if (field[1][1] == field[0][2] && field[1][1] == field[2][0]) {
        field[1][1]
    } else {
        '_'
    }

    if (diagWinner != '_') {
        println("$diagWinner wins")
        return true
    }

    val xWinner = field.fold('_') { acc, row ->
        if (row[0] != '_' && row[0] == row[1] && row[1] == row[2]) {
            if (acc != '_' && row[0] != acc) {
                throw TicTacToeException("Impossible")
            }
            row[0]
        } else {
            acc
        }
    }

    if (xWinner != '_') {
        println("$xWinner wins")
        return true
    }

    val yWinner = field.first().indices.fold('_') { acc, colIndex ->
        if (field[0][colIndex] != '_' && field[0][colIndex] == field[1][colIndex] && field[1][colIndex] == field[2][colIndex]) {
            if (acc != '_' && field[0][colIndex] != acc) {
                throw TicTacToeException("Impossible")
            }
            field[0][colIndex]
        } else {
            acc
        }
    }

    if (yWinner != '_') {
        println("$yWinner wins")
        return true
    }

    if (emptyCells != 0) {
//        println("Game not finished")
        return false
    } else {
        println("Draw")
        return true
    }
}

fun makeMove(field: MutableList<MutableList<Char>>, isCross: Boolean) {
    while (true) {
        try {
            val (y, x) = readln().split(" ", limit = 2).map{ s -> s.toInt() }
            if (x !in 1..3 || y !in 1..3) {
                println("Coordinates should be from 1 to 3!")
            } else if (field[y - 1][x - 1] != '_') {
                println("This cell is occupied! Choose another one!")
            } else {
                field[y - 1][x - 1] = if (isCross) 'X' else 'O'
                break
            }
        } catch (e: NumberFormatException) {
            println("You should enter numbers!")
        }
    }
}

fun main() {
    val field = initField("_________")
    printField(field)

    var isCross = true

    while(!checkField(field)) {
        makeMove(field, isCross)
        printField(field)
        isCross = !isCross
    }
}
package connectfour

class ConnectFourBoard(
    val player1: String,
    val player2: String,
    private val rows: Int,
    cols: Int,
    val numberOfGames: Int
) {

    private val board = Array<MutableList<Char>>(cols) { mutableListOf() }
    private val cols get() = board.size

    private var isFirstPlayer = true
    private val playerName get() = if (isFirstPlayer) player1 else player2
    private val playerSymbol get() = if (isFirstPlayer) 'o' else '*'

    var player1Score = 0
    var player2Score = 0
    var gamesPlayed = 0

    fun printStart() {
        println("$player1 VS $player2")
        println("$rows X $cols board")
        if (numberOfGames == 1) {
            println("Single game")
        } else {
            println("Total $numberOfGames games")
        }
    }

    fun printBoard() {
        for (col in 0 until cols) {
            print(" ${col + 1}")
        }
        println()
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                print("║")
                val index = rows - row - 1
                if (index > board[col].lastIndex) {
                    print(" ")
                } else {
                    print(board[col][index])
                }
            }
            println('║')
        }
        for (col in 0 until cols) {
            if (col == 0) {
                print('╚')
            } else {
                print('╩')
            }
            print('═')

        }
        println('╝')
    }

    fun makeTurn(): Boolean {
        while (true) {
            println("$playerName's turn:")
            val input = readln()
            if (input == "end") {
                gamesPlayed++
                return false
            }
            val col = try {
                input.toInt()
            } catch (e: NumberFormatException) {
                println("Incorrect column number")
                continue
            } catch (e: Exception) {
                println("Something went wrong")
                continue
            }
            if (col !in 1..cols) {
                println("The column number is out of range (1 - $cols)")
            } else if (board[col - 1].size == rows) {
                println("Column $col is full")
            } else {
                board[col - 1].add(playerSymbol)
                printBoard()
                return !checkIfPlayerWins(col)
            }
        }
    }

    private fun checkIfPlayerWins(col: Int): Boolean {
        val row = board[col - 1].size

        val diagonalFoldFn = { rowAcc : Pair<Int, Int>, column : MutableList<Char> ->
            val (row, acc) = rowAcc
            Pair(
                row - 1,
                if (acc >= 4) {
                    acc
                } else if (row !in 1 .. column.size || column[row - 1] != playerSymbol) {
                    0
                } else {
                    acc + 1
                }
            )
        }

        val playerWins =
            // by row
            board.fold(0) { acc, column ->
                if (acc >= 4) {
                    acc
                } else if (column.size < row || column[row - 1] != playerSymbol) {
                    0
                } else {
                    acc + 1
                }
            } >= 4 ||
            // col
            board[col - 1].takeLastWhile { it == playerSymbol }.size >= 4 ||
            // diagonals
            board.fold(Pair(row + col - 1, 0), diagonalFoldFn).second >= 4 ||
            board.foldRight(Pair(row + cols - col, 0)) {
                    column, rowAcc -> diagonalFoldFn(rowAcc, column)
            }.second >= 4

        if (playerWins) {
            println("Player $playerName won")
            gamesPlayed++
            if (isFirstPlayer) {
                player1Score += 2
            } else {
                player2Score += 2
            }
            return true
        }

        if (board.all { it.size == rows }) {
            println("It is a draw")
            player1Score++
            player2Score++
            gamesPlayed++
            return true
        }
        isFirstPlayer = !isFirstPlayer
        return false
    }

    fun reset() {
        board.forEach { it.clear() }
        isFirstPlayer = gamesPlayed % 2 == 0
    }
}

fun readDimensions(): Pair<Int, Int> {
    while (true) {
        println("Set the board dimensions (Rows x Columns)")
        println("Press Enter for default (6 x 7)")

        val input = readln()
        if (input == "") {
            return Pair(6, 7)
        }
        val (rows, cols) = try {
            input.lowercase().split("x", limit = 2).map { it.trim().toInt() }
        } catch (e: NumberFormatException) {
            println("Invalid input")
            continue
        }

        if (rows !in 5..9) {
            print("Board rows should be from 5 to 9")
        } else if (cols !in 5..9) {
            println("Board columns should be from 5 to 9")
        } else {
            return Pair(rows, cols)
        }
    }
}

fun readNumberOfGames(): Int {
    while (true) {
        println(
            """
            Do you want to play single or multiple games?
            For a single game, input 1 or press Enter
            Input a number of games:""".trimIndent()
        )

        val input = readln()
        val n = if (input == "") {
            1
        } else {
            try {
                input.toInt()
            } catch (e: java.lang.NumberFormatException) {
                -1
            }
        }
        if (n <= 0) {
            println("Invalid input")
        } else {
            return n
        }
    }
}

fun main() {
    println("Connect Four")

    println("First player's name:")
    val player1 = readln()

    println("Second player's name:")
    val player2 = readln()

    val (rows, cols) = readDimensions()
    val n = readNumberOfGames()

    val board = ConnectFourBoard(player1, player2, rows, cols, n)

    board.printStart()
    while (board.gamesPlayed < board.numberOfGames) {
        if (board.numberOfGames > 1) {
            println("Game #${board.gamesPlayed + 1}")
        }
        board.printBoard()
        while (board.makeTurn()) {}
        if (board.numberOfGames > 1) {
            println("Score")
            println("${board.player1}: ${board.player1Score} ${board.player2}: ${board.player2Score}")
            board.reset()
        }
    }

    println("Game over!")
}
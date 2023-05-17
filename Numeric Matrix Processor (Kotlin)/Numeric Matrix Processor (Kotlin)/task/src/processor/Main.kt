package processor

import java.lang.RuntimeException
import java.text.DecimalFormat
import kotlin.math.pow

class MatrixException(message: String) : RuntimeException(message)

val df = DecimalFormat("#.##")

class Matrix (
    private val rows: Int,
    private val cols: Int
) {
    private val cells = Array(rows) { DoubleArray(cols) }

    private fun getRow(m: Int) = cells[m].toList()
    private fun getCol(n: Int) = cells.map { it[n] }

    companion object {
        fun readMatrix(name: String = ""): Matrix {
            val name = if (name != "") "$name " else name
            print("Enter size of ${name}matrix:")
            val (rows, cols) = readln().split(" ", limit = 2).map(String::toInt)
            println("Enter ${name}matrix:")
            val matrix = Matrix(rows, cols)
            for (y in 0 until rows) {
                val row = readln().split(" ").map(String::toDouble)
                for (x in 0 until cols) {
                    matrix.cells[y][x] = row[x]
                }
            }
            return matrix
        }
    }

    override fun toString() = cells.joinToString("\n") { it.joinToString(" ", transform = df::format)}

    operator fun plus(other: Matrix): Matrix {
        if (rows != other.rows || cols != other.cols) {
            throw MatrixException("Can't add matrices with different dimensions")
        }
        val matrix = Matrix(rows, cols)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                matrix.cells[y][x] = cells[y][x] + other.cells[y][x]
            }
        }
        return matrix
    }

    operator fun times(scalar: Double): Matrix {
        val matrix = Matrix(rows, cols)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                matrix.cells[y][x] = cells[y][x] * scalar
            }
        }
        return matrix
    }

    operator fun times(other: Matrix): Matrix {
        if (cols != other.rows) {
            throw MatrixException("Can't multiply matrices")
        }
        val matrix = Matrix(rows, other.cols)
        for (y in 0 until other.cols) {
            for (x in 0 until rows) {
                matrix.cells[x][y] = getRow(x).zip(other.getCol(y), Double::times).sum()
            }
        }
        return matrix
    }

    fun transpose(): Matrix {
        val matrix = Matrix(cols, rows)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                matrix.cells[x][y] = cells[y][x]
            }
        }
        return matrix
    }

    fun transposeSide(): Matrix {
        val matrix = Matrix(cols, rows)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                matrix.cells[cols - 1 - x][rows - 1 - y] = cells[y][x]
            }
        }
        return matrix
    }

    fun transposeVertical(): Matrix {
        val matrix = Matrix(rows, cols)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                matrix.cells[rows - 1 - y][x] = cells[y][x]
            }
        }
        return matrix
    }

    fun transposeHorizontal(): Matrix {
        val matrix = Matrix(rows, cols)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                matrix.cells[y][cols - 1 - x] = cells[y][x]
            }
        }
        return matrix
    }

    fun determinant(): Double {
        return (0 until rows).fold(listOf(Pair(1.0, this))) { acc, _ ->
            acc.flatMap {
                val (d, m) = it
                (0 until m.cols).map { x ->
                    Pair(d * m.cells[0][x] * if (x % 2 == 0) 1 else -1, m.minor(0, x))
                }
            }
        }.sumOf { it.first }
    }

    private fun minor(m: Int, n: Int): Matrix {
        val matrix = Matrix(rows - 1, cols - 1)
        for (y in 0 until  rows) {
            for (x in 0 until cols) {
                val ys = if (y < m) y else if (y > m) y - 1 else continue
                val xs = if (x < n) x else if (x > n) x - 1 else continue
                matrix.cells[ys][xs] = cells[y][x]
            }
        }
        return matrix
    }

    fun inverse(): Matrix {
        val d = determinant()
        val matrix = Matrix(cols, rows)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                matrix.cells[x][y] = (1 / d) * (-1.0).pow(y + 1 + x + 1) * minor(y, x).determinant()
            }
        }
        return matrix
    }
}

operator fun Double.times(matrix: Matrix): Matrix = matrix * this

fun addMatrices() {
    val m1 = Matrix.readMatrix("first")
    val m2 = Matrix.readMatrix("second")
    val result = try {
        "The result is:\n${m1 + m2}"
    } catch (e: MatrixException) {
        "The operation cannot be performed."
    }
    println(result)
}

fun multiplyByScalar() {
    val m = Matrix.readMatrix()
    print("Enter constant:")
    val scalar = readln().toDouble()
    println("The result is:")
    print(scalar * m)
}

fun multiplyByMatrix() {
    val m1 = Matrix.readMatrix("first")
    val m2 = Matrix.readMatrix("second")
    val result = try {
        "The result is:\n${m1 * m2}"
    } catch (e: MatrixException) {
        "The operation cannot be performed."
    }
    println(result)
}

fun transpose() {
    print("""
        1. Main diagonal
        2. Side diagonal
        3. Vertical line
        4. Horizontal line
        Your choice:""".trimIndent())
    val m = when (readln()) {
        "1" -> Matrix.readMatrix().transpose()
        "2" -> Matrix.readMatrix().transposeSide()
        "3" -> Matrix.readMatrix().transposeHorizontal()
        "4" -> Matrix.readMatrix().transposeVertical()
        else -> Matrix.readMatrix()
    }
    println("The result is:")
    println(m)
}

fun determinant() {
    val m = Matrix.readMatrix()
    val d = m.determinant()
    println("The result is:")
    println(df.format(d))
}

fun inverseMatrix() {
    val m = Matrix.readMatrix()
    println("The result is:")
    println(m.inverse())
}

fun menu() {
    while (true) {
        print("""
            1. Add matrices
            2. Multiply matrix by a constant
            3. Multiply matrices
            4. Transpose matrix
            5. Calculate a determinant 
            6. Inverse matrix
            0. Exit
            Your choice:""".trimIndent())
        when (readln()) {
            "1" -> addMatrices()
            "2" -> multiplyByScalar()
            "3" -> multiplyByMatrix()
            "4" -> transpose()
            "5" -> determinant()
            "6" -> inverseMatrix()
            "0" -> return
        }
        println()
    }
}

fun main() {
    menu()
}

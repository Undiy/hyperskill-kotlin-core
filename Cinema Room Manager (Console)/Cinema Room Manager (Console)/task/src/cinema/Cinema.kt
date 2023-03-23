package cinema

class Cinema(rows: Int, cols: Int) {
    private val seats = MutableList<MutableList<Boolean>>(rows) { _ ->
        MutableList<Boolean>(cols) { _ -> false }
    }
    private var ticketsSold = 0
    private var income = 0
    private val totalIncome: Int

    val buyTicket: () -> Unit

    init {
        val getTicketPrice = if (rows * cols < 60) {
            { _ : Int  -> 10 }
        } else {
            { row -> if (row > rows / 2) 8 else 10 }
        }
        totalIncome = seats.foldIndexed(0){ row, acc, _ ->
            acc + getTicketPrice(row + 1) * cols
        }
        buyTicket = {
            while (true) {
                println()
                println("Enter a row number:")
                val row = readln().toInt()
                println("Enter a seat number in that row:")
                val col = readln().toInt()

                if (row !in 1..rows || col !in 1..cols) {
                    println("\nWrong input!")
                } else if (seats[row - 1][col - 1]) {
                    println("\nThat ticket has already been purchased!")
                } else {
                    val price = getTicketPrice(row)

                    seats[row - 1][col - 1] = true
                    ticketsSold += 1
                    income += price

                    println("\nTicket price: $$price")
                    break
                }
            }
        }

    }

    fun printSeats() {
        println("\nCinema:")
        // cols
        print(' ')
        for (col in seats.first().indices) {
            print(" ${col + 1}")
        }
        println()

        // rows
        for (row in seats.indices) {
            for (col in seats[row].indices) {
                if (col == 0) {
                    print("${row + 1}")
                }
                if (seats[row][col]) {
                    print(" B")
                } else {
                    print(" S")
                }
            }
            println()
        }
    }

    fun showStatistics() {
        println()
        println("Number of purchased tickets: $ticketsSold")
        println("Percentage: ${"%.2f".format(
            ticketsSold.toDouble() * 100.0 / (seats.size * seats.first().size).toDouble()
        )}%")
        println("Current income: $$income")
        println("Total income: $$totalIncome")
    }
}

fun main() {

    println("Enter the number of rows:")
    val rows = readln().toInt()
    println("Enter the number of seats in each row:")
    val cols = readln().toInt()

    val cinema = Cinema(rows, cols)

    while (true) {
        println()
        println(
            """
            1. Show the seats
            2. Buy a ticket
            3. Statistics
            0. Exit""".trimIndent()
        )
        when (readln().toInt()) {
            0 -> break
            1 -> cinema.printSeats()
            2 -> cinema.buyTicket()
            3 -> cinema.showStatistics()
        }
    }
}
package cinema

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.UUID.randomUUID
import java.util.concurrent.ConcurrentHashMap


const val PASSWORD = "super_secret"

@SpringBootApplication
open class Application

data class CinemaRoomTicket(val row: Int, val column: Int) {
    val price = if (row <= 4) 10 else 8
}

data class CinemaRoomStats(
    @get:JsonProperty("current_income")
    val totalIncome: Int,
    @get:JsonProperty("number_of_available_seats")
    val availableSeats: Int,
    @get:JsonProperty("number_of_purchased_tickets")
    val purchasedSeats: Int
)

object CinemaRoomInfo {
    @get:JsonProperty("total_rows")
    val totalRows: Int = 9
    @get:JsonProperty("total_columns")
    val totalColumns: Int = 9
    @JsonGetter("available_seats")
    fun availableSeats(): List<CinemaRoomTicket> {
        return (1..totalRows).flatMap { row -> (1..totalColumns).map { col ->
            CinemaRoomTicket(row, col)
        } }.filter(::isTicketAvailable)
    }

    private val tickets: MutableMap<CinemaRoomTicket, String> = ConcurrentHashMap(totalRows * totalColumns)
    fun isTicketAvailable(ticket: CinemaRoomTicket) = !tickets.containsKey(ticket)
    fun purchase(ticket: CinemaRoomTicket): String {
        if (ticket.row !in 1..totalRows || ticket.column !in 1..totalColumns) {
            throw RuntimeException("The number of a row or a column is out of bounds!")
        }
        return randomUUID().toString().also { tickets[ticket] = it }
    }
    fun returnTicket(token: String) = tickets.entries.find { it.value == token }?.key?.also {
        tickets.remove(it)
    }
    fun stats() = CinemaRoomStats(
        tickets.keys.fold(0) { acc, ticket -> acc + ticket.price },
        totalColumns * totalRows - tickets.size,
        tickets.size
    )
}

fun <T>responseOK(v: T) = ResponseEntity(v, HttpStatus.OK)
fun responseError(error: String): ResponseEntity<Any> = ResponseEntity(mapOf(Pair("error", error)), HttpStatus.BAD_REQUEST)
fun responseUnauth(): ResponseEntity<Any> = ResponseEntity(mapOf(Pair("error", "The password is wrong!")), HttpStatus.UNAUTHORIZED)

@RestController
class CinemaRoomController {
    @GetMapping("/seats")
    fun seats(): CinemaRoomInfo {
        return CinemaRoomInfo
    }

    @PostMapping("/purchase")
    fun purchase(@RequestBody ticket: CinemaRoomTicket): ResponseEntity<Any> {
        return try {
            if (CinemaRoomInfo.isTicketAvailable(ticket)) {
                val token = CinemaRoomInfo.purchase(ticket)
                responseOK(mapOf(Pair("token", token), Pair("ticket", ticket)))
            } else {
                responseError("The ticket has been already purchased!")
            }
        } catch (e: RuntimeException) {
            responseError("The number of a row or a column is out of bounds!")
        }
    }

    @PostMapping("/return")
    fun returnTicket(@RequestBody token: Map<String, String>): ResponseEntity<Any> {
        return CinemaRoomInfo.returnTicket(token["token"] ?: "")?.let {
            responseOK(mapOf(Pair("returned_ticket", it)))
        } ?: responseError("Wrong token!")
    }

    @PostMapping("/stats")
    fun stats(@RequestParam password: String): ResponseEntity<Any> {
        return if (password == PASSWORD) {
            responseOK(CinemaRoomInfo.stats())
        } else {
            responseUnauth()
        }
    }
}

@ControllerAdvice
class CinemaRoomExceptionHandler : ResponseEntityExceptionHandler() {
  override fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        return if (request.getDescription(false).endsWith("/stats")) {
            responseUnauth()
        } else {
            responseError(ex.message ?: "Missing request parameter")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
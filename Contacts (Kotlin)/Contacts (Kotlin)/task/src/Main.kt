package contacts

import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

abstract class Contact {
    private val created: Instant
    private var lastEdit: Instant
    var number: String? = null
        set(value) {
            field = if (value != null && validatePhoneNumber(value)) {
                value
            } else {
                println("Wrong number format!")
                null
            }
        }

    init {
        created = Clock.System.now()
        lastEdit = created
    }

    private val phoneRegex = """\+?(\w+([ \-]\(\w{2,}\))?|(\(\w+\)))((\s|-)\w{2,})*""".toRegex()

    private fun validatePhoneNumber(phoneNumber: String) = phoneRegex.matchEntire(phoneNumber) != null

    abstract val fullName: String

    open fun edit() {
        lastEdit = Clock.System.now()
    }

    private fun formatDateTime(dateTime: Instant) = dateTime.toLocalDateTime(TimeZone.currentSystemDefault())
        .toString().takeWhile { it != '.' }

    open fun info() {
        println("Number: ${valueOrNoData(number, "[no number]")}")
        println("Time created:: ${formatDateTime(created)}")
        println("Time last edit: ${formatDateTime(lastEdit)}")
    }

    fun valueOrNoData(value: Any?) : String {
        return valueOrNoData(value, "[no data]")
    }

    private fun valueOrNoData(value: Any?, noData: String) : String {
        return value?.toString() ?: noData
    }
}

class PersonContact : Contact() {
    private var name: String
    private var surname: String

    private var birthDate: LocalDate? = null
    private fun setBirthDate(date: String) {
        try {
            birthDate = date.toLocalDate()
        } catch (e: RuntimeException) {
            println("Bad birth date!")
        }
    }
    private var gender: String? = null
        set(value) {
            field = if (value != null && value in arrayOf("M", "F")) {
                value
            } else {
                println("Bad gender!")
                null
            }
        }

    init {
        print("Enter the name: ")
        this.name = readln()
        print("Enter the surname: ")
        this.surname = readln()
        print("Enter the birth date: ")
        setBirthDate(readln())
        print("Enter the gender (M, F): ")
        this.gender = readln()
        print("Enter the number: ")
        this.number = readln()
    }

    override val fullName get() = "$name $surname"

    override fun edit() {
        print("Select a field (name, surname, birth, gender, number): ")
        when(readln()) {
            "name" -> {
                print("Enter name: ")
                name = readln()
            }
            "surname" -> {
                print("Enter surname: ")
                surname = readln()
            }
            "birth" -> {
                print("Enter birth: ")
                setBirthDate(readln())
            }
            "gender" -> {
                print("Enter gender: ")
                gender = readln()
            }
            "number" -> {
                print("Enter number: ")
                number = readln()
            }
        }
        super.edit()
    }

    override fun info() {
        println("Name: $name")
        println("Surname: $surname")
        println("Birth date: ${valueOrNoData(birthDate)}")
        println("Gender: ${valueOrNoData(gender)}")
        super.info()
    }
}

class OrganizationContact : Contact() {
    private val name: String
    private var address: String

    init {
        print("Enter the organization name: ")
        this.name = readln()
        print("Enter the address: ")
        this.address = readln()
        print("Enter the number: ")
        this.number = readln()
    }

    override val fullName get() = name

    override fun edit() {
        print("Select a field (address, number): ")
        when(readln()) {
            "address" -> {
                print("Enter address: ")
                address = readln()
            }
            "number" -> {
                print("Enter number: ")
                number = readln()
            }
        }
        super.edit()
    }

    override fun info() {
        println("Organization name: $name")
        println("Address: $address")
        super.info()
    }
}

class Contacts {
    private val records = mutableListOf<Contact>()

    private fun selectRecord(noRecordsMessage: String): Int? {
        if (records.isEmpty()) {
            println(noRecordsMessage)
            return null
        }
        list()
        print("Select a record: ")
        return readln().toInt() - 1
    }

    fun add() {
        println("Enter the type (person, organization): ")
        when (readln()) {
            "person" -> records.add(PersonContact())
            "organization" -> records.add(OrganizationContact())
            else -> return
        }
        println("The record added.")
    }

    fun remove() {
        val i = selectRecord("No records to remove!")
        if (i != null) {
            records.removeAt(i)
            println("The record removed!")
        }
    }

    fun edit() {
        val i = selectRecord("No records to edit!")
        if (i != null) {
            records[i].edit()
            println("The record updated!")
        }
    }

    fun count() {
        println("The Phone Book has ${records.size} records.")
    }

    fun info() {
        val i = selectRecord("No records to show info!")
        if (i != null) {
            records[i].info()
        }
    }

    fun list() {
        records.forEachIndexed { i, r -> println("${i + 1}. ${r.fullName}") }
    }
 }

fun main() {
    val contacts = Contacts()

    while (true) {
        println("Enter action (add, remove, edit, count, info, list, exit):")
        when (readln()) {
            "add" -> contacts.add()
            "remove" -> contacts.remove()
            "edit" -> contacts.edit()
            "count" -> contacts.count()
            "info" -> contacts.info()
            "list" -> contacts.list()
            "exit" -> break
        }
        println()
    }
}
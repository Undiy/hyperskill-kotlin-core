package contacts

class Contact {
    var name: String
        get
        set
    var surname: String = ""
        get
        set
    var number: String? = null
        get
        set(value) {
            field = if (value != null && validatePhoneNumber(value)) {
                value
            } else {
                println("Wrong number format!")
                null
            }
        }

    override fun toString(): String {
        return "$name $surname, ${number ?: "[no number]"}"
    }

    private val phoneRegex = """\+?(\w+([ \-]\(\w{2,}\))?|(\(\w+\)))((\s|-)\w{2,})*""".toRegex()

    private fun validatePhoneNumber(phoneNumber: String) = phoneRegex.matchEntire(phoneNumber) != null

    init {
        print("Enter the name: ")
        this.name = readln()
        print("Enter the surname: ")
        this.surname = readln()
        print("Enter the number: ")
        this.number = readln()
    }
}

class Contacts {
    private val records = mutableListOf<Contact>()

    fun add() {
        records.add(Contact())
        println("The record added.")
    }

    fun remove() {
        if (records.isEmpty()) {
            println("No records to remove!")
        } else {
            list()
            print("Select a record: ")
            records.removeAt(readln().toInt() - 1)
            println("The record removed!")
        }
    }

    fun edit() {
        if (records.isEmpty()) {
            println("No records to edit!")
        } else {
            list()
            print("Select a record: ")
            val record = records[readln().toInt() - 1]
            print("Select a field (name, surname, number): ")
            when(readln()) {
                "name" -> {
                    print("Enter name: ")
                    record.name = readln()
                }
                "surname" -> {
                    print("Enter surname: ")
                    record.surname = readln()
                }
                "number" -> {
                    print("Enter number: ")
                    record.number = readln()
                }
            }
            println("The record updated!")
        }
    }

    fun count() {
        println("The Phone Book has ${records.size} records.")
    }

    fun list() {
        records.forEachIndexed { i, r -> println("${i + 1}. $r") }
    }
 }

fun main() {
    val contacts = Contacts()

    while (true) {
        println("Enter action (add, remove, edit, count, list, exit):")
        when (readln()) {
            "add" -> contacts.add()
            "remove" -> contacts.remove()
            "edit" -> contacts.edit()
            "count" -> contacts.count()
            "list" -> contacts.list()
            "exit" -> break
        }
    }
}
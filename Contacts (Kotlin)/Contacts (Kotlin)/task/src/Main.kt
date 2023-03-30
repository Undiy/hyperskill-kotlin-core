package contacts

import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.lang.RuntimeException

sealed class Contact {
    protected var created: String
    protected var lastEdit: String
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
        created = formatDateTime(Clock.System.now())
        lastEdit = created
    }

    private val phoneRegex = """\+?(\w+([ \-]\(\w{2,}\))?|(\(\w+\)))((\s|-)\w{2,})*""".toRegex()

    private fun validatePhoneNumber(phoneNumber: String) = phoneRegex.matchEntire(phoneNumber) != null

    abstract val fullName: String
    open val searchData get() = listOf(number ?: "")

    open fun edit() {
        lastEdit = formatDateTime(Clock.System.now())
    }

    private fun formatDateTime(dateTime: Instant) = dateTime.toLocalDateTime(TimeZone.currentSystemDefault())
        .toString().takeWhile { it != '.' }

    open fun info() {
        println("Number: ${valueOrNoData(number, "[no number]")}")
        println("Time created: $created")
        println("Time last edit: $lastEdit")
    }

    fun valueOrNoData(value: Any?) : String {
        return valueOrNoData(value, "[no data]")
    }

    private fun valueOrNoData(value: Any?, noData: String) : String {
        return value?.toString() ?: noData
    }

    abstract val type: ContactType
}

class PersonContact : Contact() {
    private var name: String = ""
    private var surname: String = ""

    private var birthDate: String? = null
        set(value) {
            field = try {
                value?.toLocalDate().toString()
            } catch (e: RuntimeException) {
                println("Bad birth!")
                null
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

    override val fullName get() = "$name $surname"
    override val searchData get() = (listOf(
        name, surname, birthDate ?: "", gender ?: ""
    ) + super.searchData)

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
                birthDate = readln()
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

    override val type get() = ContactType.PersonType

    companion object {
        fun read(): PersonContact {
            val contact = PersonContact()
            print("Enter the name: ")
            contact.name = readln()
            print("Enter the surname: ")
            contact.surname = readln()
            print("Enter the birth date: ")
            contact.birthDate = readln()
            print("Enter the gender (M, F): ")
            contact.gender = readln()
            print("Enter the number: ")
            contact.number = readln()
            return contact
        }
    }
}

class OrganizationContact : Contact() {
    private var name: String = ""
    private var address: String = ""

    override val fullName get() = name
    override val searchData get() = (listOf(
        name, address
    ) + super.searchData)

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

    override val type get() = ContactType.OrganizationType

    companion object {
        fun read(): OrganizationContact {
            val contact = OrganizationContact()
            print("Enter the organization name: ")
            contact.name = readln()
            print("Enter the address: ")
            contact.address = readln()
            print("Enter the number: ")
            contact.number = readln()

            contact.info()

            return contact
        }
    }
}

enum class ContactsMenu {
    Menu,
    List,
    Search,
    Record,
    Exit
}

class Contacts {
    private val records = mutableListOf<Contact>()

    private fun makeMenu(
        menuType: ContactsMenu,
        recordHandlers: Map<String, () -> ContactsMenu>,
        handlers: Map<String, () -> ContactsMenu>
    ): () -> ContactsMenu {
        val actions = ((if (recordHandlers.isEmpty()) emptyList() else listOf("[number]"))
                + handlers.keys).joinToString()
        val menu = {
            println("\n[${menuType.name.lowercase()}] Enter action ($actions): ")
            val a = readln()
            val res = (recordHandlers + handlers)[a]?.invoke() ?: menuType
//            println("action done: $a")
//            println("next: $menuType -> $res")
            res
        }
        return {
            menuRec(menuType, menuType, menu)
        }
    }

    private fun menuRec(current: ContactsMenu, dest: ContactsMenu, menu : () -> ContactsMenu): ContactsMenu {
//        println("$current -> $dest")

        return if (current != dest) {
//            println("return $dest")
            dest
        } else {
//            println("step in $current")
            menuRec(current, menu(), menu)
        }
    }

    private fun makeRecordMenus() = makeRecordMenus(records)
    private fun makeRecordMenus(rs: List<Contact>): Map<String, () -> ContactsMenu> {
        return rs.foldIndexed(emptyMap()) { i, acc, record ->
            acc + Pair((i + 1).toString(), makeRecordMenu(record))
        }
    }
    private fun makeRecordMenu(record: Contact): () -> ContactsMenu {
        return {
            record.info()
            makeMenu(
                ContactsMenu.Record,
                emptyMap(),
                mapOf(
                    "edit" to {
                        record.edit()
                        println("The record updated!")
                        ContactsMenu.Menu
                    },
                    "delete" to {
                        records.remove(record)
                        println("The record removed!")
                        ContactsMenu.Menu
                    },
                    "menu" to {
                        ContactsMenu.Menu
                    }
                )
            )()
        }
    }

    private fun printRecords() = printRecords(records)
    private fun printRecords(rs: List<Contact>) {
        rs.forEachIndexed{ i, record ->
            println("${i + 1} ${record.fullName}")
        }
    }
    private fun makeListMenu(): () -> ContactsMenu {
        return {
            printRecords()
            makeMenu(
                ContactsMenu.List,
                makeRecordMenus(),
                mapOf(
                    "back" to { ContactsMenu.Menu }
                )
            )()
        }
    }

    private fun search(query: String) : List<Contact> {
        val regex = query.lowercase().toRegex()
        return records.filter { record ->
            regex.containsMatchIn(record.searchData.filter { it != "" }.joinToString(" ", transform = String::lowercase))
        }

    }
    private fun makeSearchMenu(): () -> ContactsMenu {
        return {
            print("Enter search query: ")
            val results = search(readln())
            println("Fount ${results.size} results:")
            printRecords(results)
            makeMenu(
                ContactsMenu.Search,
                makeRecordMenus(results),
                mapOf(
                    "back" to { ContactsMenu.Menu },
                    "again" to makeSearchMenu()
                )
            )()
        }
    }

    val mainMenu = makeMenu(
        ContactsMenu.Menu,
        emptyMap(),
        mapOf(
            "add" to this::add,
            "list" to makeListMenu(),
            "search" to makeSearchMenu(),
            "count" to this::count,
            "exit" to { ContactsMenu.Exit }
        )
    )

    private fun add(): ContactsMenu {
        println("Enter the type (person, organization): ")
        when (readln()) {
            "person" -> records.add(PersonContact.read())
            "organization" -> records.add(OrganizationContact.read())
            else -> return ContactsMenu.Menu
        }
        println("The record added.")
        return ContactsMenu.Menu
    }


    private fun count(): ContactsMenu {
        println("The Phone Book has ${records.size} records.")
        return ContactsMenu.Menu
    }

    private val moshi = Moshi.Builder()
        .add(
            PolymorphicJsonAdapterFactory.of(
                Contact::class.java, "type")
                .withSubtype(
                    PersonContact::class.java, ContactType.PersonType.name)
                .withSubtype(
                    OrganizationContact::class.java, ContactType.OrganizationType.name)
        )
        .add(KotlinJsonAdapterFactory())
        .build()

    private val type = Types.newParameterizedType(MutableList::class.java, Contact::class.java)
    private val adapter = moshi.adapter<MutableList<Contact>>(type)

    fun saveJson(filename: String) {
        if (filename == "") {
            return
        }
        val file = File(filename)
        file.writeText(adapter.toJson(records))
    }

    fun loadJson(filename: String) {
        if (filename == "") {
            return
        }
        val file = File(filename)
        if (!file.exists()) {
            return
        }

        records.addAll(adapter.fromJson(file.readText()) ?: emptyList())
        println("open $filename")
    }
}

enum class ContactType {
    PersonType,
    OrganizationType
}

fun main(args: Array<String>) {
    val contacts = Contacts()
    val filename = if (args.isNotEmpty()) args[0] else ""
    contacts.loadJson(filename)

    contacts.mainMenu()

    contacts.saveJson(filename)
}
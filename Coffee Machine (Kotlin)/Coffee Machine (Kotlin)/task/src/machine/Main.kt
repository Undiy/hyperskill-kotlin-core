package machine

enum class CoffeeMachineMenu {
    MAIN_MENU,
    BUYING_MENU,
    WATER_FILL,
    MILK_FILL,
    BEANS_FILL,
    CUPS_FILL
}

class CoffeeMachine(
    var water: Int,
    var milk: Int,
    var beans: Int,
    var cups: Int,
    var money: Int,
    private var menu: CoffeeMachineMenu = CoffeeMachineMenu.MAIN_MENU
) {
    init {
        showMainMenu()
    }
    fun action(input: String) : Boolean {
        return when (menu) {
            CoffeeMachineMenu.MAIN_MENU -> {
                println("")
                when (input) {
                    "buy" -> showBuyingMenu()
                    "fill" -> showWaterFill()
                    "take" -> {
                        takeMoney()
                        showMainMenu()
                    }
                    "remaining" -> {
                        printStatus()
                        showMainMenu()
                    }
                    "exit" -> true
                    else -> showMainMenu()
                }
            }
            CoffeeMachineMenu.BUYING_MENU -> {
                println("")
                val back = when (input) {
                    "1" -> {
                        buyCoffee(250, 0, 16, 4)
                        true
                    }
                    "2" -> {
                        buyCoffee(350, 75,20,7)
                        true
                    }
                    "3" -> {
                        buyCoffee(200,100,12,6)
                        true
                    }
                    "back" -> true
                    else -> false
                }
                if (back) {
                    showMainMenu()
                } else {
                    showBuyingMenu()
                }
            }
            CoffeeMachineMenu.WATER_FILL -> {
                water += input.toInt()
                showMilkFill()
            }
            CoffeeMachineMenu.MILK_FILL -> {
                milk += input.toInt()
                showBeansFill()
            }
            CoffeeMachineMenu.BEANS_FILL -> {
                beans += input.toInt()
                showCupsFill()
            }
            CoffeeMachineMenu.CUPS_FILL -> {
                println("")
                cups += input.toInt()
                showMainMenu()
            }
        }
    }

    fun showMainMenu(): Boolean {
        println("Write action (buy, fill, take, remaining, exit): ")
        menu = CoffeeMachineMenu.MAIN_MENU
        return false
    }

    fun showBuyingMenu(): Boolean {
        println("What do you want to buy? 1 - espresso, 2 - latte, 3 - cappuccino, back - to main menu:")
        menu = CoffeeMachineMenu.BUYING_MENU
        return false
    }

    fun showWaterFill(): Boolean {
        println("Write how many ml of water you want to add:")
        menu = CoffeeMachineMenu.WATER_FILL
        return false
    }

    fun showMilkFill(): Boolean {
        println("Write how many ml of milk you want to add:")
        menu = CoffeeMachineMenu.MILK_FILL
        return false
    }

    fun showBeansFill(): Boolean {
        println("Write how many grams of coffee beans you want to add:")
        menu = CoffeeMachineMenu.BEANS_FILL
        return false
    }

    fun showCupsFill(): Boolean {
        println("Write how many disposable cups you want to add:")
        menu = CoffeeMachineMenu.CUPS_FILL
        return false
    }

    fun printStatus() {
        println("""
        The coffee machine has:
        $water ml of water
        $milk ml of milk
        $beans g of coffee beans
        $cups disposable cups
        $$money of money""".trimIndent()
        )
        println("")
    }

    fun takeMoney() {
        println("I gave you $$money\n")
        money = 0
    }

    fun buyCoffee(_water: Int, _milk: Int, _beans: Int, _money: Int) {
        if (water < _water) {
            println("Sorry, not enough water!")
            return
        }
        if (milk < _milk) {
            println("Sorry, not enough milk!")
            return
        }
        if (beans < _beans) {
            println("Sorry, not enough coffee beans!")
            return
        }
        if (cups < 1) {
            println("Sorry, not enough cups!")
            return
        }
        water -= _water
        milk -= _milk
        beans -= _beans
        cups -= 1
        money += _money
    }
}



//fun fillResources(cm : CoffeeMachine) : CoffeeMachine {
//    println("Write how many ml of water you want to add:")
//    val water = readln().toInt()
//    println("Write how many ml of milk you want to add:")
//    val milk = readln().toInt()
//    println("Write how many grams of coffee beans you want to add:")
//    val beans = readln().toInt()
//    println("Write how many disposable cups you want to add:")
//    val cups = readln().toInt()
//
//    return CoffeeMachine(
//        cm.water + water,
//        cm.milk + milk,
//        cm.beans + beans,
//        cm.cups + cups,
//        cm.money
//    )
//}


fun main() {

    val cm = CoffeeMachine(400, 540, 120, 9, 550)

    var exit = false
    while (!cm.action(readln())) {

    }
}

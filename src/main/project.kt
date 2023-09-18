package battleship

import kotlin.concurrent.thread
import kotlin.math.abs

private val starterPack: List<Ship> by lazy {
    listOf(
        Ship(name = "Aircraft Carrier", cells = 5),
        Ship(name = "Battleship", cells = 4),
        Ship(name = "Submarine", cells = 3),
        Ship(name = "Cruiser", cells = 3),
        Ship(name = "Destroyer", cells = 2)
    )
}
private val starterPack2: List<Ship> by lazy {
    listOf(
        Ship(name = "Aircraft Carrier", cells = 5),
        Ship(name = "Battleship", cells = 4),
        Ship(name = "Submarine", cells = 3),
        Ship(name = "Cruiser", cells = 3),
        Ship(name = "Destroyer", cells = 2)
    )
}

enum class Turn { FIRST, SECOND }

data class Ship(
    val name: String,
    val cells: Int,
    var startPosition: Pair<Char, Int> = Pair('a', -1),
    var endPosition: Pair<Char, Int> = Pair('a', -1),
    var cords: MutableMap<String, Char> = mutableMapOf(),
    var isSunk: Boolean = false
)

data class Player(
    val publicBoard: MutableList<MutableList<Char>> = MutableList(10) { MutableList(10) { '~' } },
    val secretBoard: MutableList<MutableList<Char>> = MutableList(10) { MutableList(10) { '~' } },
    var navi: List<Ship> = emptyList(),
    val turn: Turn,
    val playerName: String
)

fun main() {


    val firstPlayer = Player(turn = Turn.FIRST, playerName = "Player 1").apply {
        println("$playerName, place your ships on the game field")
        secretBoard.printBoard()
        askForShipsPosition(this)
        passTurnToAnotherPlayer()
    }
    val secondPlayer = Player(turn = Turn.SECOND, playerName = "Player 2").apply {
        println("$playerName, place your ships on the game field")
        secretBoard.printBoard()
        askForShipsPosition(this)
        passTurnToAnotherPlayer()
    }
    startGame(firstPlayer, secondPlayer)
}

fun passTurnToAnotherPlayer() {
    println("Press Enter and pass the move to another player")
    readln().let { println("\\033[H\\033[2J"); System.out.flush() }
}

fun startGame(firstPlayer: Player, secondPlayer: Player) {

    gameIsActive@ while (true) {
        val firstPLayerThread = thread(start = true) {
            secondPlayer.publicBoard.printBoard()
            println("---------------------")
            firstPlayer.secretBoard.printBoard()
            println("${firstPlayer.playerName}, it's your turn:")
            shootingLoop@ while (true) {
                try {
                    val rawShot = readln()
                    val currentShot = rawShot.transformToPair()
                    secondPlayer.publicBoard.processShot(secondPlayer.secretBoard,currentShot)
                    secondPlayer.navi.apply {
                        this.find { it.cords.containsKey(currentShot.toKeyFormat()) }?.cords?.set(
                            currentShot.toKeyFormat(),
                            'X'
                        )
                    }
                    if (secondPlayer.navi.any { anyShip -> anyShip.cords.containsKey(currentShot.toKeyFormat()) }) {
                        if (secondPlayer.navi.any { anyShip -> anyShip.cords.all { currentCord -> currentCord.value == 'X' } && !anyShip.isSunk }) {
                            secondPlayer.navi.find { foundShip -> foundShip.cords.all { it.value == 'X' } && !foundShip.isSunk }?.isSunk =
                                true
                            if (secondPlayer.navi.all { it.isSunk }) {
                                println("You sank the last ship. You won. Congratulations!")
                                break@shootingLoop
                            } else {
                                println("You sank a ship! Specify a new target:")
                                break@shootingLoop
                            }

                        } else {
                            println("You hit a ship! Try again:")
                            break@shootingLoop
                        }
                    } else {
                        println("You missed. Try again:")
                        break@shootingLoop
                    }
                } catch (_: Exception) {
                    println("Error! You entered the wrong coordinates! Try again:")
                    continue@shootingLoop
                }
            }
        }.join()
        passTurnToAnotherPlayer()
        val secondPlayerThread = thread(start = true) {
            firstPlayer.publicBoard.printBoard()
            println("---------------------")
            secondPlayer.secretBoard.printBoard()
            println("${secondPlayer.playerName}, it's your turn:")
            shootingLoop@ while (true) {
                try {
                    val rawShot = readln()
                    val currentShot = rawShot.transformToPair()
                    firstPlayer.publicBoard.processShot(firstPlayer.secretBoard,currentShot)
                    firstPlayer.navi.apply {
                        this.find { it.cords.containsKey(currentShot.toKeyFormat()) }?.cords?.set(
                            currentShot.toKeyFormat(),
                            'X'
                        )
                    }
                    if (firstPlayer.navi.any { anyShip -> anyShip.cords.containsKey(currentShot.toKeyFormat()) }) {
                        if (firstPlayer.navi.any { anyShip -> anyShip.cords.all { currentCord -> currentCord.value == 'X' } && !anyShip.isSunk }) {
                            firstPlayer.navi.find { foundShip -> foundShip.cords.all { it.value == 'X' } && !foundShip.isSunk }?.isSunk =
                                true
                            if (firstPlayer.navi.all { it.isSunk }) {
                                println("You sank the last ship. You won. Congratulations!")
                                break@shootingLoop
                            } else {
                                println("You sank a ship! Specify a new target:")
                                break@shootingLoop
                            }

                        } else {
                            println("You hit a ship! Try again:")
                            break@shootingLoop
                        }
                    } else {
                        println("You missed. Try again:")
                        break@shootingLoop
                    }
                } catch (_: Exception) {
                    println("Error! You entered the wrong coordinates! Try again:")
                    continue@shootingLoop
                }
            }
        }.join()
        passTurnToAnotherPlayer()
    }
}

private fun MutableList<MutableList<Char>>.processShot(referenceBoard: MutableList<MutableList<Char>>, shot: Pair<Char, Int>) {
    val y = shot.first.code - 65
    val x = shot.second
    when (referenceBoard[y][x]) {
        'O' -> {
            this[y][x] = 'X'
            referenceBoard[y][x] = 'X'
            this.printBoard()
        }
        '~' -> {
            this[y][x] = 'M'
            referenceBoard[y][x] = 'M'
        }
        else -> { this.printBoard() }
    }
}

fun askForShipsPosition(player: Player) {

    if (player.turn == Turn.FIRST) player.apply {
        navi = starterPack.map { currentShip ->
            println("Enter the coordinates of the ${currentShip.name} (${currentShip.cells} cells):")
            currentShipConfig@ while (true) {
                val shipPlaceRow = readln().split(" ")
                val rawStartPosition = shipPlaceRow.first().transformToPair()
                val rawEndPosition = shipPlaceRow[1].transformToPair()
                val xStart = getLowerBetween(rawStartPosition.first.code, rawEndPosition.first.code).toChar()
                val xEnd = getHigherBetween(rawStartPosition.first.code, rawEndPosition.first.code).toChar()
                val yStart = getLowerBetween(rawStartPosition.second, rawEndPosition.second)
                val yEnd = getHigherBetween(rawStartPosition.second, rawEndPosition.second)
                currentShip.startPosition = Pair(xStart, yStart)
                currentShip.endPosition = Pair(xEnd, yEnd)

                if (isShipWellPlaced(currentShip, secretBoard)) {
                    updateShipCordsAndPosition(currentShip)
                    secretBoard.placeShip(currentShip)
                    break@currentShipConfig
                }
            }
            secretBoard.printBoard()
            currentShip
        }
    }

    if (player.turn == Turn.SECOND) player.apply {
        navi = starterPack2.map { currentShip ->
            println("Enter the coordinates of the ${currentShip.name} (${currentShip.cells} cells):")
            currentShipConfig@ while (true) {
                val shipPlaceRow = readln().split(" ")
                val rawStartPosition = shipPlaceRow.first().transformToPair()
                val rawEndPosition = shipPlaceRow[1].transformToPair()
                val xStart = getLowerBetween(rawStartPosition.first.code, rawEndPosition.first.code).toChar()
                val xEnd = getHigherBetween(rawStartPosition.first.code, rawEndPosition.first.code).toChar()
                val yStart = getLowerBetween(rawStartPosition.second, rawEndPosition.second)
                val yEnd = getHigherBetween(rawStartPosition.second, rawEndPosition.second)
                currentShip.startPosition = Pair(xStart, yStart)
                currentShip.endPosition = Pair(xEnd, yEnd)

                if (isShipWellPlaced(currentShip, secretBoard)) {
                    updateShipCordsAndPosition(currentShip)
                    secretBoard.placeShip(currentShip)
                    break@currentShipConfig
                }
            }
            secretBoard.printBoard()
            currentShip
        }
    }

}

fun updateShipCordsAndPosition(ship: Ship) {
    val resultMap = mutableMapOf<String, Char>()
    for (currentCharCode in ship.startPosition.first.code..ship.endPosition.first.code) {
        for (currentNumber in ship.startPosition.second..ship.endPosition.second) {
            val currentKey = "${currentCharCode.toChar()}${currentNumber}"
            resultMap[currentKey] = '0'
        }
    }
    ship.cords = resultMap
}

fun getLowerBetween(firstNumber: Int, secondNumber: Int): Int =
    if (firstNumber >= secondNumber) secondNumber else firstNumber

fun getHigherBetween(firstNumber: Int, secondNumber: Int): Int =
    if (firstNumber <= secondNumber) secondNumber else firstNumber

fun isShipWellPlaced(ship: Ship, secretBoard: MutableList<MutableList<Char>>): Boolean {

    if (!(ship.startPosition.first == ship.endPosition.first || ship.startPosition.second == ship.endPosition.second)) {
        println("Error! Wrong ship location! Try again:")
        return false
    }

    if (ship.startPosition.first == ship.endPosition.first) {
        if (abs(ship.startPosition.second - ship.endPosition.second) + 1 != ship.cells) {
            println("Error! Wrong length of the ${ship.name}! Try again:")
            return false
        }
    }
    if (ship.startPosition.second == ship.endPosition.second) {
        if (abs(ship.startPosition.first.code - ship.endPosition.first.code) + 1 != ship.cells) {
            println("Error! Wrong length of the ${ship.name}! Try again:")
            return false
        }
    }
    //validar si se solapan las naves
    //validar si están muy cerca

    val (xStart, xEnd) = orderCoordinates(ship.startPosition.second, ship.endPosition.second)
    val (yStart, yEnd) = orderCoordinates(ship.startPosition.first, ship.endPosition.first)
    for (x in xStart..xEnd) {
        for (y in yStart..yEnd) {
            if (secretBoard[y][x] == 'O') return false
            try {
                if (secretBoard[y - 1][x - 1] == 'O') return printCloseShipErrorAndReturnFalse()
            } catch (_: Exception) {
            }
            try {
                if (secretBoard[y - 1][x] == 'O') return printCloseShipErrorAndReturnFalse()
            } catch (_: Exception) {
            }
            try {
                if (secretBoard[y - 1][x + 1] == 'O') return printCloseShipErrorAndReturnFalse()
            } catch (_: Exception) {
            }
            try {
                if (secretBoard[y][x - 1] == 'O') return printCloseShipErrorAndReturnFalse()
            } catch (_: Exception) {
            }
            try {
                if (secretBoard[y][x] == 'O') return printCloseShipErrorAndReturnFalse()
            } catch (_: Exception) {
            }
            try {
                if (secretBoard[y][x + 1] == 'O') return printCloseShipErrorAndReturnFalse()
            } catch (_: Exception) {
            }
            try {
                if (secretBoard[y + 1][x - 1] == 'O') return printCloseShipErrorAndReturnFalse()
            } catch (_: Exception) {
            }
            try {
                if (secretBoard[y + 1][x] == 'O') return printCloseShipErrorAndReturnFalse()
            } catch (_: Exception) {
            }
            try {
                if (secretBoard[y + 1][x + 1] == 'O') return printCloseShipErrorAndReturnFalse()
            } catch (_: Exception) {
            }
        }
    }

    return true
}

private fun printCloseShipErrorAndReturnFalse(): Boolean {
    println("Error! You placed it too close to another one. Try again:")
    return false
}

private fun MutableList<MutableList<Char>>.printBoard() {
    println("  1 2 3 4 5 6 7 8 9 10")
    for (rowIndex in this.indices) {
        print("${(rowIndex + 65).toChar()}")
        for (column in this[rowIndex]) print(" $column")
        println()
    }
}

private fun String.transformToPair(): Pair<Char, Int> = Pair(this.first(), this.substring(1).toInt() - 1)

private fun MutableList<MutableList<Char>>.placeShip(ship: Ship) {
    val (xStart, xEnd) = orderCoordinates(ship.startPosition.second, ship.endPosition.second)
    val (yStart, yEnd) = orderCoordinates(ship.startPosition.first, ship.endPosition.first)
    for (x in xStart..xEnd) {
        for (y in yStart..yEnd) {
            this[y][x] = 'O'
        }
    }
}

fun orderCoordinates(startPosition: Int, endPosition: Int): Pair<Int, Int> =
    if (startPosition > endPosition) Pair(endPosition, startPosition) else Pair(startPosition, endPosition)

fun orderCoordinates(startPosition: Char, endPosition: Char): Pair<Int, Int> = if (startPosition > endPosition) Pair(
    endPosition.code - 65,
    startPosition.code - 65
) else Pair(startPosition.code - 65, endPosition.code - 65)

private fun Pair<Char, Int>.toKeyFormat(): String = "${this.first}${this.second}"
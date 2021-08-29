package minesweeper

import kotlin.random.Random

class Minesweeper(private val dimension: Int) {
    private var lines = Array(dimension) { Array(dimension) { 0 } }
    private var view = Array(dimension) { Array(dimension) { 0 } } // 0 = hidden, 1 = show, 2 = marked
    private var mines = 0
    private var fakeMines = 0
    private var freeLeft = dimension * dimension // number of fields not yet shown to user ( mines will get subtracted from it )
    private var setMines = false
    private var first = true // for first run through of the surrounding() function
    private var lost = false

    /**
     * Play the game
     */
    fun playGame() {
        initialize()
        while ((fakeMines > 0 || mines > 0) && !lost && freeLeft != 0) {
            printField()
            fieldAction()
        }
        printField()
        println(if (lost) "You stepped on a mine and failed!" else "Congratulations! You found all the mines!")
    }

    /**
     * Initialize the field
     */
    private fun initialize() {
        mines = getNum("How many mines do you want on the field? ", false)
        freeLeft -= mines

        while (!setMines) {
            printField()
            fieldAction()
        }
        for (n in lines.indices) { // in case user marked mines before freeing a field
            for (m in lines.indices) {
                if (view[n][m] == 2 && lines[n][m] == dimension) {
                    fakeMines -= 1
                    mines -= 1
                }
            }
        }
    }

    /**
     * Print in field
     */
    private fun printField() {
        val strLine = "—│—————————│"
        println("\n │123456789│")
        println(strLine)
        for (row in lines.indices) {
            print("${row + 1}│")
            for (col in lines[row].indices) {
                print(
                    when {
                        lines[row][col] == dimension && lost -> "X"
                        view[row][col] == 2 && !lost -> "*"
                        view[row][col] == 1 -> {
                            if (lines[row][col] == 0) "/" else lines[row][col]
                        }
                        else -> "."
                    }
                )
            }
            println("│")
        }
        println(strLine)
    }

    /**
     * Do the user specified action
     */
    private fun fieldAction() {
        var marked = false
        while (!marked) {
            print("Set/unset mine marks or claim a cell as free: ")
            val inputArray = readLine()!!.split(" ")
            val str1 = inputArray[0]
            val str2 = inputArray[1]
            val action = inputArray[2]
            var y = if (isNumber(str1)) str1.toInt() else getNum(str1)
            var x = if (isNumber(str2)) str2.toInt() else getNum(str2)
            x = getNumberInRange(x, 1..dimension)
            y = getNumberInRange(y, 1..dimension)
            x -= 1
            y -= 1

            when (action) {
                "free" -> marked = free(x, y)
                "mine" -> marked = markMine(x, y)
            }
        }
    }

    /**
     * Explore and mark the cell as free
     *
     * @param row Row position
     * @param col Column position
     */
    private fun free(row: Int, col: Int): Boolean {
        if (!setMines) { // sets up the view that placeMines() needs to work
            if (view[row][col] == 2) fakeMines -= 1
            view[row][col] = 1
            neighborCheck(row, col)
            first = false
            placeMines()
            for (x in view.indices) { // resets the view, so that open fields can be shown
                for (y in view[x].indices) {
                    if (view[x][y] == 1) view[x][y] = 0
                }
            }
            view[row][col] = 1
            setMines = true
            freeLeft -= 1
            neighborCheck(row, col)
            return true
        } else { // frees a field if it is not a mine or already free
            if (lines[row][col] == dimension) {
                lost = true
                return true
            } else {
                when (view[row][col]) {
                    0 -> {
                        view[row][col] = 1
                        freeLeft -= 1
                        if (lines[row][col] == 0) neighborCheck(row, col)
                        return true
                    }
                    1 -> {
                        println("Field is already free")
                        return false
                    }
                    2 -> {
                        view[row][col] = 1
                        freeLeft -= 1
                        if (lines[row][col] == 0) neighborCheck(row, col)
                        fakeMines -= 1
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Mark mine
     *
     * @param row Row position
     * @param col Column position
     */
    private fun markMine(row: Int, col: Int): Boolean {
        if (lines[row][col] == dimension) {
            return if (view[row][col] == 0) {
                mines -= 1
                view[row][col] = 2
                true
            } else {
                mines += 1
                view[row][col] = 0
                true
            }
        } else {
            return when (view[row][col]) {
                0 -> {
                    view[row][col] = 2
                    fakeMines += 1
                    true
                }
                2 -> {
                    view[row][col] = 0
                    fakeMines -= 1
                    true
                }
                else -> {
                    println("open field cannot be marked")
                    false
                }
            }
        }
    }

    /**
     * Place mines
     */
    private fun placeMines() {
        repeat(mines) {
            var changed = false
            while (!changed) {
                val row = (0 until dimension).random()
                val col = (0 until dimension).random()
                if (lines[row][col] != dimension && view[row][col] != 1) {
                    lines[row][col] = dimension
                    changed = true
                    neighborCheck(row, col)
                }
            }
        }
    }

    /**
     * Check fields around a given field and then calls neighborWork to do the work with each one
     *
     * @param row Row position
     * @param col Column position
     */
    private fun neighborCheck(row: Int, col: Int) {
        if (col != 0 && lines[row][col - 1] != dimension) neighborWork(row, col - 1)
        if (col != 8 && lines[row][col + 1] != dimension) neighborWork(row, col + 1)
        if (row != 0) {
            if (lines[row - 1][col] != dimension) neighborWork(row - 1, col)
            if (col != 0 && lines[row - 1][col - 1] != dimension) neighborWork(row - 1, col - 1)
            if (col != 8 && lines[row - 1][col + 1] != dimension) neighborWork(row - 1, col + 1)
        }
        if (row != 8) {
            if (lines[row + 1][col] != dimension) neighborWork(row + 1, col)
            if (col != 0 && lines[row + 1][col - 1] != dimension) neighborWork(row + 1, col - 1)
            if (col != 8 && lines[row + 1][col + 1] != dimension) neighborWork(row + 1, col + 1)
        }
    }

    /**
     * Has 3 different things it does, based on if it's the first run through and if all the mines have been set yet.
     * After first two cases have been satisfied it then is used to clear fields around an empty field.
     *
     * @param row Row position
     * @param col Column position
     */
    private fun neighborWork(row: Int, col: Int) {
        if (!setMines && !first) lines[row][col] += 1 else {
            if (view[row][col] != 1) {
                if (view[row][col] == 2) fakeMines -= 1
                view[row][col] = 1
                if (!first) freeLeft -= 1
                if (lines[row][col] == 0 && !first) neighborCheck(row, col)
            }
        }
    }

    /**
     * Get number from user when current input is invalid
     *
     * @param text User input
     * @param defaultMessage Default error message
     * @return Integer number
     */
    private fun getNum(text: String, defaultMessage: Boolean = true): Int {
        val strErrorNum = " was not a number, please try again: "
        var num = text

        while (!isNumber(num)) {
            print(if (defaultMessage) num + strErrorNum else num)
            num = readLine()!!
        }

        return num.toInt()
    }


    /**
     * Get a number in the required range
     *
     * @param num Number to be checked
     * @param range Range to be checked against
     * @return Number in range
     */
    private fun getNumberInRange(num: Int, range: IntRange): Int {
        var inputNum = num
        while (notInRange(inputNum, range)) {
            inputNum = getNum("$inputNum was out of range. Please enter a number in the range ${range.first} " +
                    "and ${range.last}: ", false)
        }
        return inputNum
    }

    /**
     * Check if given number is in given range
     *
     * @param num Number to be checked
     * @param range Range to be checked against
     * @return Boolean if range contains num
     */
    private fun notInRange(num: Int, range: IntRange) = (!range.contains(num))

    /**
     * Check if given string is a number or not
     *
     * @param num String
     * @return Boolean if num is Int
     */
    private fun isNumber(num: String) = num.toIntOrNull() != null

    /**
     * Get random integer in the specified range
     */
    private fun IntRange.random() = Random.nextInt(endInclusive + 1 - start) + start
}

fun main() {
    val dimension = 9
    val game = Minesweeper(dimension)
    game.playGame()
}
import java.util.LinkedList

data class MinesweeperBoard(val list: List<String>) {

    private val MINE_SYMBOL  = '*'
    private val MINE_VALUE   = 9
    private val BLANK_VALUE  = 0

    private val rows : Int = if (list.isNotEmpty()) list.size else -1
    private val cols : Int = if (list.isNotEmpty()) list.first().length else -1

    private val LEFT_CORNER  = 0
    private val RIGHT_CORNER = cols - 1
    private val UPPER_CORNER = 0
    private val LOWER_CORNER = rows - 1


    // If the input is empty, then return an empty list
    // If the input does not contain anything, then return a list of nothing
    // Otherwise, perform the algorithm below
    fun withNumbers(): List<String> = if (list.isEmpty()) emptyList() else if (list.equals("")) listOf("") else {

        // Mines Positions
        val minesPosition : LinkedList<Pair<Int, Int>> = LinkedList()

        // Board
        val board: LinkedList<Int> = LinkedList()

        // For each given line, loop over the sequence and convert its symbol with a digit and then register
        // the mine position within the board
        list.withIndex().forEach { (row: Int, line: String) -> line.withIndex().forEach { (col: Int, char: Char) ->
                if (char == MINE_SYMBOL) {
                    // Also, register the position for a given mine
                    minesPosition.addLast(Pair(row, col))
                    board.addLast(MINE_VALUE)
                }else {
                    board.addLast(BLANK_VALUE)
                }
        } }

        // Algorithm: for each given mine, select all the neighbours.
        for((row, col) in minesPosition) {
            val neighbours: List<Pair<Int, Int>> = listOf(
                Pair(row - 1, col - 1), Pair(row - 1, col),
                Pair(row - 1, col + 1), Pair(row, col - 1),
                Pair(row, col + 1),     Pair(row + 1, col - 1),
                Pair(row + 1, col),     Pair(row + 1, col + 1))
            // Loop over all the neighbours, but pay attention to the board limits and
            // to other mines
            for (neighbour in neighbours) {
                if( (neighbour.second in LEFT_CORNER..RIGHT_CORNER) &&
                    (neighbour.first in UPPER_CORNER..LOWER_CORNER)
                            && !minesPosition.contains(neighbour) ) {
                    // Update the current value
                    val x : Int = neighbour.first
                    val y : Int = neighbour.second
                    board[(x * cols) + y] += 1
                }
            }
        }

        // For each given sequence, convert the integer list into a string
        val result : MutableList<String> = mutableListOf()
        for(row in 0 until rows) {
            result.add(board.subList(row * cols, (row * cols) + cols).joinToString("").replace('9', '*').replace('0', ' '))
        }
        // Return the result
        result
    }
}


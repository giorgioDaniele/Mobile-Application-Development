import java.util.LinkedList


class Forth {

    private var MIN_NUMBER_FOR_SWAP  = 2
    private var MIN_NUMBER_FOR_OVER  = 2

    private var ADD  : String = "+"
    private var SUB  : String = "-"
    private var MUL  : String = "*"
    private var DIV  : String = "/"
    private var DROP : String = "drop"
    private var DUP  : String = "dup"
    private var SWAP : String = "swap"
    private var OVER : String = "over"

    // It stores all the ovverided instructions and the new ones, as well
    var userAliasInstructions   : MutableMap<String, LinkedList<String>> = mutableMapOf()
    // It stores all the new variable
    var userAliasVariables     : MutableMap<String, String> = mutableMapOf()
    // It stores all the new typdef operations
    var userAliasOperations    : MutableMap<String, String> = mutableMapOf()


    val isANumbersSequence : (List<String>) -> Boolean =  { stack : List<String> ->  stack.all { symbol : String -> symbol.lowercase() !in arrayOf(ADD, SUB, MUL, DIV, DROP, DUP, SWAP, OVER) && symbol.lowercase() !in userAliasInstructions && symbol !in userAliasVariables && symbol !in userAliasOperations}  }
    val isAnEmptySequence  : (List<String>) -> Boolean =  { stack : List<String> ->  stack.isEmpty() }

    val popFirstOperand  : (LinkedList<String>) -> Int = { stack : LinkedList<String> ->  try { stack.pop().toInt() } catch (exception: java.util.NoSuchElementException) { throw java.util.NoSuchElementException("empty stack") }}
    val popSecondOperand : (LinkedList<String>) -> Int = { stack : LinkedList<String> ->  try { stack.pop().toInt() } catch (exception: java.util.NoSuchElementException) { throw java.util.NoSuchElementException("only one value on the stack") }}

    // Math operations
    val doADD  : (Int, Int, LinkedList<String>) -> Unit = { n1: Int, n2: Int, stack : LinkedList<String> -> stack.push((n1 + n2).toString()) }
    val doSUB  : (Int, Int, LinkedList<String>) -> Unit = { n1: Int, n2: Int, stack : LinkedList<String> -> stack.push((n1 - n2).toString()) }
    val doMUL  : (Int, Int, LinkedList<String>) -> Unit = { n1: Int, n2: Int, stack : LinkedList<String> -> stack.push((n1 * n2).toString()) }
    val doDIV  : (Int, Int, LinkedList<String>) -> Unit = { n1: Int, n2: Int, stack : LinkedList<String> -> if(n2 == 0) throw ArithmeticException("divide by zero") else stack.push((n1 / n2).toString()) }

    // Stack manipulation instructions
    val doDROP : (LinkedList<String>) -> Unit = { stack : LinkedList<String> ->  try { stack.pop().toInt() } catch (exception: java.util.NoSuchElementException) { throw java.util.NoSuchElementException("empty stack") }}

    val doDUP : (LinkedList<String>) -> Unit  = {  stack : LinkedList<String> ->
        if(stack.isEmpty()) throw java.util.NoSuchElementException("empty stack") else { /* Fetch the last value */ val value = stack.pop(); stack.push(value); stack.push(value) }}

    val doSWAP : (LinkedList<String>) -> Unit = {  stack : LinkedList<String> ->
        if(stack.isEmpty()) throw java.util.NoSuchElementException("empty stack") else {
        if(stack.size < MIN_NUMBER_FOR_SWAP) throw java.util.NoSuchElementException("only one value on the stack") else {val firstOperand  = stack.pop(); val secondOperand = stack.pop(); stack.push(firstOperand); stack.push(secondOperand) }}
    }
    val doOVER : (LinkedList<String>) -> Unit = {  stack : LinkedList<String> ->
        if(stack.isEmpty()) throw java.util.NoSuchElementException("empty stack") else {
            if(stack.size < MIN_NUMBER_FOR_OVER) throw java.util.NoSuchElementException("only one value on the stack") else { val firstOperand = stack.pop(); val secondOperand = stack.pop(); stack.push(secondOperand); stack.push(firstOperand); stack.push((secondOperand)) }}}

    private fun operatorDispatcher (stack: LinkedList<String>, symbol: String) : Unit {

        var symbolToProcess : String = symbol
        // If there is an override, use it
        if(symbol.lowercase() in userAliasInstructions) symbolToProcess = userAliasInstructions[symbol.lowercase()].toString()
        // ...otherwise, use the default behaviour

        when(symbolToProcess.lowercase()) {

            ADD -> { val firstOperand  = popFirstOperand(stack); val secondOperand = popSecondOperand(stack); doADD(secondOperand, firstOperand, stack) }
            SUB -> { val firstOperand  = popFirstOperand(stack); val secondOperand = popSecondOperand(stack); doSUB(secondOperand, firstOperand, stack) }
            MUL -> { val firstOperand  = popFirstOperand(stack); val secondOperand = popSecondOperand(stack); doMUL(secondOperand, firstOperand, stack) }
            DIV -> { val firstOperand  = popFirstOperand(stack); val secondOperand = popSecondOperand(stack); doDIV(secondOperand, firstOperand, stack) }
            DUP ->  doDUP(stack)
            DROP -> doDROP(stack)
            SWAP -> doSWAP(stack)
            OVER -> doOVER(stack)
            // ...user instruction
            else -> {
                // ...fetch the correspondent commands
                val commands = userAliasInstructions[symbol.lowercase()] ?: emptyList()
                val input = LinkedList<String>(); input.addAll(commands)
                // ...if they are already known, then recursive on this function
                if(commands.all { syntax: String -> syntax in arrayOf(ADD, SUB, MUL, DIV, DROP, DUP, SWAP, OVER) })
                    commands.forEach { symbol: String -> operatorDispatcher(stack, symbol) }
                else {
                    // Write here the new instructions and their own behavior
                    if(symbol == "countup") { val values = userAliasInstructions[symbol.lowercase()] ?: emptyList(); for(value in commands) stack.push(value)}
                }
            }
        }
    }

    private fun evaluteCodeLine(code: List<String>) : List<Int> =

        // If the code sequence is made of numbers only, return the list by itself
        if (isANumbersSequence(code)) code.map { symbol : String -> symbol.toInt() }
        // If the code sequence is empty, return an empty list
        else if (isAnEmptySequence(code)) emptyList()
        // Otherwise, process the code sequence
        else {

        var symbolSequence : LinkedList<String> = LinkedList()
        for (symbol in code)
            // If the symbol is a number or a variable
            if(symbol.lowercase() !in arrayOf(ADD, SUB, MUL, DIV, DUP, DROP, SWAP, OVER) && symbol.lowercase() !in userAliasInstructions) {
                var symbolToStack : String = symbol
                if(symbol in userAliasVariables) symbolToStack= userAliasVariables[symbol].toString()
                symbolSequence.push(symbolToStack)
            }
            // If the symbol is an instruction
            else operatorDispatcher(symbolSequence, symbol)
        // Return the resulting stack
        symbolSequence.map { symbol : String -> symbol.toInt() }.reversed()
    }

    fun evaluate(vararg line: String): List<Int> {

        val resultList : LinkedList<Int> = LinkedList()

        // Each line is an input to process
        for(instruction in line) {

            // Check if there is just one string within the current instruction. If yes, and that string is a stack instruction, throw an exception
            if(instruction.split(" ").count() == 1 &&
                instruction.split(" ").first() in arrayOf(ADD, SUB, MUL, DIV, DUP, DROP, SWAP, OVER))
                    throw IllegalArgumentException("empty stack")

            // Check if there is just one string within the current instruction. If yes, and that string is just a random name, throw an exception
            if(instruction.split(" ").count() == 1 &&
                instruction.split(" ").first() !in arrayOf(ADD, SUB, MUL, DIV, DUP, DROP, SWAP, OVER) &&
                instruction.split(" ").first() !in userAliasOperations)
                throw IllegalArgumentException("undefined operation")

            // In the case of an instruction to define a new one
            if(instruction.contains(":") && instruction.split(" ")
                    .filter { symbol: String -> !(symbol.equals(":") || symbol.equals(";")) }
                    .all    { symbol: String -> symbol.matches(Regex("[0-9]+")) }) throw IllegalArgumentException("illegal operation")


            //Check if it is an typedef instruction or not
            if(instruction.contains(":")) {

                // This is a typedef instruction
                val command : Pair<List<String>, List<String>> = instruction.split(" ")
                    .filter { syntax: String -> !(syntax.equals(":") || syntax.equals(";")) }
                    .map    { syntax: String -> syntax.lowercase() }
                    .partition { syntax: String -> syntax.lowercase() !in arrayOf(ADD, SUB, MUL, DIV, DROP, DUP, SWAP, OVER) }

                    val leftChunk  = command.first    // It contains all the symbol which are not original
                    val rightChunk = command.second   // It contains all the symbol which are original

                    // If leftChunk is empty, the user is modding an original operation (he is just aliasing an existing operation)
                    if(leftChunk.isEmpty()) {
                        val typedefCommands: LinkedList<String> = LinkedList()
                        typedefCommands.addAll(rightChunk.subList(1,rightChunk.size))
                        userAliasInstructions[rightChunk.first()] = typedefCommands
                    }
                    // If rightChuck is empty and leftChunk contains two elements (foo 5, bar foo, ...), the user is defining a variable
                    else if(rightChunk.isEmpty() && leftChunk.size == 2) {
                        val variableName  : String = leftChunk[0]
                        val variableValue : String = leftChunk[1]
                        // If variableValue is digit, the user is creating a variable
                        if(variableValue.contains(Regex("[0-9]+"))) { userAliasVariables.put(variableName, variableValue) }
                        // ...otherwise, the user is changing the name
                        else if(variableValue in userAliasVariables) userAliasVariables.put(variableName, userAliasVariables.get(variableValue).toString())
                    }
                    // If rightChunk is empty, and the first element within the leftChunk contains some chars, then the user
                    // is defining a new instruction followed by its behavior
                    else if(rightChunk.isEmpty() && leftChunk.first().matches(Regex("[A-z]+"))) {
                            val typedefCommands: LinkedList<String> = LinkedList()
                            typedefCommands.addAll(leftChunk.subList(1, leftChunk.size))
                            userAliasOperations[leftChunk.first()] = typedefCommands.joinToString(" ")
                    }
                    // If rightChunk is empty, but leftChunk is larger than 1, the user is defining an operation
                    else if(rightChunk.isNotEmpty() && leftChunk.size > 2) {
                        val variableName  : String = leftChunk[0]
                        userAliasOperations.put(variableName, (leftChunk.subList(1, leftChunk.size) + rightChunk).joinToString(" "))
                    }
                    // ...otherwise, the user is defining a new name for an original instruction or a chain of them
                    else {
                        val typedefCommands: LinkedList<String> = LinkedList()
                        typedefCommands.addAll(rightChunk)
                        userAliasInstructions[leftChunk.first()] = typedefCommands
                    }
            }else {
                // Check if the user wants to execute a custom operation
                if(instruction in userAliasOperations) {
                    val result = evaluteCodeLine(userAliasOperations[instruction].toString().split(" "))
                    resultList.addAll(result)
                }
                else {
                    // ...otherwise, follow a traditional flow
                    val result = evaluteCodeLine(instruction.split(" "))
                    resultList.addAll(result)
                }
            }
        }
        return resultList
    }
}

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

    private val allNumber = Regex("[0-9]+")
    private val allChars  = Regex("[A-z]+")

    // It stores all the override instructions and the new ones, as well
    private val renamedInstructions   : MutableMap<String, LinkedList<String>> = mutableMapOf()
    // It stores all the new variable
    private val userVariables      : MutableMap<String, String> = mutableMapOf()
    // It stores all the new typedef operations
    private val userOperations     : MutableMap<String, String> = mutableMapOf()

    // If a given instruction contains just symbol and it belongs to Forth instructions set, but without parameters, it is an invalid instruction
    val checkForInvalidInstruction : (String) -> Boolean = { instruction: String -> (!instruction.trim().contains(" ") && instruction.lowercase() in arrayOf(ADD, SUB, MUL, DIV, DUP, DROP, SWAP, OVER)) }
    // If a given instruction contains just a symbol and it does not belong neither to Forth instructions set nor to alias instructions set, it is just a random word
    val checkForInvalidSyntax      : (String) -> Boolean = { instruction: String -> (!instruction.trim().contains(" ") && instruction.lowercase() !in arrayOf(ADD, SUB, MUL, DIV, DUP, DROP, SWAP, OVER) && instruction.lowercase() !in userOperations) }
    // If the user is writing a new definition, but it contains only numbers, it is an invalid action
    val checkForInvalidDefinition  : (String) -> Boolean = { instruction: String -> instruction.trim().contains(":") && instruction.trim().split(" ").filter { symbol: String -> !(symbol == ":" || symbol == ";") }.all { symbol: String -> symbol.matches(allNumber) }}

    // The parser works in this way: all the well-known symbols are collected into the second element in the Pair, while the rest is collected into the first element in the Pair
    val instructionParser : (String) -> Pair<List<String>, List<String>> = { instruction: String -> instruction.split(" ").filter { syntax: String -> !(syntax == ":" || syntax == ";") }.map { syntax: String -> syntax.lowercase() }.partition { syntax: String -> syntax.lowercase() !in arrayOf(ADD, SUB, MUL, DIV, DROP, DUP, SWAP, OVER) }}


    val checkForSimpleNumberList : (List<String>) -> Boolean =  { stack : List<String> ->  stack.all { symbol : String -> symbol.lowercase().matches(allNumber)} }
    val checkForEmptyList        : (List<String>) -> Boolean =  { stack : List<String> ->  stack.isEmpty() }

    val isThisSymbolDigit        : (String) -> Boolean = { symbol: String -> symbol.lowercase() !in arrayOf(ADD, SUB, MUL, DIV, DUP, DROP, SWAP, OVER) && symbol.lowercase() !in renamedInstructions }

    val popFirstOperand  : (LinkedList<String>) -> Int = { stack : LinkedList<String> ->  try { stack.pop().toInt() } catch (exception: java.util.NoSuchElementException) { throw java.util.NoSuchElementException("empty stack") }}
    val popSecondOperand : (LinkedList<String>) -> Int = { stack : LinkedList<String> ->  try { stack.pop().toInt() } catch (exception: java.util.NoSuchElementException) { throw java.util.NoSuchElementException("only one value on the stack") }}


    /*****************************************************************************************************************************************/
    /*****************************************************************************************************************************************/
    /* Forth operation definition */
    /*****************************************************************************************************************************************/
    /*****************************************************************************************************************************************/
    // Math instructions
    val doADD  : (Int, Int, LinkedList<String>) -> Unit = { n1: Int, n2: Int, stack : LinkedList<String> -> stack.push((n1 + n2).toString()) }
    val doSUB  : (Int, Int, LinkedList<String>) -> Unit = { n1: Int, n2: Int, stack : LinkedList<String> -> stack.push((n1 - n2).toString()) }
    val doMUL  : (Int, Int, LinkedList<String>) -> Unit = { n1: Int, n2: Int, stack : LinkedList<String> -> stack.push((n1 * n2).toString()) }
    val doDIV  : (Int, Int, LinkedList<String>) -> Unit = { n1: Int, n2: Int, stack : LinkedList<String> -> if(n2 == 0) throw ArithmeticException("divide by zero") else stack.push((n1 / n2).toString()) }
    // Stack manipulation instructions
    val doDROP : (LinkedList<String>) -> Unit = { stack : LinkedList<String> ->  try { stack.pop().toInt() } catch (exception: java.util.NoSuchElementException) { throw java.util.NoSuchElementException("empty stack") }}
    val doDUP : (LinkedList<String>) -> Unit  = {  stack : LinkedList<String> ->  if(stack.isEmpty()) throw java.util.NoSuchElementException("empty stack") else { /* Fetch the last value */ val value = stack.pop(); stack.push(value); stack.push(value) }}
    val doSWAP : (LinkedList<String>) -> Unit = {  stack : LinkedList<String> ->
        if(stack.isEmpty()) throw java.util.NoSuchElementException("empty stack") else {
        if(stack.size < MIN_NUMBER_FOR_SWAP) throw java.util.NoSuchElementException("only one value on the stack") else {val firstOperand  = stack.pop(); val secondOperand = stack.pop(); stack.push(firstOperand); stack.push(secondOperand) }}
    }
    val doOVER : (LinkedList<String>) -> Unit = {  stack : LinkedList<String> ->
        if(stack.isEmpty()) throw java.util.NoSuchElementException("empty stack") else {
            if(stack.size < MIN_NUMBER_FOR_OVER) throw java.util.NoSuchElementException("only one value on the stack") else { val firstOperand = stack.pop(); val secondOperand = stack.pop(); stack.push(secondOperand); stack.push(firstOperand); stack.push((secondOperand)) }}}
    /*****************************************************************************************************************************************/
    /*****************************************************************************************************************************************/

    private fun operatorDispatcher (stack: LinkedList<String>, symbol: String) : Unit {

        var symbolToProcess : String = symbol
        // If there is an override, use it
        if(symbol.lowercase() in renamedInstructions) symbolToProcess = renamedInstructions[symbol.lowercase()].toString()
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
            else -> {
                // If it reaches this point, it means the user is performing a renamed instruction
                val commands = renamedInstructions[symbol.lowercase()] ?: emptyList()
                val input = LinkedList<String>(); input.addAll(commands)
                // ...if they are already known, then recursive on this function
                if(commands.all { syntax: String -> syntax in arrayOf(ADD, SUB, MUL, DIV, DROP, DUP, SWAP, OVER) })
                    commands.forEach { symbol: String -> operatorDispatcher(stack, symbol) }
                else {
                    // Write here the new instructions and their own behavior
                    if(symbol == "countup") { val values = renamedInstructions[symbol.lowercase()] ?: emptyList(); for(value in commands) stack.push(value)}
                }
            }
        }
    }

    private fun evaluteCodeLine(code: List<String>) : List<Int> =  if (checkForSimpleNumberList(code)) code.map { symbol : String -> symbol.toInt() }  else if (checkForEmptyList(code)) emptyList()  else {

        var symbolSequence : LinkedList<String> = LinkedList()

        for (symbol in code)
            // If the symbol is a number or a variable
            if(isThisSymbolDigit(symbol)) {
                var symbolToStack : String = symbol
                if(symbol in userVariables) symbolToStack = userVariables[symbol].toString()
                symbolSequence.push(symbolToStack)
            }
            // If the symbol is an instruction
            else operatorDispatcher(symbolSequence, symbol)
        // Return the resulting stack
        symbolSequence.map { symbol : String -> symbol.toInt() }.reversed()
    }

    fun evaluate(vararg line: String): List<Int> {

        val resultList : LinkedList<Int> = LinkedList()

        for(instruction in line) {
            // Check for the input
            if(checkForInvalidInstruction(instruction)) throw IllegalArgumentException("empty stack")
            if(checkForInvalidSyntax(instruction))      throw IllegalArgumentException("undefined operation")
            if(checkForInvalidDefinition(instruction))  throw IllegalArgumentException("illegal operation")
            if(instruction.contains(":")) {

                val command : Pair<List<String>, List<String>> = instructionParser(instruction)

                    val unknownSymbols   = command.first
                    val knownSymbols     = command.second

                    // If there are no known symbols, it means all symbols are arleardy known. In practise, the user is defining
                    // something like "+ *", "dup over", "over dup", and so on
                    if(unknownSymbols.isEmpty()) {
                        val typedefCommands: LinkedList<String> = LinkedList()
                        typedefCommands.addAll(knownSymbols.subList(1,knownSymbols.size))
                        renamedInstructions[knownSymbols.first()] = typedefCommands
                    }

                    // If there are no known symbols and there are just two unknown symbol, it means the user is creating a new
                    // variable or overriding an existing variable with a new name
                    else if(knownSymbols.isEmpty() && unknownSymbols.size == 2) {
                        val variableName  : String = unknownSymbols[0]
                        val variableValue : String = unknownSymbols[1]
                        // If variableValue is digit, the user is creating a variable
                        if(variableValue.contains(allNumber)) userVariables[variableName] = variableValue
                        // ...otherwise, the user is replacing an existing name
                        else if(variableValue in userVariables) userVariables[variableName] = userVariables[variableValue].toString()
                    }

                    // If there are no known symbols, but the first known symbol is a literal, the user is definining a new
                    // opeation, by providing its own behavior
                    else if(knownSymbols.isEmpty() && unknownSymbols.first().matches(allChars)) {
                            val typedefCommands: LinkedList<String> = LinkedList()
                            typedefCommands.addAll(unknownSymbols.subList(1, unknownSymbols.size))
                            userOperations[unknownSymbols.first()] = typedefCommands.joinToString(" ")
                    }

                    // If rightChunk is empty, but leftChunk is larger than 1, the user is defining an operation
                    else if(knownSymbols.isNotEmpty() && unknownSymbols.size > 2) {
                        val variableName  : String = unknownSymbols[0]
                        userOperations[variableName] = (unknownSymbols.subList(1, unknownSymbols.size) + knownSymbols).joinToString(" ")
                    }

                    // ...otherwise, the user is defining a new name for an original instruction or a chain of them
                    else {
                        val typedefCommands: LinkedList<String> = LinkedList()
                        typedefCommands.addAll(knownSymbols)
                        renamedInstructions[unknownSymbols.first()] = typedefCommands
                    }
            }else {
                // Check if the user wants to execute a custom operation
                if(instruction in userOperations) {
                    val result = evaluteCodeLine(userOperations[instruction].toString().split(" "))
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

/*
    FROM OFFICIAL KOTLIN documentation:
        To declare an extension function, prefix its name with a receiver type,
        which refers to the type being extended.
        The "this" keyword inside an extension function corresponds to the receiver object
        (the one that is passed before the dot)
*/

    // Since this is a generic definition function,
    // it require to express some placeholder
        // In the following, I am defining a function
        // which is able to perform appending given
        // two List<T>. Thus, it require the function
        // to have <T> before fun keyword

fun <T> List<T>.customAppend(list: List<T>): List<T> = if(list.isEmpty()) this else {
    val newList : MutableList <T> = mutableListOf()
    this.forEach { item : T -> newList.add(item)};
    list.forEach { item : T -> newList.add(item)}
    newList
}

fun List<Any>.customConcat(): List<Any> = if(this.isEmpty()) this else {
    this.customFoldLeft(emptyList(), { accumulator, value ->
        // If the current element is a list of list, invoke recursively the same function
        if (value is List<*>) accumulator.customAppend(value as List<Any>).customConcat()
        // If the current element is just a value, then append it
        else accumulator.customAppend(listOf(value))
    })
}

fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> = if(this.isEmpty()) this else {
    val newList : MutableList <T> = mutableListOf()
    for(element in this) if(predicate(element)) newList.add(element)
    newList
}

val List<Any>.customSize: Int get() = this.customFoldLeft(0, {acc: Int, _ -> acc + 1 })

fun <T, U> List<T>.customMap(transform: (T) -> U): List<U> = if(this.isEmpty()) emptyList() else {
    val mapped   : MutableList <U> = mutableListOf()
    for(element in this) mapped.add(transform(element))
    mapped
}

fun <T, U> List<T>.customFoldLeft(initial: U, f: (U, T) -> U): U = if(this.isEmpty()) initial else {
    var result = initial
    for(element in this) result = f(result, element)
    result
}

fun <T, U> List<T>.customFoldRight(initial: U, f: (T, U) -> U): U = if(this.isEmpty()) initial else {
    this.customReverse().customFoldLeft(initial, { value, accumulator -> f(accumulator, value) })
}

fun <T> List<T>.customReverse(): List<T> = if(this.isEmpty()) this else {
    val reverse   : MutableList <T> = mutableListOf()
    for((index, element) in this.withIndex()) reverse.add(this.elementAt((this.size - 1) - index))
    reverse
}
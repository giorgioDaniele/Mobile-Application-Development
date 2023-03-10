class BankAccount {


    // This implementation is not so far
    // from a condition variable patter

    // There is a variable (isOpen), and
    // depending on its own value, up and running threads
    // could wait to access a shared source
    
    private var isOpen = true
    var balance: Long = 0
        // Define a getter for the above parameter
        get() {
            check(isOpen)
            return field
        }

    fun adjustBalance(amount: Long) {
        synchronized(this) {
            this.balance += amount
        }
    }
    fun close() {
        isOpen = false
    }
}
package contributors

/**
 * add vm option -Dkotlinx.coroutines.debug
 * to show coroutine debug log
 */
fun main() {
    setDefaultFontSize(18f)
    ContributorsUI().apply {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
    }
}
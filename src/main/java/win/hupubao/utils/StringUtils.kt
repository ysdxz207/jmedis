package win.hupubao.utils

object StringUtils {
    fun isNumeric(input: String): Boolean =
            try {
                input.toDouble()
                true
            } catch (e: NumberFormatException) {
                false
            }

    fun isEmpty(str: String?): Boolean {
        return str == null || str.isEmpty()
    }

}
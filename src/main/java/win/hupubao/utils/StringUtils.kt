package win.hupubao.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import java.lang.Exception

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

    fun isJson(value: String): Boolean {

        try {
            val o: Any = JSON.parse(value) ?: return false

            if (o !is JSONObject && o !is JSONArray) {
                return false
            }

        } catch (e: Exception) {
            return false
        }

        return true
    }

}
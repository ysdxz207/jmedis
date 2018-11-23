package win.hupubao.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.serializer.SerializerFeature
import win.hupubao.enums.FormatType

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

    fun isJson(value: String?): Boolean {

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

    fun formatJson(value: String?, deepFormat: Boolean): String? {
        if (isEmpty(value) || !StringUtils.isJson(value)) {
            return value
        }

        return if (deepFormat) {
            value
        } else {
            JSON.toJSONString(JSON.parse(value), SerializerFeature.PrettyFormat)
        }
    }

    fun formatJson(value: String?, formatType: FormatType): String? {
        if (isEmpty(value) || !StringUtils.isJson(value)) {
            return value
        }
        return when (formatType) {
            FormatType.Json -> formatJson(value, false)
            FormatType.Text ->  if (isJson(value)) JSON.parse(value).toString() else value
            FormatType.JsonPlus -> formatJson(value, true)
        }
    }

}
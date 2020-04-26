package win.hupubao.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.parser.ParserConfig
import com.alibaba.fastjson.serializer.SerializerFeature
import win.hupubao.beans.RedisValue
import win.hupubao.enums.FormatType

object StringUtils {
    fun isNumeric(input: String): Boolean =
            try {
                input.toDouble()
                true
            } catch (e: NumberFormatException) {
                false
            }

    fun isEmpty(str: Any?): Boolean {
        return str == null || str.toString().isEmpty()
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

    fun parseToJson(value: Any?, deep: Boolean): Any? {
        if (value == null || isEmpty(value.toString()) || !isJson(value.toString())) {
            return value
        }
        val json = JSON.parse(value.toString())

        if (!deep) {
            return json
        }

        if (json is JSONObject) {
            for (e in json.entries) {
                e.setValue(parseToJson(e.value, deep))
            }
        } else if (json is JSONArray){
            for (index in json.indices) {
                json[index] = parseToJson(json[index], deep)
            }
        }
        return json
    }

    fun formatJson(value: String?, deepFormat: Boolean): String? {
        if (isEmpty(value) || !isJson(value)) {
            return value
        }

        return JSON.toJSONString(parseToJson(value, deepFormat), SerializerFeature.PrettyFormat)
    }

    fun formatJson(value: Any?, hash:Boolean, formatType: FormatType): String? {
        if (isEmpty(value)) {
            return value.toString()
        }

        if (value is String && !isJson(value)) {
            return value.toString()
        }

        val isString = value is String

        val jsonValue = if (isString) {
            value.toString()
        } else {
            JSON.toJSONString(value)
        }
        return when (formatType) {
            FormatType.Json -> formatJson(jsonValue, false)
            FormatType.Text ->  jsonValue
            FormatType.JsonPlus -> formatJson(jsonValue, true)
            FormatType.JsonPlusList -> if (hash) {
                val json = parseToJson(value, true) as JSONObject
                JSON.toJSONString(json.values, true)
            } else {
                formatJson(jsonValue, true)
            }
        }
    }

}
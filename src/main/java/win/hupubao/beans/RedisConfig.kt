package win.hupubao.beans

class RedisConfig {


    var id: Long? = null
    var alias: String? = null
    var host: String? = null
    var port: Int? = null
    var auth: String? = null

    override fun toString(): String {
        return alias!!
    }
}

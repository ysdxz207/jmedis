package win.hupubao.beans

class RedisDB {
    var index: Int? = null
    var name: String? = null
    var keysNum: Int = 0
    var expires: Long? = null

    override fun toString(): String {
        return "$name - $keysNum keys"
    }
}

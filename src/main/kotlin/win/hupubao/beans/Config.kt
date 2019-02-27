package win.hupubao.beans

class Config {

    var startup: Boolean? = null
    var redisConfigList: MutableList<RedisConfig> = emptyList<RedisConfig>().toMutableList()
}

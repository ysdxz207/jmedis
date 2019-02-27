package win.hupubao.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import org.apache.commons.io.FileUtils
import win.hupubao.beans.Config
import java.io.File
import java.io.IOException

/**
 * @author ysdxz207
 * @date 2018-11-19 09:46:48
 * 配置管理
 */

object ConfigUtils {
    private val CONFIG_FILE_NAME = System.getProperty("user.home") + "/.jmedis/conf.json"
    private val CONFIG_FILE: File
    private val ENCODING = "UTF-8"
    /**
     * 缓存
     */
    private var CONFIG: Config? = null


    init {
        CONFIG_FILE = File(CONFIG_FILE_NAME)
        if (!CONFIG_FILE.exists()) {
            val parent = CONFIG_FILE.parentFile
            if (!parent.exists()) {
                parent.mkdirs()
            }
            try {
                CONFIG_FILE.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            initConfig()
        }
    }

    private fun initConfig() {

        val config = Config()
        config.startup = false
        save(config)
    }

    fun save(config: Config) {
        try {
            val it = config.redisConfigList.iterator()
            while (it.hasNext()) {
                if (it.next().id == null) {
                    it.remove()
                }
            }
            FileUtils.writeStringToFile(CONFIG_FILE, JSON.toJSONString(config, SerializerFeature.PrettyFormat), ENCODING)
            CONFIG = config
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun get(): Config {
        if (CONFIG != null) {
            return CONFIG!!
        }
        try {
            val str = FileUtils.readFileToString(CONFIG_FILE, ENCODING)
            val json = JSON.parseObject(str)
            CONFIG = JSON.toJavaObject(json, Config::class.java)
            return CONFIG!!
        } catch (e: IOException) {
            e.printStackTrace()
            error("Config error.")
        }
    }

    fun deleteRedisConfigById(id: Long) {
        val config = get()
        val it = config.redisConfigList.iterator()
        while (it.hasNext()) {
            val redisConfig = it.next()
            if (id == redisConfig.id) {
                it.remove()
            }
        }
        save(config)
    }

}

/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.hupubao.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.*
import redis.clients.jedis.exceptions.JedisConnectionException
import redis.clients.jedis.exceptions.JedisDataException
import win.hupubao.common.exception.JedisConfigException

import java.util.*

/**
 * @author W.feihong
 * @date 2017-08-03
 */
object RedisUtils {

    private var host: String? = null
    private var port: Int = 0
    private var password: String? = null
    private var maxActive: Int = 0
    private var maxIdle: Int = 0
    private var maxWait: Int = 0
    private var timeout: Int = 0

    private var jedisPool: JedisPool? = null


    /**
     * 需要关闭jedis
     * @return
     */
    private val jedis: Jedis?
        get() {
            var jedis: Jedis? = null
            try {
                jedis = jedisPool!!.getResource()
            } catch (e: Exception) {
                if (e.cause is JedisDataException) {

                    error("Jedis 连接配置错误，请检查redis.properties文件。异常信息：" + e.cause.message)
                }
                if (e.cause is JedisConnectionException) {
                    throw JedisConnectionException("Redis可能未启动。异常信息：" + e.cause.message)
                }
            }

            return jedis
        }

    init {
        //读取相关的配置
        val resourceBundle = ResourceBundle.getBundle("redis")
        host = resourceBundle.getString("redis.host")
        port = Integer.parseInt(resourceBundle.getString("redis.port"))
        password = resourceBundle.getString("redis.password")
        maxActive = Integer.parseInt(resourceBundle.getString("redis.pool.maxActive"))
        maxIdle = Integer.parseInt(resourceBundle.getString("redis.pool.maxIdle"))
        maxWait = Integer.parseInt(resourceBundle.getString("redis.pool.maxWait"))
        timeout = Integer.parseInt(resourceBundle.getString("redis.pool.timeout"))


        init(host, port, password)
    }

    private fun init(host: String, port: Int, password: String) {
        // 建立连接池配置参数
        val config = JedisPoolConfig()
        // 设置最大连接数
        config.setMaxTotal(maxActive)
        // 设置最大阻塞时间
        config.setMaxWaitMillis(maxWait)
        // 设置空间连接
        config.setMaxIdle(maxIdle)
        if (StringUtils.isNotBlank(password)) {
            jedisPool = JedisPool(config, host, port, timeout, password)
        } else {
            jedisPool = JedisPool(config, host, port)
        }
    }

    fun testConnection() {
        jedis!!.use({ jedis ->
            jedis!!.set("TEST_CONNECTION", "connected")
            jedis!!.del("TEST_CONNECTION")
        })

    }

    operator fun get(key: String): String {
        jedis!!.use({ jedis -> return jedis!!.get(key) })
    }

    operator fun <T> get(key: String, clazz: Class<T>): T? {
        val str = get(key)
        return if (StringUtils.isBlank(str)) {
            null
        } else JSON.parseObject(str, clazz)
    }

    fun <T> getList(key: String, clazz: Class<T>): List<T>? {
        val str = get(key)
        return if (StringUtils.isBlank(str)) {
            null
        } else JSON.parseArray(str, clazz)
    }

    operator fun set(key: String, value: String) {
        jedis!!.use({ jedis -> jedis!!.set(key, value) })
    }

    operator fun set(key: String, value: String, expireSeconds: Int) {
        jedis!!.use({ jedis ->
            jedis!!.set(key, value)
            jedis!!.expire(key, expireSeconds)
        })
    }

    fun delete(vararg keys: String): Long {
        val pattern = JSON.toJSONString(keys).indexOf("*") != -1
        var num: Long = 0
        if (pattern) {
            for (key in keys) {
                num += delete(key)
            }
            return num
        }
        jedis!!.use({ jedis -> return jedis!!.del(keys) })
    }

    fun delete(pattern: String): Long {
        val keysSet = RedisUtils.keys(pattern)
        val keys = keysSet.toTypedArray()
        return if (keys.size == 0) {
            0L
        } else RedisUtils.delete(*keys)
    }

    fun keys(pattern: String): Set<String> {
        jedis!!.use({ jedis -> return jedis!!.keys(pattern) })
    }

    fun <T> getDefault(key: String, clazz: Class<T>, defaultValue: T): T? {
        val str = get(key)
        return if (StringUtils.isBlank(str)) {
            defaultValue
        } else JSONObject.parseObject(str, clazz)
    }

    private fun selectDB(shardedJedis: ShardedJedis, dbIndex: Int) {
        val collection = shardedJedis.getAllShards()
        val iterator = collection.iterator()
        while (iterator.hasNext()) {
            val jedis = iterator.next()
            jedis.select(dbIndex)
        }

    }

    @JvmStatic
    fun main(args: Array<String>) {
        val jedisShardInfo = JedisShardInfo("http://127.0.0.1:6379/2")
        jedisShardInfo.setPassword("123456")
        System.out.println(jedisShardInfo.getDb())
        val shards = Arrays.asList<JedisShardInfo>(
                jedisShardInfo
        )

        val config = GenericObjectPoolConfig()

        val shardedJedisPool = ShardedJedisPool(config, shards)

        val shardedJedis = shardedJedisPool.getResource()
        println(JSON.toJSONString(shardedJedis.getAllShardInfo()))
    }
}

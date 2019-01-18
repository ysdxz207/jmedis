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
import redis.clients.jedis.*
import win.hupubao.beans.RedisDB

import kotlin.collections.ArrayList

/**
 * @author ysdxz207
 * @date 2018-11-20
 */
object RedisUtils {

    lateinit var jedisPool: JedisPool
    private const val MAX_ACTIVE = 50
    private const val MAX_WAIT = 1000L
    private const val MAX_IDLE = 10
    // 2 seconds
    private const val TIMEOUT = 2000

    private var DB_INDEX_DEFAULT = 0


    /**
     * 需要关闭jedis
     * @return
     */
    private val jedis: Jedis
        get() {
            val jedis: Jedis = jedisPool.resource
            jedis.select(DB_INDEX_DEFAULT)
            return jedis
        }

    fun config(host: String,
               port: Int,
               password: String) {

        config(host, port, password, MAX_ACTIVE, MAX_IDLE, MAX_WAIT, TIMEOUT)
    }

    fun config(host: String,
               port: Int,
               password: String,
               maxActive: Int,
               maxIdle: Int,
               maxWait: Long,
               timeout: Int) {

        // 建立连接池配置参数
        val config = JedisPoolConfig()
        // 设置最大连接数
        config.maxTotal = maxActive
        // 设置最大阻塞时间
        config.maxWaitMillis = maxWait
        // 设置空间连接
        config.maxIdle = maxIdle

        jedisPool = if (StringUtils.isEmpty(password)) {
            JedisPool(config, host, port)
        } else {
            JedisPool(config, host, port, timeout, password)
        }
    }

    fun testConnection() {
        jedis.use { jedis ->
            jedis.set("TEST_CONNECTION", "connected")
            jedis.del("TEST_CONNECTION")
        }

    }

    operator fun get(key: String): String? {
        jedis.use { jedis -> return jedis.get(key) }
    }

    operator fun <T> get(key: String, clazz: Class<T>): T? {
        val str = get(key)
        return if (StringUtils.isEmpty(str)) {
            null
        } else JSON.parseObject(str, clazz)
    }

    fun hget(key: String, field: String): String? {
        jedis.use { jedis -> return jedis.hget(key, field) }
    }

    fun hset(key: String, field: String, value: String): Long {
        jedis.use { jedis -> return jedis.hset(key, field, value) }
    }

    fun <T> getList(key: String, clazz: Class<T>): List<T>? {
        val str = get(key)
        return if (StringUtils.isEmpty(str)) {
            null
        } else JSON.parseArray(str, clazz)
    }

    operator fun set(key: String, value: String) {
        jedis.use { jedis -> jedis.set(key, value) }
    }

    operator fun set(key: String, value: String, expireSeconds: Int) {
        jedis.use { jedis ->
            jedis.set(key, value)
            jedis.expire(key, expireSeconds)
        }
    }

    fun delete(vararg keys: String): Long {
        val pattern = keys.indexOf("*") != -1
        var num: Long = 0
        if (pattern) {
            for (key in keys) {
                num += delete(key)
            }
            return num
        }
        jedis.use { jedis -> return jedis.del(*keys) }
    }

    fun delete(pattern: String): Long {
        val keysSet = RedisUtils.keys(pattern)
        val keys = keysSet.toTypedArray()
        return if (keys.isEmpty()) {
            0L
        } else RedisUtils.delete(*keys)
    }

    fun keys(pattern: String): Set<String> {
        jedis.use { jedis -> return jedis.keys(pattern) }
    }

    fun <T> getDefault(key: String, clazz: Class<T>, defaultValue: T): T? {
        val str = get(key)
        return if (StringUtils.isEmpty(str)) {
            defaultValue
        } else JSONObject.parseObject(str, clazz)
    }

    fun selectDB(dbIndex: Int) {
        DB_INDEX_DEFAULT = dbIndex
    }

    fun info(selection: String): String {
        return jedis.use { jedis -> jedis.info(selection) }
    }

    fun configGet(pattern: String): List<String> {
        return jedis.use { jedis -> jedis.configGet(pattern) }
    }

    fun configSet(parameter: String, value: String): String {
        return jedis.use { jedis -> jedis.configSet(parameter, value) }
    }

    fun dbList(): List<RedisDB> {
        val listDB = ArrayList<RedisDB>()
        val keyspace = info("keyspace")
        val list = keyspace.replace("\"", "").split("\r\n")
        for (str in list.subList(1, list.size - 1)) {
            val redisDB = RedisDB()
            val ls = str.split(":")
            val lsMap = ls[1].split(",")
            redisDB.index = ls[0].replace("db", "").toInt()
            redisDB.name = ls[0]
            redisDB.keysNum = lsMap[0].split("=")[1].toInt()
            redisDB.expires = lsMap[1].split("=")[1].toLong()
            listDB.add(redisDB)
        }
        return listDB.sortedBy {
            it.name
        }
    }



    fun hkeys(key: String): Set<String> {
        return jedis.use { jedis -> jedis.hkeys(key) }
    }

    fun hvals(key: String): List<String> {
        return jedis.use { jedis -> jedis.hvals(key) }
    }

    fun hgetAll(key: String): Map<String, String> {
        return jedis.use { jedis -> jedis.hgetAll(key) }
    }

    fun hdel(key: String, field: String): Long {
        return jedis.use { jedis -> jedis.hdel(key, field) }
    }

    fun ttl(key: String): Long {
        return jedis.use { jedis -> jedis.ttl(key) }
    }

    fun expire(key: String, expireSeconds: Int): Long {
        jedis.use { jedis -> return jedis.expire(key, expireSeconds) }
    }

    @JvmStatic
    fun main(args: Array<String>) {

        config("127.0.0.1", 6379, "123456")

        print(JSON.toJSONString(dbList()))

    }
}

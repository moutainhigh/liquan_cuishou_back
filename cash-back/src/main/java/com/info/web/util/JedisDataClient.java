package com.info.web.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import com.info.back.utils.ObjectTranscoderUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis操作
 *
 * @author gaoyuhai
 */
public class JedisDataClient {

    private static JedisPool poolMaster;

    private JedisDataClient() {

    }

    private static Logger log = Logger.getLogger(JedisDataClient.class);

    private synchronized static JedisPool initMasterPool() throws Exception {
        if (null == poolMaster) {
            String redisHostMaster = null;
            int redisPort;
            int timeout;
            try {
                redisHostMaster = RedisConfigConstant.getConstant("redisHostMaster").trim();
                redisPort = Integer.parseInt(RedisConfigConstant.getConstant("redisPort").trim());
                timeout = Integer.parseInt(RedisConfigConstant.getConstant("timeout").trim());
                log.info("redisHostMaster ip:" + redisHostMaster);
                log.info("redis port:" + redisPort);
                log.info("redis timeout:" + timeout);
                JedisPoolConfig configMaster = new JedisPoolConfig();
                configMaster.setMaxTotal(30000);// 设置最大连接数
                configMaster.setMaxIdle(100); // 设置最大空闲数
                configMaster.setMinIdle(10);
                configMaster.setMaxWaitMillis(10000);// 设置超时时间
                configMaster.setTestWhileIdle(true);
                configMaster.setTestOnBorrow(false);
                poolMaster = new JedisPool(configMaster, redisHostMaster,
                        redisPort, timeout);
            } catch (Exception e) {
                throw e;
            }
        }
        return poolMaster;
    }

    private synchronized static Jedis getMasterJedis() throws Exception {
        if (poolMaster == null) {
            initMasterPool();
        }
        Jedis jedis = null;
        if (poolMaster != null) {
            jedis = poolMaster.getResource();
//			jedis.auth("xjxcuishou001");
            jedis.auth(RedisConfigConstant.getConstant("redisPassword").trim());
        }
        return jedis;
    }

    private static void returnResource(final Jedis jedis,
                                       final JedisPool jedisPool) {
        if (jedis != null && jedisPool != null) {
            jedis.disconnect();
            jedisPool.returnResourceObject(jedis);
        }
    }

    /**
     * 向redis加入数据
     *
     * @param key
     * @param value
     * @throws Exception
     */
    public static void set(String key, String value) throws Exception {
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            jedisMaster.set(key, value);
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 向redis加入String类型数据,并设置保存时间
     *
     * @param key
     * @param value
     * @param seconds 数据保存时间（单位：秒）
     * @throws Exception
     */
    public static void set(String key, String value, int seconds)
            throws Exception {
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            jedisMaster.set(key, value);
            jedisMaster.expire(key, seconds);

        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 从redis取值
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static String get(String key) throws Exception {
        if (null == key || "".equals(key.trim())) {
            return null;
        }
        Jedis jedisMaster = null;
        Jedis jedisSlave = null;
        String value = null;
        try {
            jedisMaster = getMasterJedis();
            value = jedisMaster.get(key);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                returnResource(jedisMaster, poolMaster);
            } catch (Exception e) {
                throw e;
            }
        }
        return value;
    }

    public static long del(String key) throws Exception {
        if (null == key || "".equals(key.trim())) {
            return 0;
        }
        long value = 0;
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            value = jedisMaster.del(key);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                returnResource(jedisMaster, poolMaster);
            } catch (Exception e) {
                throw e;
            }
        }
        return value;
    }

    public static List<String> hmget(String key, String... files)
            throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        List<String> list = null;
        try {
            jedisMaster = getMasterJedis();
            list = jedisMaster.hmget(key, files);
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
        return list;
    }

    public static void hmset(String key, Map<String, String> map,
                             Integer seconds) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            jedisMaster.hmset(key, map);
            if (null != seconds && seconds > 0) {
                jedisMaster.expire(key, seconds);
            }
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    public static void hset(String key, String mapKey, String mapValue,
                            Integer seconds) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            jedisMaster.hset(key, mapKey, mapValue);
            if (null != seconds && seconds > 0) {
                jedisMaster.expire(key, seconds);
            }
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 续期
     *
     * @param key
     * @param seconds
     * @throws Exception
     */
    public static void expire(String key, int seconds) throws Exception {
        if (null == key || "".equals(key.trim())) {
            return;
        }
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            jedisMaster.expire(key, seconds);
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 向List尾部加入数据，放入缓存，如果key不存在，创建该对象
     *
     * @param key
     * @param
     * @throws Exception
     */
    public static void setList(String key, List<String> list, int... times)
            throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            if (null != list && list.size() > 0) {
                jedisMaster.rpush(key, list.toArray(new String[list.size()]));
                if (null == times || times.length == 0) {
                    expire(key, (int) TimeUnit.MINUTES.toSeconds(30));
                } else {
                    expire(key, times[0]);
                }
                log.debug(">>>>>>>>>>>list key=" + key);
            }
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 向List尾部加入单个数据，放入缓存，如果key不存在，创建该对象
     *
     * @param key
     * @param
     * @param :第一次插入,
     * @throws Exception
     */
    public static void setList(String key, String listValue) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            jedisMaster.lpush(key, listValue);
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    public static void rpush(String key, String listValue) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            jedisMaster.rpush(key, listValue);
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }
    public static String lpop(String key) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        String value = "";
        try {
            jedisMaster = getMasterJedis();
            value = jedisMaster.lpop(key);
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
        return value;
    }
    public static Integer llen(String key) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        Integer length = null;
        try {
            jedisMaster = getMasterJedis();
            length = jedisMaster.llen(key).intValue();
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
        return length;
    }

    /**
     * 向list加入数据放入缓存，并保持list中的数据不重复
     *
     * @param key
     * @param list
     * @param times
     * @throws Exception
     */
    public static void setUniqueList(String key, List<String> list,
                                     int... times) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            if (null != list && list.size() > 0) {
                Set<String> set = new LinkedHashSet<String>();
                set.addAll(list);
                List<String> oldList = jedisMaster.lrange(key, 0, -1);
                set.addAll(oldList);
                set.addAll(list);
                list = new ArrayList<String>(set);
                jedisMaster.del(key);
                jedisMaster.rpush(key, list.toArray(new String[list.size()]));

				/* 如果设置times则指定了过期时间，不写则永久 */
                // if(null == times || times.length == 0){
                // expire(key, (int)TimeUnit.MINUTES.toSeconds(30));
                // } else {
                // expire(key, times[0]);
                // }
            }
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * Description:取出list中的数据，start从0开始，end=-1表示全部取出
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @throws Exception
     */
    public static List<String> getList(String key, long start, long end)
            throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        List<String> list = null;
        try {
            jedisMaster = getMasterJedis();
            list = jedisMaster.lrange(key, start, end);
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
        return list;
    }

    /**
     * 删除指定值的元素
     *
     * @param key
     * @param values
     * @throws Exception
     */
    public synchronized static void delListItem(String key, String... values)
            throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        List<String> list = null;
        try {
            jedisMaster = getMasterJedis();
            list = jedisMaster.lrange(key, 0, -1);
            if ((null != list && list.size() > 0)
                    && (null != values && values.length > 0)) {
                for (int j = 0; j < values.length; j++) {
                    for (int i = 0; i < list.size(); i++) {
                        if (values[j].equals(list.get(i))) {
                            list.remove(i);
                            break;
                        }
                    }
                }
            }
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 取出map所有的key
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static List<String> hkeys(String key) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        List<String> list = null;
        try {
            jedisMaster = getMasterJedis();
            Set<String> set = jedisMaster.hkeys(key);
            if (null != set && set.size() > 0) {
                return new ArrayList<String>(set);
            }
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
        return list;
    }

    /**
     * 取出map所有的value
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static List<String> hvals(String key) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        List<String> list = null;
        try {
            jedisMaster = getMasterJedis();
            list = jedisMaster.hvals(key);
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
        return list;
    }

    /**
     * 判断是否存在
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static boolean exists(String key) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            return jedisMaster.exists(key);
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 删除map中的某个/些键值
     *
     * @param key
     * @param fields
     * @return
     * @throws Exception
     */
    public static Long hdel(String key, String... fields) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            return jedisMaster.hdel(key, fields);
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 从redis取所有的key
     *
     * @param
     * @return
     * @throws Exception
     */
    public static String getKeys() throws Exception {
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            Set<String> keys = jedisMaster.keys("*");
            Iterator<String> it = keys.iterator();
            StringBuffer sb = new StringBuffer();
            while (it.hasNext()) {
                String key = it.next();
//				System.out.println("key=" + key);
            }
            return sb.toString();
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 从redis取所有的key
     *
     * @param
     * @return
     * @throws Exception
     */
    public static List<String> getAllKeys() throws Exception {
        Jedis jedisMaster = null;
        List<String> keyList = new ArrayList<String>();
        try {
            jedisMaster = getMasterJedis();
            Set<String> keys = jedisMaster.keys("*");
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
//				System.out.println("key=" + key);
                keyList.add(key);
            }
            return keyList;
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    public static void batchDel(String pattern) throws Exception{
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            Set<String> keys = jedisMaster.keys(pattern);
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String keyStr = it.next();
                jedisMaster.del(keyStr);
            }
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    public static List<String> getAllValuesByPattern(String pattern) throws Exception {
        Jedis jedisMaster = null;
        List<String> valuesList = new ArrayList<String>();
        try {
            jedisMaster = getMasterJedis();
            Set<String> keys = jedisMaster.keys(pattern);
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                valuesList.add(jedisMaster.get(it.next()));
            }
            return valuesList;
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }


    public static List<String> getAllKeysByPattern(String pattern) throws Exception {
        Jedis jedisMaster = null;
        List<String> keyList = new ArrayList<String>();
        try {
            jedisMaster = getMasterJedis();
            Set<String> keys = jedisMaster.keys(pattern);
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                keyList.add(key);
            }
            return keyList;
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 删除list中对应value的值
     *
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    public static boolean delRedisKeyByValue(String key, String value)
            throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            jedisMaster.lrem(key, 0, value);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 删除所有的key
     *
     * @throws Exception
     */
    public static void delRediss() throws Exception {
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            Set<String> keys = jedisMaster.keys("*");
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                log.info("key = " + key);
                jedisMaster.del(key);
            }
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 更新list中数据
     *
     * @param key
     * @param index 索引
     * @param value
     * @throws Exception
     */
    public static void lset(String key, long index, String value)
            throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            jedisMaster.lset(key, index, value);
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 获取value存在list的索引值
     *
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    public static int getIndex(String key, String value) throws Exception {
        if (null == key || "".equals(key.trim())) {
            throw new Exception("the key can not be null.");
        }
        Jedis jedisMaster = null;
        List<String> list = null;
        try {
            jedisMaster = getMasterJedis();
            list = jedisMaster.lrange(key, 0, -1);
            if ((null != list && list.size() > 0)) {
                for (int i = 0; i < list.size(); i++) {
                    if (value.equals(list.get(i))) {
                        return i;
                    }
                }
            }
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
        return -1;
    }

    public static void main(String[] args) throws Exception {
        delRediss();
        // JedisDataClient.del();
        // JedisDataClient.delRedisKeyByValue(tendListKey, tenderKey);
        // getKeys();
        // set("xyz", "0000");
        //
        // boolean b = exists("xyz");
        //
        // System.out.println(b);
        // System.out.println(get("xyz"));
        // {"flag":1,"id":10104,"tenderNum":21,"tenderRate":2.1,"userId":0}
//		delRedisKeyByValue("TWO_BORROW_TMC_10001",
//				"{\"flag\":1,\"id\":10105,\"tenderNum\":30,\"tenderRate\":3,\"userId\":0}");
//		setList("TWO_BORROW_TMC_10001",
//				"{\"flag\":0,\"id\":10105,\"tenderNum\":30,\"tenderRate\":3,\"userId\":0}");
//		getKeys();
    }

    /**
     * 设置 list
     *
     * @param <T>
     * @param key
     * @param list
     */
    public static <T> void setList(String keyPrefix, String key, List<T> list, int expireTime) {
        Jedis jedisMaster = null;
        try {
            if (!CollectionUtils.isEmpty(list)) {
                jedisMaster = getMasterJedis();
                String realKey = keyPrefix + key;
                jedisMaster.set(realKey.getBytes(), ObjectTranscoderUtil.serialize(list));
                expire(realKey, expireTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 获取list
     *
     * @param <T>
     * @param key
     * @return list
     */
    public static <T> List<T> getList(String keyPrefix, String key) {
        String realKey = keyPrefix + key;
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            if (jedisMaster == null || !jedisMaster.exists(realKey.getBytes())) {
                return null;
            }
            byte[] in = jedisMaster.get(realKey.getBytes());
            List<T> list = (List<T>) ObjectTranscoderUtil.deserialize(in);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
        return null;
    }

    /**
     * 设置 map
     *
     * @param <T>
     * @param key
     * @param map
     */
    public static <T> void setMap(String key, Map<String, T> map) {
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            jedisMaster.set(key.getBytes(), ObjectTranscoderUtil.serialize(map));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
    }

    /**
     * 获取list
     *
     * @param <T>
     * @param key
     * @return list
     */
    public static <T> Map<String, T> getMap(String keyPrefix, String key) {
        String realKey = keyPrefix + key;
        Jedis jedisMaster = null;
        try {
            jedisMaster = getMasterJedis();
            if (jedisMaster == null || !jedisMaster.exists(realKey)) {
                return null;
            }
            byte[] in = jedisMaster.get(key.getBytes());
            Map<String, T> map = (Map<String, T>) ObjectTranscoderUtil.deserialize(in);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnResource(jedisMaster, poolMaster);
        }
        return null;
    }


}

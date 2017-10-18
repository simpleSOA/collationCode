package com.ly.database.redis;

import redis.clients.jedis.*;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.util.SafeEncoder;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author: Administrator
 * Date: 2016/3/25 Time: 13:57
 */
public class JedisUtil {
    private static final JedisUtil REDIS = new JedisUtil();
    private static final String SETNX_EXPIRE_SCRIPT = "if redis.call('setnx', KEYS[1], KEYS[2]) == 1 then\n"
            + "return redis.call('expire', KEYS[1], KEYS[3]);\n"
            + "end\n"
            + "return nil;";
    /**
     * 缓存生存时间
     */
    private final int expire = 60000;
    private static JedisPool jedisPool;

    private JedisUtil() {
        init();
    }

    /**
     * 构建redis连接池
     *
     * @return JedisPool
     */
    public static void init() {
        ResourceBundle bundle = ResourceBundle.getBundle("redis");
        if (bundle == null) {
            throw new IllegalArgumentException("[redis.properties] is not found!");
        }
        if (jedisPool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            jedisPool = new JedisPool(config, bundle.getString("redis.ip"), Integer.parseInt(bundle.getString("redis.port")));
        }
    }

    public JedisPool getPool() {
        return jedisPool;
    }

    /**
     * 从jedis连接池中获取获取jedis对象
     *
     * @return
     */
    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    /**
     * 获取JedisUtil实例
     *
     * @return
     */
    public static JedisUtil getInstance() {
        return REDIS;
    }


    /**
     * 设置过期时间
     *
     * @param key
     * @param seconds 单位秒
     */
    public void expire(String key, int seconds) {
        if (seconds <= 0) {
            return;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.expire(key, seconds);
        } finally {
            jedis.close();
        }

    }

    /**
     * 设置默认过期时间
     *
     * @param key
     * @author ruan 2013-4-11
     */
    public void expire(String key) {
        expire(key, expire);
    }

    private boolean isNotBlank(String str) {
        return str != null && str.length() > 0;
    }

    /****************************** 操作Key的方法 *******************************/
    /**
     * 清空所有key
     */
    public String flushAll() {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.flushAll();
        } finally {
            jedis.close();
        }
    }

    /**
     * setnx带过期时间功能
     *
     * @param key     键名
     * @param value   键值
     * @param seconds 单位秒
     * @return 成功返回true, 失败false
     * @see redis.clients.jedis.Jedis#setnx(String, String)
     * @see redis.clients.jedis.Jedis#expire(String, int)
     */
    public boolean setnxAndExpire(String key, String value, int seconds) {
        Jedis redis = null;
        try {
            redis = getJedis();
            Object result = redis.eval(SETNX_EXPIRE_SCRIPT, 3, key, value, seconds + "");
            return result != null;
        } finally {
            redis.close();
        }
    }

    /**
     * 当 oldkey 已经存在时,将 oldkey 改名为 newkey，如果不存在该oldkey,将会发生异常
     *
     * @param oldkey oldkey
     * @param newkey newkey
     * @return 成功返回true，其他情况false
     */
    public boolean rename(String oldkey, String newkey) {
        return "OK".equals(rename(SafeEncoder.encode(oldkey),
                SafeEncoder.encode(newkey)));
    }

    /**
     * 当且仅当 newkey 不存在时，将 key 改名为 newkey
     *
     * @param oldkey
     * @param newkey
     * @return 修改成功返回true，如果newkey存在返回false
     */
    public boolean renamenx(String oldkey, String newkey) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.renamenx(oldkey, newkey) == 1;
        } finally {
            jedis.close();
        }
    }

    /**
     * 更改key
     *
     * @param oldkey
     * @param newkey
     * @return 状态码
     */
    private String rename(byte[] oldkey, byte[] newkey) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.rename(oldkey, newkey);
        } finally {
            jedis.close();
        }
    }

    /**
     * 设置key的过期时间，以秒为单位
     *
     * @param key
     * @param seconds,以秒为单位
     * @return 成功返回true，当 key 不存在或者不能为 key 设置生存时间时,返回false
     */
    public boolean expired(String key, int seconds) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.expire(key, seconds) == 1;
        } finally {
            jedis.close();
        }
    }

    /**
     * 设置key的过期时间,它是距历元（即格林威治标准时间 1970 年 1 月 1 日的 00:00:00，格里高利历）的偏移量。
     *
     * @param key
     * @param timestamp unix时间戳，以秒为单位
     * @return 成功返回true，当 key 不存在或者不能为 key 设置生存时间时,返回false
     */
    public boolean expireAt(String key, long timestamp) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.expireAt(key, timestamp) == 1;
        } finally {
            jedis.close();
        }
    }

    /**
     * 设置key的过期时间
     *
     * @return 当 key 不存在时，返回 -2，当 key 存在但没有设置剩余生存时间时，返回 -1，否则，以秒为单位，返回 key 的剩余生存时间
     */
    public long ttl(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.ttl(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 取消对key过期时间的设置
     *
     * @return 修改成功返回true，如果 key 不存在或 key 没有设置生存时间，返回false
     */
    public boolean persist(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.persist(key) == 1;
        } finally {
            jedis.close();
        }
    }

    /**
     * 删除keys对应的记录,可以是多个key
     *
     * @return 删除的记录数
     */
    public long del(String... keys) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.del(keys);
        } finally {
            jedis.close();
        }
    }


    /**
     * 判断key是否存在
     *
     * @return 存在true, 否则false
     */
    public boolean exists(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.exists(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 对List,Set,SortSet进行排序,如果集合数据较大应避免使用这个方法
     *
     * @return List<String> 集合的全部记录
     **/
    public List<String> sort(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sort(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 对List,Set,SortSet进行排序或limit
     *
     * @param param 定义排序类型或limit的起止位置.
     * @return List<String> 全部或部分记录
     **/
    public List<String> sort(String key, SortingParams param) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sort(key, param);
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回指定key存储的类型
     *
     * @return String string|list|set|zset|hash|none
     **/
    public String type(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.type(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 查找所有匹配给定的模式的键
     * 在一个大的数据库中使用可能造成性能问题,慎用
     *
     * @param pattern key的表达式,*表示多个，？表示一个
     */
    public Set<String> keys(String pattern) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.keys(pattern);
        } finally {
            jedis.close();
        }
    }

    /*********************对存储结构为Set类型的操作*************************/

    /**
     * 向Set添加一条记录，如果member已存在返回0,否则返回1
     *
     * @return 返回加入不是重复元素的个数
     */
    public long sadd(String key, String... member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sadd(key, member);
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取给定key中元素个数
     *
     * @param key
     * @return 元素个数
     */
    public long scard(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.scard(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回从第一组和所有的给定集合之间的差异的成员
     *
     * @param keys
     * @return 差异的成员集合
     */
    public Set<String> sdiff(String... keys) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sdiff(keys);
        } finally {
            jedis.close();
        }
    }

    /**
     * 这个命令等于sdiff,但返回的不是结果集,而是将结果集存储在新的集合中，如果目标已存在，则覆盖。
     *
     * @param newkey 新结果集的key
     * @param keys   比较的集合
     * @return 新集合中的记录数
     **/
    public long sdiffstore(String newkey, String... keys) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sdiffstore(newkey, keys);
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回给定集合交集的成员,如果其中一个集合为不存在或为空，则返回空Set
     *
     * @param keys
     * @return 交集成员的集合
     **/
    public Set<String> sinter(String... keys) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sinter(keys);
        } finally {
            jedis.close();
        }
    }

    /**
     * 这个命令等于sinter,但返回的不是结果集,而是将结果集存储在新的集合中，如果目标已存在，则覆盖。
     *
     * @param newkey 新结果集的key
     * @param keys   比较的集合
     * @return 新集合中的记录数
     **/
    public long sinterstore(String newkey, String... keys) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sinterstore(newkey, keys);
        } finally {
            jedis.close();
        }
    }

    /**
     * 确定一个给定的值是否存在
     *
     * @param key
     * @param member 要判断的值
     * @return 存在返回true, 如果 member 元素不是集合的成员，或 key 不存在，返回false
     **/
    public boolean sismember(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sismember(key, member);
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回集合中的所有成员
     *
     * @return 成员集合
     */
    public Set<String> smembers(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.smembers(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 将成员从源集合移出放入目标集合 <br/>
     * 如果源集合不存在或不包哈指定成员，不进行任何操作，返回0<br/>
     * 否则该成员从源集合上删除，并添加到目标集合，如果目标集合中成员已存在，则只在源集合进行删除
     *
     * @param srckey 源集合
     * @param dstkey 目标集合
     * @param member 源集合中的成员
     * @return 状态码，1成功，0失败
     */
    public long smove(String srckey, String dstkey, String member) {
        Jedis jedis = getJedis();
        long s = jedis.smove(srckey, dstkey, member);
        jedis.close();
        return s;
    }

    /**
     * 从集合中删除成员
     *
     * @param key
     * @return 被删除的成员
     */
    public String spop(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.spop(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 从集合中删除指定成员
     *
     * @param key
     * @param members 要删除的成员
     * @return 被成功移除的元素的数量，不包括被忽略的元素
     */
    public long srem(String key, String... members) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.srem(key, members);
        } finally {
            jedis.close();
        }
    }

    /**
     * 合并多个集合并返回合并后的结果，合并后的结果集合并不保存<br/>
     *
     * @param keys
     * @return 合并后的结果集合
     */
    public Set<String> sunion(String... keys) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sunion(keys);
        } finally {
            jedis.close();
        }
    }

    /**
     * 合并多个集合并将合并后的结果集保存在指定的新集合中，如果新集合已经存在则覆盖
     *
     * @param newkey 新集合的key
     * @param keys   要合并的集合
     **/
    public long sunionstore(String newkey, String... keys) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sunionstore(newkey, keys);
        } finally {
            jedis.close();
        }
    }

    /******************对存储结构为SortedSet(排序的)类型的操作*********************************/


    /**
     * 向集合中增加一条记录,如果这个值已存在，这个值对应的权重将被置为新的权重
     *
     * @param key
     * @param score  权重
     * @param member 要加入的值，
     * @return 1成功添加的新成员，0表示被更新的、已经存在的成员
     */
    public long zadd(String key, double score, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zadd(key, score, member);
        } finally {
            jedis.close();
        }
    }

    public long zadd(String key, Map<String, Double> scoreMembers) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zadd(key, scoreMembers);
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取集合中元素的数量
     *
     * @param key
     * @return 如果返回0则集合不存在
     */
    public long zcard(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zcard(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取指定闭区间内集合的数量
     *
     * @param key
     * @param min 最小排序位置
     * @param max 最大排序位置
     * @return 返回成员在min和max之间的数量
     */
    public long zcount(String key, double min, double max) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zcount(key, min, max);
        } finally {
            jedis.close();
        }
    }

    /**
     * 权重增加给定值，如果给定的member已存在，否则则相当于新增
     *
     * @param key
     * @param score  要增的权重
     * @param member 要插入的值
     * @return 增后的权重
     */
    public double zincrby(String key, double score, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zincrby(key, score, member);
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
     *
     * @param key
     * @param start 开始位置(包含)
     * @param end   结束位置(包含)
     * @return Set<String>
     */
    public Set<String> zrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrange(key, start, end);
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回指定权重区间的元素集合
     *
     * @param key
     * @param min 上限权重
     * @param max 下限权重
     * @return Set<String>
     */
    public Set<String> zrangeByScore(String key, double min, double max) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrangeByScore(key, min, max);
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递增(从小到大)顺序排列
     *
     * @return 排名位置
     */
    public long zrank(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrank(key, member);
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取指定值在集合中的位置，集合排序从高到低
     *
     * @return 排名位置
     */
    public long zrevrank(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrevrank(key, member);
        } finally {
            jedis.close();
        }
    }

    /**
     * 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略。从集合中删除成员
     *
     * @return 被成功移除的成员的数量，不包括被忽略的成员
     */
    public long zrem(String key, String... member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrem(key, member);
        } finally {
            jedis.close();
        }
    }

    /**
     * 删除给定位置区间的元素
     *
     * @param key
     * @param start 开始区间，从0开始(包含)
     * @param end   结束区间,-1为最后一个元素(包含)
     * @return 删除的数量
     */
    public long zremrangeByRank(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zremrangeByRank(key, start, end);
        } finally {
            jedis.close();
        }
    }

    /**
     * 删除给定权重区间的元素
     *
     * @param key
     * @param min 下限权重(包含)
     * @param max 上限权重(包含)
     * @return 删除的数量
     */
    public long zremrangeByScore(String key, double min, double max) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zremrangeByScore(key, min, max);
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回有序集 key 中，指定区间内的成员。
     * 其中成员的位置按 score 值递减(从大到小)来排列。
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order)排列。
     */
    public Set<String> zrevrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrevrange(key, start, end);
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取给定值在集合中的权重
     *
     * @return double 权重
     */
    public Double zscore(String key, String memebr) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zscore(key, memebr);
        } finally {
            jedis.close();
        }
    }


    /**
     * 对存储结构为HashMap类型的操作
     */

    /**
     * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
     *
     * @param key
     * @param fieid 存储的名字
     * @return 被成功移除的域的数量，不包括被忽略的域。
     */
    public long hdel(String key, String... fieid) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hdel(key, fieid);
        } finally {
            jedis.close();
        }
    }

    /**
     * 测试hash中指定的存储是否存在
     *
     * @return true存在，false不存在
     */
    public boolean hexists(String key, String fieid) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hexists(key, fieid);
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回hash中指定存储位置的值
     *
     * @return 存储对应的值
     */
    public String hget(String key, String fieid) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hget(key, fieid);
        } finally {
            jedis.close();
        }
    }

    /**
     * 以Map的形式返回hash中的存储和值
     */
    public Map<String, String> hgetAll(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hgetAll(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 将哈希表 key 中的域 field 的值设为 value
     *
     * @return 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1,否则返回0
     **/
    public long hset(String key, String fieid, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hset(key, fieid, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 添加对应关系，只有在fieid不存在时才执行
     *
     * @return 状态码 1成功，0失败fieid已存
     **/
    public long hsetnx(String key, String fieid, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hsetnx(key, fieid, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取hash中value的集合
     *
     * @return List<String>
     */
    public List<String> hvals(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hvals(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 在指定的存储位置加上指定的数字，存储位置的值必须可转为数字类型
     *
     * @param key
     * @param fieid 存储位置
     * @param value 要增加的值,可以是负数
     * @return 增加指定数字后，存储位置的值
     */
    public long hincrby(String key, String fieid, long value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hincrBy(key, fieid, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回指定hash中的所有存储名字,类似Map中的keySet方法
     *
     * @param key
     * @return Set<String> 存储名称的集合
     */
    public Set<String> hkeys(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hkeys(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取hash中存储的个数，类似Map中size方法
     *
     * @param key
     * @return long 存储的个数
     */
    public long hlen(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hlen(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 根据多个key，获取对应的value，返回List,如果指定的key不存在,List对应位置为null
     *
     * @return List<String>
     */
    public List<String> hmget(String key, String... fieids) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hmget(key, fieids);
        } finally {
            jedis.close();
        }
    }

    /**
     * 添加对应关系，如果对应关系已存在，则覆盖
     *
     * @param key
     * @param map 对应关系
     * @return 成功返回true
     */
    public boolean hmset(String key, Map<String, String> map) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return "OK".equals(jedis.hmset(key, map));
        } finally {
            jedis.close();
        }
    }


    /**
     * 对存储结构为String类型的操作
     */
    /**
     * 根据key获取记录
     *
     * @param key
     * @return 值
     */
    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.get(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 添加有过期时间的记录
     *
     * @param key
     * @param seconds 过期时间，以秒为单位
     * @param value
     * @return String 操作状态
     */
    public boolean setEx(String key, int seconds, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return "OK".equals(jedis.setex(key, seconds, value));
        } finally {
            jedis.close();
        }
    }

    /**
     * 添加一条记录，仅当给定的key不存在时才插入
     *
     * @return true插入成功且key不存在，false未插入，key存在
     */
    public boolean setnx(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return 1 == jedis.setnx(key, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 添加记录,如果记录已存在将覆盖原有的value,
     * 在 Redis 2.6.12 版本以前,该方法总是返回 true,
     * 从 Redis 2.6.12 版本开始,该操作成功完成时,    才返回 true
     */
    public boolean set(String key, String value) {
        return set(SafeEncoder.encode(key), SafeEncoder.encode(value));
    }

    /**
     * 添加记录,如果记录已存在将覆盖原有的value
     *
     * @return 状态码
     */
    private boolean set(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return "OK".equals(jedis.set(key, value));
        } finally {
            jedis.close();
        }
    }

    /**
     * 从指定位置开始插入数据，插入的数据会覆盖指定位置以后的数据<br/>
     * 例:String str1="123456789";<br/>
     * 对str1操作后setRange(key,4,0000)，str1="123400009";
     *
     * @return 修改后字符串的长度
     */
    public long setRange(String key, long offset, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.setrange(key, offset, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 在指定的key中追加value
     *
     * @return 追加后字符串的长度
     **/
    public long append(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.append(key, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 将key对应的value减去指定的值，只有value可以转为数字时该方法才可用
     *
     * @return long 减后的值
     */
    public long decrBy(String key, long number) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.decrBy(key, number);
        } finally {
            jedis.close();
        }
    }

    /**
     * <b>可以作为获取唯一id的方法</b><br/>
     * 将key对应的value加上指定的值，只有value可以转为数字时该方法才可用
     *
     * @return 相加后的值
     */
    public long incrBy(String key, long number) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.incrBy(key, number);
        } finally {
            jedis.close();
        }
    }

    /**
     * 对指定key对应的value进行截取
     *
     * @param key
     * @param startOffset 开始位置(包含)
     * @param endOffset   结束位置(包含)
     * @return String 截取的值
     */
    public String getrange(String key, long startOffset, long endOffset) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.getrange(key, startOffset, endOffset);
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取并设置指定key对应的value<br/>
     * 如果key存在返回之前的value,否则返回null
     *
     * @return String 原始value或null
     */
    public String getSet(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.getSet(key, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 批量获取记录,如果指定的key不存在返回List的对应位置将是null
     *
     * @param keys
     * @return List<String> 值得集合
     */
    public List<String> mget(String... keys) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.mget(keys);
        } finally {
            jedis.close();
        }
    }

    /**
     * 批量存储记录
     * <blockquote>
     * mset("key1","value1","key2","value2")
     * </blockquote>
     *
     * @return String 状态码
     */
    public void mset(String... keysvalues) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.mset(keysvalues);
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取key对应的值的长度
     *
     * @return value值得长度
     */
    public long strlen(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.strlen(key);
        } finally {
            jedis.close();
        }
    }


    /*************************对存储结构为List类型的操作*************/

    /**
     * List长度
     *
     * @param key
     * @return 长度
     */
    public long llen(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.llen(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 覆盖操作,将覆盖List中指定位置的值
     *
     * @param key
     * @param index 位置
     * @param value 值
     * @return 操作状态
     */
    public boolean lset(String key, int index, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return "OK".equals(jedis.lset(key, index, value));
        } finally {
            jedis.close();
        }
    }

    /**
     * 在value的相对位置插入记录
     * 当 pivot 不存在于列表 key 时，不执行任何操作。
     * 当 key 不存在时， key 被视为空列表，不执行任何操作。
     * 如果 key 不是列表类型，返回一个错误。
     *
     * @param key
     * @param where 前面插入或后面插入
     * @param pivot 相对位置的内容
     * @param value 插入的内容
     * @return 如果命令执行成功，返回插入操作完成之后，列表的长度,如果没有找到 pivot ，返回 -1,如果 key 不存在或为空列表，返回 0
     */
    public long linsert(String key, LIST_POSITION where, String pivot,
                        String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.linsert(key, where, pivot, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回列表 key 中，下标为 index 的元素
     *
     * @param key
     * @param index 位置
     * @return 列表中下标为 index 的元素,如果 index 参数的值不在列表的区间范围内,返回null
     **/
    public String lindex(String key, int index) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lindex(key, index);
        } finally {
            jedis.close();
        }
    }

    /**
     * 将List中的第一条记录移出List
     *
     * @param key
     * @return 移出的记录
     */
    public String lpop(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lpop(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 将List中最后第一条记录移出List
     *
     * @param key
     * @return 移出的记录
     */
    public String rpop(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.rpop(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表头,
     * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表头
     *
     * @return 记录总数
     */
    public long lpush(String key, String... value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lpush(key, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表尾(最右边)。
     * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表尾
     *
     * @return 记录总数
     */
    public long rpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.rpush(key, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取指定闭区间范围的记录
     *
     * @return List
     */
    public List<String> lrange(String key, long start, long end) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lrange(key, start, end);
        } finally {
            jedis.close();
        }
    }

    /**
     * 删除List中c条记录，被删除的记录值为value
     *
     * @param key
     * @param c     要删除的数量，如果为负数则从List的尾部检查并删除符合的记录
     * @param value 要匹配的值
     * @return 删除后的List中的记录数
     */
    public long lrem(String key, int c, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lrem(key, c, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除
     *
     * @param key
     * @param start 记录的开始位置(0表示第一条记录)
     * @param end   记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
     * @return 执行状态码
     */
    public boolean ltrim(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return "OK".equals(jedis.ltrim(key, start, end));
        } finally {
            jedis.close();
        }
    }
}

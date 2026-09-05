package com.wxy.zzarental.common.util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 通用操作工具类
 * <p>
 * 基于 {@link StringRedisTemplate} 封装，key 和 value 均为 String 类型。
 * 覆盖 Key 通用操作、String、Hash、List、Set、ZSet 以及分布式锁等常用场景，
 * 业务中直接注入使用即可，无需再感知底层 RedisTemplate 的 API。
 *
 * @author wxy
 * @description Redis 常用操作方法封装
 * @date 2026/09/05
 */
@Component
public class RedisUtil {

    /**
     * 释放锁的 Lua 脚本：仅当 key 对应的 value 等于请求标识时才会删除，防止误删他人持有的锁
     */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ============================ Key 通用操作 ============================

    /**
     * 判断 key 是否存在
     *
     * @param key Redis 的 key
     * @return true-存在；false-不存在
     */
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 删除单个 key
     *
     * @param key Redis 的 key
     * @return true-删除成功；false-删除失败或 key 不存在
     */
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 批量删除 key
     *
     * @param keys key 集合
     * @return 实际删除的数量
     */
    public Long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return stringRedisTemplate.delete(keys);
    }

    /**
     * 根据 pattern 模糊查询所有匹配的 key
     * <p>注意：生产环境数据量大时建议使用 scan 命令，避免阻塞 Redis</p>
     *
     * @param pattern key 匹配模式，例如 zza:user:*
     * @return 匹配到的 key 集合
     */
    public Set<String> keys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }

    /**
     * 根据 pattern 模糊删除匹配的 key
     *
     * @param pattern key 匹配模式
     * @return 实际删除的数量
     */
    public Long deleteByPattern(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return stringRedisTemplate.delete(keys);
    }

    /**
     * 设置 key 的过期时间
     *
     * @param key     Redis 的 key
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true-设置成功；false-设置失败或 key 不存在
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    /**
     * 设置 key 在指定时间点过期
     *
     * @param key  Redis 的 key
     * @param date 过期时间点
     * @return true-设置成功；false-设置失败或 key 不存在
     */
    public Boolean expireAt(String key, Date date) {
        return stringRedisTemplate.expireAt(key, date);
    }

    /**
     * 获取 key 剩余过期时间
     *
     * @param key Redis 的 key
     * @return 剩余秒数；-1 表示永不过期；-2 表示 key 不存在
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    /**
     * 获取 key 剩余过期时间，可指定时间单位
     *
     * @param key  Redis 的 key
     * @param unit 时间单位
     * @return 指定单位下的剩余时间
     */
    public Long getExpire(String key, TimeUnit unit) {
        return stringRedisTemplate.getExpire(key, unit);
    }

    /**
     * 移除 key 的过期时间，使其持久保存
     *
     * @param key Redis 的 key
     * @return true-移除成功；false-移除失败或 key 原本就没有过期时间
     */
    public Boolean persist(String key) {
        return stringRedisTemplate.persist(key);
    }

    // ============================ String 操作 ============================

    /**
     * 缓存普通字符串
     *
     * @param key   Redis 的 key
     * @param value 字符串值
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存字符串并设置过期时间
     *
     * @param key     Redis 的 key
     * @param value   字符串值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 仅当 key 不存在时写入（SETNX），常用于幂等校验或加锁
     *
     * @param key   Redis 的 key
     * @param value 字符串值
     * @return true-写入成功；false-key 已存在，写入失败
     */
    public Boolean setIfAbsent(String key, String value) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 仅当 key 不存在时写入并设置过期时间（SET NX EX）
     *
     * @param key     Redis 的 key
     * @param value   字符串值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true-写入成功；false-key 已存在，写入失败
     */
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 获取字符串值
     *
     * @param key Redis 的 key
     * @return 对应的值；key 不存在时返回 null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 获取旧值并写入新值
     *
     * @param key   Redis 的 key
     * @param value 新值
     * @return 写入前的旧值
     */
    public String getAndSet(String key, String value) {
        return stringRedisTemplate.opsForValue().getAndSet(key, value);
    }

    /**
     * 批量获取值
     *
     * @param keys key 集合
     * @return 值列表，顺序与入参 keys 保持一致，不存在的 key 对应位置为 null
     */
    public List<String> multiGet(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量写入
     *
     * @param map key-value 映射
     */
    public void multiSet(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        stringRedisTemplate.opsForValue().multiSet(map);
    }

    /**
     * key 对应的数字自增 1
     *
     * @param key Redis 的 key
     * @return 自增后的值
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * key 对应的数字自增指定步长
     *
     * @param key   Redis 的 key
     * @param delta 自增步长（可为负数）
     * @return 自增后的值
     */
    public Long incrementBy(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * key 对应的数字自增 double 步长
     *
     * @param key   Redis 的 key
     * @param delta 自增步长（可为负数）
     * @return 自增后的值
     */
    public Double incrementByDouble(String key, double delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * key 对应的数字自减 1
     *
     * @param key Redis 的 key
     * @return 自减后的值
     */
    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    /**
     * key 对应的数字自减指定步长
     *
     * @param key   Redis 的 key
     * @param delta 自减步长
     * @return 自减后的值
     */
    public Long decrementBy(String key, long delta) {
        return stringRedisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 向字符串尾部追加内容
     *
     * @param key   Redis 的 key
     * @param value 待追加的内容
     * @return 追加后的字符串长度
     */
    public Integer append(String key, String value) {
        return stringRedisTemplate.opsForValue().append(key, value);
    }

    /**
     * 获取字符串值的长度
     *
     * @param key Redis 的 key
     * @return 字符串长度；key 不存在时返回 0
     */
    public Long strLen(String key) {
        return stringRedisTemplate.opsForValue().size(key);
    }

    // ============================ Hash 操作 ============================

    /**
     * 向 Hash 中写入一个 field-value
     *
     * @param key   Hash 的 key
     * @param field 字段名
     * @param value 字段值
     */
    public void hashSet(String key, String field, String value) {
        hashOperations().put(key, field, value);
    }

    /**
     * 向 Hash 中批量写入 field-value
     *
     * @param key    Hash 的 key
     * @param values field-value 映射
     */
    public void hashSetAll(String key, Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        hashOperations().putAll(key, values);
    }

    /**
     * 仅当 Hash 中 field 不存在时写入
     *
     * @param key   Hash 的 key
     * @param field 字段名
     * @param value 字段值
     * @return true-写入成功；false-field 已存在，写入失败
     */
    public Boolean hashSetIfAbsent(String key, String field, String value) {
        return hashOperations().putIfAbsent(key, field, value);
    }

    /**
     * 获取 Hash 中指定 field 的值
     *
     * @param key   Hash 的 key
     * @param field 字段名
     * @return 字段值；field 不存在时返回 null
     */
    public String hashGet(String key, String field) {
        return hashOperations().get(key, field);
    }

    /**
     * 批量获取 Hash 中多个 field 的值
     *
     * @param key    Hash 的 key
     * @param fields 字段名集合
     * @return 字段值列表，顺序与入参 fields 保持一致
     */
    public List<String> hashMultiGet(String key, Collection<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }
        return hashOperations().multiGet(key, fields);
    }

    /**
     * 获取 Hash 中全部 field-value
     *
     * @param key Hash 的 key
     * @return field-value 映射
     */
    public Map<String, String> hashGetAll(String key) {
        return hashOperations().entries(key);
    }

    /**
     * 删除 Hash 中一个或多个 field
     *
     * @param key      Hash 的 key
     * @param hashKeys 待删除的 field
     * @return 实际删除的 field 数量
     */
    public Long hashDelete(String key, Object... hashKeys) {
        if (hashKeys == null || hashKeys.length == 0) {
            return 0L;
        }
        return hashOperations().delete(key, hashKeys);
    }

    /**
     * 判断 Hash 中指定 field 是否存在
     *
     * @param key   Hash 的 key
     * @param field 字段名
     * @return true-存在；false-不存在
     */
    public Boolean hashHasKey(String key, String field) {
        return hashOperations().hasKey(key, field);
    }

    /**
     * Hash 中指定 field 的数字自增 1
     *
     * @param key   Hash 的 key
     * @param field 字段名
     * @return 自增后的值
     */
    public Long hashIncrement(String key, String field) {
        return hashOperations().increment(key, field, 1L);
    }

    /**
     * Hash 中指定 field 的数字自增指定步长
     *
     * @param key   Hash 的 key
     * @param field 字段名
     * @param delta 自增步长（可为负数）
     * @return 自增后的值
     */
    public Long hashIncrementBy(String key, String field, long delta) {
        return hashOperations().increment(key, field, delta);
    }

    /**
     * Hash 中指定 field 的数字自增 double 步长
     *
     * @param key   Hash 的 key
     * @param field 字段名
     * @param delta 自增步长（可为负数）
     * @return 自增后的值
     */
    public Double hashIncrementByDouble(String key, String field, double delta) {
        return hashOperations().increment(key, field, delta);
    }

    /**
     * 获取 Hash 中全部 field 名
     *
     * @param key Hash 的 key
     * @return field 集合
     */
    public Set<String> hashKeys(String key) {
        return hashOperations().keys(key);
    }

    /**
     * 获取 Hash 中全部 field 对应的值
     *
     * @param key Hash 的 key
     * @return 值列表
     */
    public List<String> hashValues(String key) {
        return hashOperations().values(key);
    }

    /**
     * 获取 Hash 的 field 数量
     *
     * @param key Hash 的 key
     * @return field 数量
     */
    public Long hashSize(String key) {
        return hashOperations().size(key);
    }

    /**
     * 获取强类型化的 Hash 操作对象，field 和 value 均为 String
     *
     * @return HashOperations 操作对象
     */
    private HashOperations<String, String, String> hashOperations() {
        return stringRedisTemplate.opsForHash();
    }

    // ============================ List 操作 ============================

    /**
     * 向列表头部（左侧）压入一个元素
     *
     * @param key   List 的 key
     * @param value 元素值
     * @return 压入后的列表长度
     */
    public Long leftPush(String key, String value) {
        return stringRedisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 向列表头部批量压入元素
     *
     * @param key    List 的 key
     * @param values 元素集合
     * @return 压入后的列表长度
     */
    public Long leftPushAll(String key, Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        return stringRedisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * 向列表头部批量压入元素
     *
     * @param key    List 的 key
     * @param values 元素（可变参数）
     * @return 压入后的列表长度
     */
    public Long leftPushAll(String key, String... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        return stringRedisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * 向列表尾部（右侧）压入一个元素
     *
     * @param key   List 的 key
     * @param value 元素值
     * @return 压入后的列表长度
     */
    public Long rightPush(String key, String value) {
        return stringRedisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 向列表尾部批量压入元素
     *
     * @param key    List 的 key
     * @param values 元素集合
     * @return 压入后的列表长度
     */
    public Long rightPushAll(String key, Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        return stringRedisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * 向列表尾部批量压入元素
     *
     * @param key    List 的 key
     * @param values 元素（可变参数）
     * @return 压入后的列表长度
     */
    public Long rightPushAll(String key, String... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        return stringRedisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * 从列表头部弹出（移除并返回）一个元素
     *
     * @param key List 的 key
     * @return 头部元素；列表为空时返回 null
     */
    public String leftPop(String key) {
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    /**
     * 阻塞式从列表头部弹出元素，超时时间内列表无元素则返回 null
     *
     * @param key     List 的 key
     * @param timeout 阻塞等待时间
     * @param unit    时间单位
     * @return 头部元素；超时无元素时返回 null
     */
    public String leftPop(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.opsForList().leftPop(key, timeout, unit);
    }

    /**
     * 从列表尾部弹出（移除并返回）一个元素
     *
     * @param key List 的 key
     * @return 尾部元素；列表为空时返回 null
     */
    public String rightPop(String key) {
        return stringRedisTemplate.opsForList().rightPop(key);
    }

    /**
     * 阻塞式从列表尾部弹出元素，超时时间内列表无元素则返回 null
     *
     * @param key     List 的 key
     * @param timeout 阻塞等待时间
     * @param unit    时间单位
     * @return 尾部元素；超时无元素时返回 null
     */
    public String rightPop(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.opsForList().rightPop(key, timeout, unit);
    }

    /**
     * 获取列表中指定区间的元素
     *
     * @param key   List 的 key
     * @param start 起始下标，从 0 开始，可为负数（-1 表示最后一个元素）
     * @param end   结束下标，可为负数
     * @return 区间内的元素列表
     */
    public List<String> listRange(String key, long start, long end) {
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取列表长度
     *
     * @param key List 的 key
     * @return 列表长度
     */
    public Long listSize(String key) {
        return stringRedisTemplate.opsForList().size(key);
    }

    /**
     * 裁剪列表，只保留指定区间内的元素
     *
     * @param key   List 的 key
     * @param start 起始下标
     * @param end   结束下标
     */
    public void listTrim(String key, long start, long end) {
        stringRedisTemplate.opsForList().trim(key, start, end);
    }

    /**
     * 获取列表中指定下标的元素
     *
     * @param key   List 的 key
     * @param index 下标，可为负数（-1 表示最后一个元素）
     * @return 对应下标的元素；下标越界时返回 null
     */
    public String listIndex(String key, long index) {
        return stringRedisTemplate.opsForList().index(key, index);
    }

    /**
     * 修改列表中指定下标的元素
     *
     * @param key   List 的 key
     * @param index 下标
     * @param value 新元素值
     */
    public void listSet(String key, long index, String value) {
        stringRedisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 从列表中移除指定数量的匹配元素
     *
     * @param key   List 的 key
     * @param count count &gt; 0 时从头部开始移除 count 个；
     *              count &lt; 0 时从尾部开始移除 -count 个；count = 0 时移除全部
     * @param value 待移除的元素
     * @return 实际移除的数量
     */
    public Long listRemove(String key, long count, String value) {
        return stringRedisTemplate.opsForList().remove(key, count, value);
    }

    // ============================ Set 操作 ============================

    /**
     * 向 Set 中添加元素
     *
     * @param key    Set 的 key
     * @param values 待添加的元素（可变参数）
     * @return 实际新增的元素数量
     */
    public Long setAdd(String key, String... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        return stringRedisTemplate.opsForSet().add(key, values);
    }

    /**
     * 向 Set 中批量添加元素
     *
     * @param key    Set 的 key
     * @param values 待添加的元素集合
     * @return 实际新增的元素数量
     */
    public Long setAdd(String key, Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        return stringRedisTemplate.opsForSet().add(key, values.toArray(new String[0]));
    }

    /**
     * 从 Set 中移除一个或多个元素
     *
     * @param key    Set 的 key
     * @param values 待移除的元素
     * @return 实际移除的元素数量
     */
    public Long setRemove(String key, Object... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        return stringRedisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 获取 Set 中全部元素
     *
     * @param key Set 的 key
     * @return 元素集合
     */
    public Set<String> setMembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    /**
     * 判断元素是否存在于 Set 中
     *
     * @param key   Set 的 key
     * @param value 元素
     * @return true-存在；false-不存在
     */
    public Boolean setIsMember(String key, String value) {
        return stringRedisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取 Set 的元素数量
     *
     * @param key Set 的 key
     * @return 元素数量
     */
    public Long setSize(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    /**
     * 随机弹出 Set 中的一个元素
     *
     * @param key Set 的 key
     * @return 弹出的元素；Set 为空时返回 null
     */
    public String setPop(String key) {
        return stringRedisTemplate.opsForSet().pop(key);
    }

    /**
     * 随机弹出 Set 中指定数量的元素
     *
     * @param key   Set 的 key
     * @param count 弹出数量
     * @return 弹出的元素列表
     */
    public List<String> setPop(String key, long count) {
        return stringRedisTemplate.opsForSet().pop(key, count);
    }

    /**
     * 随机获取 Set 中的一个元素（不删除）
     *
     * @param key Set 的 key
     * @return 随机元素；Set 为空时返回 null
     */
    public String setRandomMember(String key) {
        return stringRedisTemplate.opsForSet().randomMember(key);
    }

    /**
     * 求 key 与其它 Set 的并集
     *
     * @param key       Set 的 key
     * @param otherKeys 其它 Set 的 key 集合
     * @return 并集元素集合
     */
    public Set<String> setUnion(String key, Collection<String> otherKeys) {
        return stringRedisTemplate.opsForSet().union(key, otherKeys);
    }

    /**
     * 求 key 与其它 Set 的交集
     *
     * @param key       Set 的 key
     * @param otherKeys 其它 Set 的 key 集合
     * @return 交集元素集合
     */
    public Set<String> setIntersect(String key, Collection<String> otherKeys) {
        return stringRedisTemplate.opsForSet().intersect(key, otherKeys);
    }

    /**
     * 求 key 与其它 Set 的差集（存在于 key 中但不存在于其它 Set 中的元素）
     *
     * @param key       Set 的 key
     * @param otherKeys 其它 Set 的 key 集合
     * @return 差集元素集合
     */
    public Set<String> setDifference(String key, Collection<String> otherKeys) {
        return stringRedisTemplate.opsForSet().difference(key, otherKeys);
    }

    // ============================ ZSet 操作 ============================

    /**
     * 向 ZSet 添加一个元素并设置分数
     *
     * @param key   ZSet 的 key
     * @param value 元素
     * @param score 分数
     * @return true-新增成功；false-元素已存在，仅更新分数
     */
    public Boolean zAdd(String key, String value, double score) {
        return stringRedisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 向 ZSet 批量添加带分数的元素
     *
     * @param key    ZSet 的 key
     * @param tuples 带分数的元素集合
     * @return 实际新增的元素数量
     */
    public Long zAdd(String key, Set<ZSetOperations.TypedTuple<String>> tuples) {
        if (tuples == null || tuples.isEmpty()) {
            return 0L;
        }
        return stringRedisTemplate.opsForZSet().add(key, tuples);
    }

    /**
     * 从 ZSet 中移除一个或多个元素
     *
     * @param key    ZSet 的 key
     * @param values 待移除的元素
     * @return 实际移除的数量
     */
    public Long zRemove(String key, Object... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        return stringRedisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 获取 ZSet 中指定元素的分数
     *
     * @param key   ZSet 的 key
     * @param value 元素
     * @return 分数；元素不存在时返回 null
     */
    public Double zScore(String key, String value) {
        return stringRedisTemplate.opsForZSet().score(key, value);
    }

    /**
     * ZSet 中指定元素分数自增（可为负数）
     *
     * @param key   ZSet 的 key
     * @param value 元素
     * @param delta 分数增量
     * @return 自增后的分数
     */
    public Double zIncrementScore(String key, String value, double delta) {
        return stringRedisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    /**
     * 获取元素在 ZSet 中的升序排名（从 0 开始）
     *
     * @param key   ZSet 的 key
     * @param value 元素
     * @return 升序排名；元素不存在时返回 null
     */
    public Long zRank(String key, String value) {
        return stringRedisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 获取元素在 ZSet 中的降序排名（从 0 开始，分数最高排 0）
     *
     * @param key   ZSet 的 key
     * @param value 元素
     * @return 降序排名；元素不存在时返回 null
     */
    public Long zReverseRank(String key, String value) {
        return stringRedisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 按升序获取 ZSet 中指定排名区间的元素
     *
     * @param key   ZSet 的 key
     * @param start 起始排名
     * @param end   结束排名
     * @return 元素集合
     */
    public Set<String> zRange(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 按降序获取 ZSet 中指定排名区间的元素
     *
     * @param key   ZSet 的 key
     * @param start 起始排名
     * @param end   结束排名
     * @return 元素集合
     */
    public Set<String> zReverseRange(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 按升序获取 ZSet 中指定排名区间的元素及分数
     *
     * @param key   ZSet 的 key
     * @param start 起始排名
     * @param end   结束排名
     * @return 元素及分数集合
     */
    public Set<ZSetOperations.TypedTuple<String>> zRangeWithScores(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * 按降序获取 ZSet 中指定排名区间的元素及分数
     *
     * @param key   ZSet 的 key
     * @param start 起始排名
     * @param end   结束排名
     * @return 元素及分数集合
     */
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeWithScores(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    /**
     * 按升序获取 ZSet 中指定分数区间内的元素
     *
     * @param key ZSet 的 key
     * @param min 最小分数（含）
     * @param max 最大分数（含）
     * @return 元素集合
     */
    public Set<String> zRangeByScore(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 统计 ZSet 中指定分数区间内的元素数量
     *
     * @param key ZSet 的 key
     * @param min 最小分数（含）
     * @param max 最大分数（含）
     * @return 元素数量
     */
    public Long zCount(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * 获取 ZSet 的元素数量
     *
     * @param key ZSet 的 key
     * @return 元素数量
     */
    public Long zSize(String key) {
        return stringRedisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 移除 ZSet 中指定排名区间内的元素
     *
     * @param key   ZSet 的 key
     * @param start 起始排名
     * @param end   结束排名
     * @return 实际移除的数量
     */
    public Long zRemoveRangeByRank(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().removeRange(key, start, end);
    }

    /**
     * 移除 ZSet 中指定分数区间内的元素
     *
     * @param key ZSet 的 key
     * @param min 最小分数（含）
     * @param max 最大分数（含）
     * @return 实际移除的数量
     */
    public Long zRemoveRangeByScore(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    // ============================ 分布式锁 ============================

    /**
     * 尝试获取分布式锁（底层为 SET NX EX，获取成功自动设置过期时间防止死锁）
     *
     * @param key          锁的 key
     * @param requestId    请求标识，推荐使用 UUID，用于释放锁时校验
     * @param expireMillis 锁自动过期时间（毫秒）
     * @return true-获取锁成功；false-获取锁失败
     */
    public Boolean tryLock(String key, String requestId, long expireMillis) {
        return setIfAbsent(key, requestId, expireMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 释放分布式锁
     * <p>通过 Lua 脚本比较 value 后删除，只有锁持有者才能释放，避免误删其它线程的锁</p>
     *
     * @param key       锁的 key
     * @param requestId 加锁时的请求标识
     * @return true-释放成功；false-锁已过期或请求标识不匹配
     */
    public Boolean unlock(String key, String requestId) {
        Long result = stringRedisTemplate.execute(
                UNLOCK_SCRIPT, Collections.singletonList(key), requestId);
        return result != null && result == 1L;
    }
}

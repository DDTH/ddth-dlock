package com.github.ddth.dlock.impl.redis;

import com.github.ddth.commons.redis.JedisConnector;
import com.github.ddth.commons.redis.JedisUtils;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

/**
 * <a href="http://redis.io">Redis</a> implementation of {@link IDLock}.
 *
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class RedisDLock extends BaseRedisDLock {
    /**
     * To override the {@link #setRedisHostAndPort(String)} setting.
     */
    public final static String LOCK_PROP_REDIS_HOST_AND_PORT = "dlock.redis_host_and_port";

    private String redisHostAndPort = Protocol.DEFAULT_HOST + ":" + Protocol.DEFAULT_PORT;

    public RedisDLock(String name) {
        super(name);
    }

    /**
     * Redis' host and port scheme (format {@code host:port}).
     *
     * @return
     */
    public String getRedisHostAndPort() {
        return redisHostAndPort;
    }

    /**
     * Redis' host and port scheme (format {@code host:port}).
     *
     * @param redisHostAndPort
     * @return
     */
    public RedisDLock setRedisHostAndPort(String redisHostAndPort) {
        this.redisHostAndPort = redisHostAndPort;
        return this;
    }

    /**
     * {@inheritDocs}
     *
     * @since 0.1.1.2
     */
    @Override
    protected JedisConnector buildJedisConnector() {
        JedisConnector jedisConnector = new JedisConnector();
        jedisConnector.setJedisPoolConfig(JedisUtils.defaultJedisPoolConfig()).setRedisHostsAndPorts(redisHostAndPort)
                .init();
        return jedisConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RedisDLock init() {
        /*
         * Parse custom property: redis-host-and-port
         */
        String hostAndPort = getLockProperty(LOCK_PROP_REDIS_HOST_AND_PORT);
        if (!StringUtils.isBlank(hostAndPort)) {
            this.redisHostAndPort = hostAndPort;
        }
        super.init();
        return this;
    }

    /**
     * @return
     */
    protected Jedis getJedis() {
        return getJedisConnector().getJedis();
    }

    /*----------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    @Override
    public LockResult lock(int waitWeight, String clientId, long lockDurationMs) {
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("Invalid client-id.");
        }
        if (lockDurationMs <= 0) {
            throw new IllegalArgumentException("Lock duration must be greater than zero.");
        }
        try (Jedis jedis = getJedis()) {
            String zsetName = getZsetName();
            if (waitWeight >= 0) {
                jedis.zadd(zsetName, waitWeight, clientId);
                Long rank = jedis.zrevrank(zsetName, clientId);
                if (rank != null && rank.intValue() != 0) {
                    return LockResult.HOLD_BY_ANOTHER_CLIENT;
                }
            }
            String key = getName();
            Object response = jedis.eval(getScriptLock(), 0, key, clientId, String.valueOf(lockDurationMs));
            if (response == null) {
                updateLockHolder(jedis);
                return LockResult.HOLD_BY_ANOTHER_CLIENT;
            } else {
                updateLockHolder(clientId, lockDurationMs);
                if (waitWeight >= 0) {
                    jedis.del(zsetName);
                }
                return LockResult.SUCCESSFUL;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LockResult unlock(String clientId) {
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("Invalid client-id.");
        }
        try (Jedis jedis = getJedis()) {
            return unlockResult(jedis.eval(getScriptUnlock(), 0, getName(), clientId));
        }
    }
}

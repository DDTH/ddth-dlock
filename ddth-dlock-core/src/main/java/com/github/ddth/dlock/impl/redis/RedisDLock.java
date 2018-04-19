package com.github.ddth.dlock.impl.redis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.commons.redis.JedisConnector;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

/**
 * <a href="http://redis.io">Redis</a> implementation of {@link IDLock}.
 * 
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class RedisDLock extends BaseRedisDLock {

    private final Logger LOGGER = LoggerFactory.getLogger(RedisDLock.class);

    /**
     * To override the {@link #setRedisHostAndPort(String)} setting.
     */
    public final static String LOCK_PROP_REDIS_HOST_AND_PORT = "dlock.redis_host_and_port";

    private String redisHostAndPort = Protocol.DEFAULT_HOST + ":" + Protocol.DEFAULT_PORT;

    public RedisDLock(String name) {
        super(name);
    }

    /**
     * @return
     */
    protected Jedis getJedis() {
        return getJedisConnector().getJedis();
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
     * Sets Redis' host and port scheme (format {@code host:port}).
     * 
     * @param redisHostAndPort
     * @return
     */
    public RedisDLock setRedisHostAndPort(String redisHostAndPort) {
        this.redisHostAndPort = redisHostAndPort;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RedisDLock init() {
        super.init();

        /*
         * Parse custom property: redis-host-and-port
         */
        String hostAndPort = getLockProperty(LOCK_PROP_REDIS_HOST_AND_PORT);
        if (!StringUtils.isBlank(hostAndPort)) {
            this.redisHostAndPort = hostAndPort;
        }

        if (getJedisConnector() == null) {
            try {
                JedisConnector jedisConnector = new JedisConnector();
                jedisConnector.setRedisHostsAndPorts(redisHostAndPort).init();
                setJedisConnector(jedisConnector);
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }

        return this;
    }

    /*----------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    @Override
    public LockResult lock(String clientId, long lockDurationMs) {
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("Invalid ClientID!");
        }
        if (lockDurationMs <= 0) {
            throw new IllegalArgumentException("Lock duration must be greater than zero!");
        }
        try (Jedis jedis = getJedis()) {
            String key = getName();
            Object response = jedis.eval(getScriptLock(), 0, key, clientId,
                    String.valueOf(lockDurationMs));
            if (response == null) {
                updateLockHolder(jedis);
                return LockResult.HOLD_BY_ANOTHER_CLIENT;
            } else {
                updateLockHolder(clientId, lockDurationMs);
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
            throw new IllegalArgumentException("Invalid ClientID!");
        }
        try (Jedis jedis = getJedis()) {
            String key = getName();
            Object response = jedis.eval(getScriptUnlock(), 0, key, clientId);
            if (response == null) {
                return LockResult.HOLD_BY_ANOTHER_CLIENT;
            } else if ("0".equals(response.toString())) {
                return LockResult.NOT_FOUND;
            } else {
                return LockResult.SUCCESSFUL;
            }
        }
    }

}

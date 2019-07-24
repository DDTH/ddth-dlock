package com.github.ddth.dlock.impl.redis;

import com.github.ddth.commons.redis.JedisConnector;
import com.github.ddth.dlock.IDLockFactory;
import com.github.ddth.dlock.impl.AbstractDLockFactory;
import com.github.ddth.dlock.impl.redis.internal.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for <a href="http://redis.io">Redis</a>-based implementations of
 * {@link IDLockFactory}.
 *
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class BaseRedisDLockFactory extends AbstractDLockFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(BaseRedisDLockFactory.class);

    /**
     * Flag to mark if the Redis resource (e.g. Redis client pool) is created
     * and handled by the factory.
     */
    protected boolean myOwnRedis = false;

    private String redisPassword;
    private JedisConnector jedisConnector;

    /**
     * Password to connect to Redis server/cluster.
     *
     * @return
     */
    public String getRedisPassword() {
        return redisPassword;
    }

    /**
     * Password to connect to Redis server/cluster.
     *
     * @param redisPassword
     * @return
     */
    public BaseRedisDLockFactory setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
        return this;
    }

    /**
     * @return
     */
    protected JedisConnector getJedisConnector() {
        return jedisConnector;
    }

    /**
     * @param jedisConnector
     * @return
     */
    public BaseRedisDLockFactory setJedisConnector(JedisConnector jedisConnector) {
        if (myOwnRedis && this.jedisConnector != null) {
            this.jedisConnector.close();
        }
        this.jedisConnector = jedisConnector;
        myOwnRedis = false;
        return this;
    }

    /**
     * Build a {@link JedisConnector} instance for my own use.
     *
     * @return
     * @since 0.1.1.2
     */
    protected abstract JedisConnector buildJedisConnector();

    /**
     * {@inheritDoc}
     *
     * @since 0.1.1.2
     */
    @Override
    public BaseRedisDLockFactory init() {
        if (jedisConnector == null) {
            jedisConnector = buildJedisConnector();
            myOwnRedis = jedisConnector != null;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        try {
            super.destroy();
        } finally {
            try {
                jedisConnector = RedisUtils.closeJedisConnector(jedisConnector, myOwnRedis);
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.1.2
     */
    @Override
    public BaseRedisDLock createLock(String name) {
        return (BaseRedisDLock) super.createLock(name);
    }
}

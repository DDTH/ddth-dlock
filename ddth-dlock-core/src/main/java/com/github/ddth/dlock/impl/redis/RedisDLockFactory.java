package com.github.ddth.dlock.impl.redis;

import com.github.ddth.commons.redis.JedisConnector;
import com.github.ddth.commons.redis.JedisUtils;
import com.github.ddth.dlock.IDLockFactory;
import redis.clients.jedis.Protocol;

import java.util.Properties;

/**
 * <a href="http://redis.io">Redis</a> implementation of {@link IDLockFactory}
 * that creates {@link RedisDLock} objects.
 *
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class RedisDLockFactory extends BaseRedisDLockFactory {
    private String redisHostAndPort = Protocol.DEFAULT_HOST + ":" + Protocol.DEFAULT_PORT;

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
    public RedisDLockFactory setRedisHostAndPort(String redisHostAndPort) {
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
        jedisConnector.setJedisPoolConfig(JedisUtils.defaultJedisPoolConfig())
                .setRedisHostsAndPorts(getRedisHostAndPort()).setRedisPassword(getRedisPassword()).init();
        return jedisConnector;
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.1.2
     */
    @Override
    public RedisDLock createLock(String name) {
        return (RedisDLock) super.createLock(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RedisDLock createLockInternal(String name, Properties lockProps) {
        RedisDLock lock = new RedisDLock(name);
        lock.setLockProperties(lockProps);
        lock.setRedisHostAndPort(getRedisHostAndPort()).setRedisPassword(getRedisPassword());
        lock.setJedisConnector(getJedisConnector());
        return lock;
    }
}

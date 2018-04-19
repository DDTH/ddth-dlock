package com.github.ddth.dlock.impl.redis;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.commons.redis.JedisConnector;
import com.github.ddth.dlock.IDLockFactory;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * <a href="http://redis.io">Redis</a> implementation of {@link IDLockFactory}
 * that creates {@link RedisDLock} objects.
 * 
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class RedisDLockFactory extends BaseRedisDLockFactory {

    private final Logger LOGGER = LoggerFactory.getLogger(RedisDLockFactory.class);

    /**
     * Creates a new {@link JedisPool}, with default database and timeout.
     * 
     * @param hostAndPort
     * @param password
     * @return
     */
    public static JedisPool newJedisPool(String hostAndPort, String password) {
        return newJedisPool(hostAndPort, password, Protocol.DEFAULT_DATABASE,
                Protocol.DEFAULT_TIMEOUT);
    }

    /**
     * Creates a new {@link JedisPool}, with specified database and default
     * timeout.
     * 
     * @param hostAndPort
     * @param password
     * @param db
     * @return
     */
    public static JedisPool newJedisPool(String hostAndPort, String password, int db) {
        return newJedisPool(hostAndPort, password, db, Protocol.DEFAULT_TIMEOUT);
    }

    /**
     * Creates a new {@link JedisPool} with default database and specified
     * timeout.
     * 
     * @param hostAndPort
     * @param password
     * @param timeoutMs
     * @return
     */
    public static JedisPool newJedisPool(String hostAndPort, String password, long timeoutMs) {
        return newJedisPool(hostAndPort, password, Protocol.DEFAULT_DATABASE, timeoutMs);
    }

    /**
     * Creates a new {@link JedisPool}.
     * 
     * @param hostAndPort
     * @param password
     * @param db
     * @param timeoutMs
     * @return
     */
    public static JedisPool newJedisPool(String hostAndPort, String password, int db,
            long timeoutMs) {
        final int maxTotal = Runtime.getRuntime().availableProcessors();
        final int maxIdle = maxTotal / 2;

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMinIdle(1);
        poolConfig.setMaxIdle(maxIdle > 0 ? maxIdle : 1);
        poolConfig.setMaxWaitMillis(timeoutMs + 1000);
        // poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);

        String[] tokens = hostAndPort.split(":");
        String host = tokens.length > 0 ? tokens[0] : Protocol.DEFAULT_HOST;
        int port = tokens.length > 1 ? Integer.parseInt(tokens[1]) : Protocol.DEFAULT_PORT;
        JedisPool jedisPool = new JedisPool(poolConfig, host, port, (int) timeoutMs, password, db);
        return jedisPool;
    }

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
     * Sets Redis' host and port scheme (format {@code host:port}).
     * 
     * @param redisHostAndPort
     * @return
     */
    public RedisDLockFactory setRedisHostAndPort(String redisHostAndPort) {
        this.redisHostAndPort = redisHostAndPort;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RedisDLockFactory init() {
        super.init();

        if (getJedisConnector() == null) {
            try {
                JedisConnector jedisConnector = new JedisConnector();
                jedisConnector.setRedisHostsAndPorts(redisHostAndPort)
                        .setRedisPassword(getRedisPassword()).init();
                setJedisConnector(jedisConnector);
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RedisDLock createLockInternal(String name, Properties lockProps) {
        RedisDLock lock = new RedisDLock(name);
        lock.setLockProperties(lockProps);
        lock.setRedisHostAndPort(redisHostAndPort).setRedisPassword(getRedisPassword());
        lock.setJedisConnector(getJedisConnector());
        return lock;
    }

}

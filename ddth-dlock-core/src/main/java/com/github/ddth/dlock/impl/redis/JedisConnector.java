package com.github.ddth.dlock.impl.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.dlock.DLockException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

/**
 * Manage connection to Redis server.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class JedisConnector implements AutoCloseable {

    private final Logger LOGGER = LoggerFactory.getLogger(JedisConnector.class);

    private String redisHostsAndPorts = Protocol.DEFAULT_HOST + ":" + Protocol.DEFAULT_PORT;
    private String redisPassword;

    private JedisPool jedisPool;
    private JedisCluster jedisCluster;

    /**
     * Get Redis' hosts and ports scheme (format
     * {@code host1:port1,host2:port2}).
     * 
     * @return
     */
    public String getRedisHostsAndPorts() {
        return redisHostsAndPorts;
    }

    /**
     * Set Redis' hosts and ports scheme (format
     * {@code host1:port1,host2:port2}).
     * 
     * @param redisHostsAndPorts
     * @return
     */
    public JedisConnector setRedisHostsAndPorts(String redisHostsAndPorts) {
        this.redisHostsAndPorts = redisHostsAndPorts;
        return this;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public JedisConnector setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
        return this;
    }

    synchronized private void connectJedisPool() throws DLockException {
        if (jedisPool == null) {
            String[] tokens = redisHostsAndPorts.split("[,;\\s]+");
            jedisPool = RedisDLockFactory.newJedisPool(tokens[0], getRedisPassword());
        }
    }

    public Jedis getJedis() {
        if (jedisPool == null) {
            connectJedisPool();
        }
        return jedisPool.getResource();
    }

    synchronized private void connectJedisCluster() throws DLockException {
        if (jedisCluster == null) {
            jedisCluster = ClusteredRedisDLockFactory.newJedisCluster(redisHostsAndPorts,
                    getRedisPassword());
        }
    }

    public JedisCluster getJedisCluster() {
        if (jedisCluster == null) {
            connectJedisCluster();
        }
        return jedisCluster;
    }

    public JedisConnector init() {
        return this;
    }

    public void destroy() {
        if (jedisPool != null) {
            try {
                jedisPool.close();
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            } finally {
                jedisPool = null;
            }
        }

        if (jedisCluster != null) {
            try {
                jedisCluster.close();
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            } finally {
                jedisCluster = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        destroy();
    }

}

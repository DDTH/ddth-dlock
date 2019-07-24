package com.github.ddth.dlock.impl.redis;

import com.github.ddth.commons.redis.JedisConnector;
import com.github.ddth.commons.redis.JedisUtils;
import com.github.ddth.dlock.IDLockFactory;
import redis.clients.jedis.Protocol;

import java.util.Properties;

/**
 * Clustered <a href="http://redis.io">Redis</a> implementation of
 * {@link IDLockFactory} that creates {@link ClusteredRedisDLock} objects.
 *
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class ClusteredRedisDLockFactory extends BaseRedisDLockFactory {
    private String redisHostsAndPorts = Protocol.DEFAULT_HOST + ":" + Protocol.DEFAULT_PORT;

    /**
     * Redis' hosts and ports scheme (format
     * {@code host1:port1,host2:port2,host3:port3}).
     *
     * @return
     */
    public String getRedisHostsAndPorts() {
        return redisHostsAndPorts;
    }

    /**
     * Redis' hosts and ports scheme (format
     * {@code host1:port1,host2:port2,host3:port3}).
     *
     * @param redisHostsAndPorts
     * @return
     */
    public ClusteredRedisDLockFactory setRedisHostsAndPorts(String redisHostsAndPorts) {
        this.redisHostsAndPorts = redisHostsAndPorts;
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
                .setRedisHostsAndPorts(getRedisHostsAndPorts()).setRedisPassword(getRedisPassword()).init();
        return jedisConnector;
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.1.2
     */
    @Override
    public ClusteredRedisDLock createLock(String name) {
        return (ClusteredRedisDLock) super.createLock(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ClusteredRedisDLock createLockInternal(String name, Properties lockProps) {
        ClusteredRedisDLock lock = new ClusteredRedisDLock(name);
        lock.setLockProperties(lockProps);
        lock.setRedisHostsAndPorts(getRedisHostsAndPorts()).setRedisPassword(getRedisPassword());
        lock.setJedisConnector(getJedisConnector());
        return lock;
    }
}

package com.github.ddth.dlock.impl.redis;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.github.ddth.commons.redis.JedisConnector;
import com.github.ddth.commons.redis.JedisUtils;
import com.github.ddth.dlock.IDLockFactory;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * Clustered <a href="http://redis.io">Redis</a> implementation of
 * {@link IDLockFactory} that creates {@link ClusteredRedisDLock} objects.
 * 
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class ClusteredRedisDLockFactory extends BaseRedisDLockFactory {

    public final static int DEFAULT_MAX_ATTEMPTS = 3;

    /**
     * Creates a new {@link JedisCluster}, with default timeout.
     * 
     * @param hostsAndPorts
     *            format {@code host1:port1,host2:port2...}
     * @param password
     * @return
     */
    public static JedisCluster newJedisCluster(String hostsAndPorts, String password) {
        return newJedisCluster(hostsAndPorts, password, Protocol.DEFAULT_TIMEOUT);
    }

    /**
     * Creates a new {@link JedisCluster}.
     * 
     * @param hostsAndPorts
     *            format {@code host1:port1,host2:port2...}
     * @param password
     * @param timeoutMs
     * @return
     */
    public static JedisCluster newJedisCluster(String hostsAndPorts, String password,
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

        Set<HostAndPort> clusterNodes = new HashSet<>();
        String[] hapList = hostsAndPorts.split("[,;\\s]+");
        for (String hostAndPort : hapList) {
            String[] tokens = hostAndPort.split(":");
            String host = tokens.length > 0 ? tokens[0] : Protocol.DEFAULT_HOST;
            int port = tokens.length > 1 ? Integer.parseInt(tokens[1]) : Protocol.DEFAULT_PORT;
            clusterNodes.add(new HostAndPort(host, port));
        }

        JedisCluster jedisCluster = new JedisCluster(clusterNodes, (int) timeoutMs, (int) timeoutMs,
                DEFAULT_MAX_ATTEMPTS, password, poolConfig);
        return jedisCluster;
    }

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
                .setRedisHostsAndPorts(redisHostsAndPorts).init();
        return jedisConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ClusteredRedisDLock createLockInternal(String name, Properties lockProps) {
        ClusteredRedisDLock lock = new ClusteredRedisDLock(name);
        lock.setLockProperties(lockProps);
        lock.setRedisHostsAndPorts(redisHostsAndPorts).setRedisPassword(getRedisPassword());
        lock.setJedisConnector(getJedisConnector());
        return lock;
    }

}

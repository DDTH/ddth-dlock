package com.github.ddth.dlock.impl.redis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.dlock.LockResult;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Protocol;

/**
 * Clustered <a href="http://redis.io">Redis</a> implementation of
 * {@link ICache}.
 * 
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class ClusteredRedisDLock extends BaseRedisDLock {

    private final Logger LOGGER = LoggerFactory.getLogger(ClusteredRedisDLock.class);

    /**
     * To override the {@link #setRedisHostsAndPorts(String)} setting.
     */
    public final static String LOCK_PROP_REDIS_HOSTS_AND_PORTS = "dlock.redis_hosts_and_ports";

    private String redisHostsAndPorts = Protocol.DEFAULT_HOST + ":" + Protocol.DEFAULT_PORT;

    public ClusteredRedisDLock(String name) {
        super(name);
    }

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
     * Sets Redis' hosts and ports scheme (format
     * {@code host1:port1,host2:port2,host3:port3}).
     * 
     * @param redisHostsAndPorts
     * @return
     */
    public ClusteredRedisDLock setRedisHostsAndPorts(String redisHostsAndPorts) {
        this.redisHostsAndPorts = redisHostsAndPorts;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClusteredRedisDLock init() {
        super.init();

        /*
         * Parse custom property: redis-hosts-and-ports
         */
        String hostsAndPorts = getLockProperty(LOCK_PROP_REDIS_HOSTS_AND_PORTS);
        if (!StringUtils.isBlank(hostsAndPorts)) {
            this.redisHostsAndPorts = hostsAndPorts;
        }

        if (getJedisConnector() == null) {
            try {
                JedisConnector jedisConnector = new JedisConnector();
                jedisConnector.setRedisHostsAndPorts(redisHostsAndPorts).init();
                setJedisConnector(jedisConnector);
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }

        return this;
    }

    /**
     * @return
     */
    protected JedisCluster getJedis() {
        return getJedisConnector().getJedisCluster();
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

        JedisCluster jedis = getJedis();
        String key = getName();
        Object response = jedis.eval(getScriptLock(), 0, key, clientId,
                String.valueOf(lockDurationMs));
        if (response == null) {
            return LockResult.HOLD_BY_ANOTHER_CLIENT;
        } else {
            return LockResult.SUCCESSFUL;
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

        JedisCluster jedis = getJedis();
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

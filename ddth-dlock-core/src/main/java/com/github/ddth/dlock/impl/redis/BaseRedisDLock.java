package com.github.ddth.dlock.impl.redis;

import com.github.ddth.commons.redis.JedisConnector;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.AbstractDLock;
import com.github.ddth.dlock.impl.redis.internal.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.commands.JedisClusterCommands;
import redis.clients.jedis.commands.JedisCommands;

import java.util.function.Function;

/**
 * Base class for <a href="http://redis.io">Redis</a>-based implementations of
 * {@link IDLock}.
 *
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class BaseRedisDLock extends AbstractDLock {
    private final Logger LOGGER = LoggerFactory.getLogger(BaseRedisDLock.class);

    /**
     * To override the {@link #setRedisPassword(String)} setting.
     */
    public final static String LOCK_PROP_REDIS_PASSWORD = "dlock.redis_password";

    private String scriptLock, scriptUnlock;

    private JedisConnector jedisConnector;
    /**
     * Flag to mark if the Redis resource (e.g. Redis client pool) is created
     * and handled by the lock instance.
     */
    protected boolean myOwnRedis = true;
    private String redisPassword;

    public BaseRedisDLock(String name) {
        super(name);
    }

    /**
     * @return
     */
    public JedisConnector getJedisConnector() {
        return jedisConnector;
    }

    /**
     * @param jedisConnector
     * @return
     */
    public BaseRedisDLock setJedisConnector(JedisConnector jedisConnector) {
        if (myOwnRedis && this.jedisConnector != null) {
            this.jedisConnector.destroy();
        }
        this.jedisConnector = jedisConnector;
        myOwnRedis = false;
        return this;
    }

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
    public BaseRedisDLock setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
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
     */
    @Override
    public BaseRedisDLock init() {
        super.init();

        /*
         * Parse custom property: redis-password
         */
        String password = getLockProperty(LOCK_PROP_REDIS_PASSWORD);
        if (!StringUtils.isBlank(password)) {
            this.redisPassword = password;
        }

        if (jedisConnector == null) {
            jedisConnector = buildJedisConnector();
            myOwnRedis = jedisConnector != null;
        }

        /*
         * Implementation: get the current client-id value; if not null
         * AND not equal to the supplied-client-id then return nil (a),
         * otherwise write the supplied-client-id (b).
         *
         * Parameters: (1) key name, (2) supplied-client-id, (3) TTL in
         * milliseconds.
         *
         * Return: (a) nil if lock is currently hold by another client,
         * (b) non-nil if successful.
         */
        scriptLock = "local cval=redis.call(\"get\", ARGV[1]);" + " if cval and cval~=ARGV[2] then return nil"
                + " else return redis.call(\"set\", ARGV[1], ARGV[2], \"PX\", ARGV[3]); end";

        /*
         * Implementation: get the current client-id value; if not null
         * AND not equal to the supplied-client-id then return nil (a), otherwise
         * delete the key (b).
         *
         * Parameters: (1) key name, (2) supplied-client-id.
         *
         * Return: (a) nil if lock is currently hold by another client,
         * (b) "1" if successful, (3) "0" if lock is not found
         */
        scriptUnlock = "local cval=redis.call(\"get\", ARGV[1]);" + " if cval and cval~=ARGV[2] then return nil"
                + " else return redis.call(\"del\", ARGV[1]); end";

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

    protected String getScriptLock() {
        return scriptLock;
    }

    protected BaseRedisDLock setScriptLock(String scriptLock) {
        this.scriptLock = scriptLock;
        return this;
    }

    protected String getScriptUnlock() {
        return scriptUnlock;
    }

    protected BaseRedisDLock setScriptUnlock(String scriptUnlock) {
        this.scriptUnlock = scriptUnlock;
        return this;
    }

    private void doUpdateLockHolder(Function<String, String> funcGetClientIdFromRedis,
            Function<String, Long> funcGetTtlFromRedis) {
        String key = getName();
        String clientId = funcGetClientIdFromRedis.apply(key);
        setClientId(clientId);
        if (!StringUtils.isBlank(clientId)) {
            Long ttl = funcGetTtlFromRedis.apply(key);
            if (ttl != null && ttl.longValue() != -2) {
                setTimestampExpiry(
                        ttl.longValue() != -1 ? System.currentTimeMillis() + ttl.longValue() : Integer.MAX_VALUE);
            } else {
                setTimestampExpiry(Integer.MAX_VALUE);
            }
        } else {
            setTimestampExpiry(0);
        }
    }

    /**
     * Update current lock's holder info.
     *
     * @param jedisClusterCommands
     * @since 1.0.0
     */
    protected void updateLockHolder(JedisClusterCommands jedisClusterCommands) {
        doUpdateLockHolder(jedisClusterCommands::get, jedisClusterCommands::pttl);
    }

    /**
     * Update current lock's holder info.
     *
     * @param jedisCommands
     * @since 0.1.1
     */
    protected void updateLockHolder(JedisCommands jedisCommands) {
        doUpdateLockHolder(jedisCommands::get, jedisCommands::pttl);
    }

    /**
     * Get name of the ZSET to store clientId's score.
     *
     * @return
     * @since 0.1.2
     */
    protected String getZsetName() {
        return getName() + "-z";
    }

    /**
     * Convenient method to build result for {@link #unlock(String)}} method with result from Redis server.
     *
     * @param response
     * @return
     * @since 1.0.0
     */
    protected LockResult unlockResult(Object response) {
        if (response == null) {
            return LockResult.HOLD_BY_ANOTHER_CLIENT;
        } else if ("0".equals(response.toString())) {
            return LockResult.NOT_FOUND;
        } else {
            return LockResult.SUCCESSFUL;
        }
    }
}

package com.github.ddth.dlock.test.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;

import com.github.ddth.dlock.impl.redis.RedisDLockFactory;
import com.github.ddth.dlock.test.BaseDLockTCase;

import redis.clients.jedis.Jedis;

/**
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class RedisDLockTCase extends BaseDLockTCase {

    protected static class MyRedisDLockFactory extends RedisDLockFactory {
        public MyRedisDLockFactory init() {
            super.init();

            try (Jedis jedis = getJedisConnector().getJedis()) {
                jedis.flushAll();
            }

            return this;
        }
    }

    @Before
    public void setUp() {
        Map<String, Properties> lockProperties = new HashMap<>();

        MyRedisDLockFactory factory = new MyRedisDLockFactory();
        factory.setRedisHostAndPort("localhost:6379");
        factory.setLockProperties(lockProperties);
        factory.init();
        lockFactory = factory;
    }

}

package com.github.ddth.dlock.test.clusteredredis;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;

import com.github.ddth.dlock.impl.redis.ClusteredRedisDLockFactory;
import com.github.ddth.dlock.test.BaseDLockTCase;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class ClusteredRedisDLockTCase extends BaseDLockTCase {

    protected static class MyClusteredRedisDLockFactory extends ClusteredRedisDLockFactory {
        public MyClusteredRedisDLockFactory init() {
            super.init();

            JedisCluster jedis = getJedisConnector().getJedisCluster();
            jedis.getClusterNodes().forEach((name, pool) -> {
                try (Jedis node = pool.getResource()) {
                    node.flushAll();
                }
            });

            return this;
        }
    }

    @Before
    public void setUp() {
        Map<String, Properties> lockProperties = new HashMap<>();

        MyClusteredRedisDLockFactory factory = new MyClusteredRedisDLockFactory();
        factory.setRedisHostsAndPorts("localhost:7000");
        factory.setLockProperties(lockProperties);
        factory.init();
        lockFactory = factory;
    }

}

package com.github.ddth.dlock.test.clusteredredis;

import com.github.ddth.dlock.impl.redis.ClusteredRedisDLockFactory;
import com.github.ddth.dlock.test.BaseDLockTCase;
import org.junit.Before;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class ClusteredRedisDLockTCase extends BaseDLockTCase {

    protected static class MyClusteredRedisDLockFactory extends ClusteredRedisDLockFactory {
        private boolean isMaster(String name, Jedis node) {
            for (String nodeInfo : node.clusterNodes().split("\n")) {
                if (nodeInfo.contains(name)) {
                    return nodeInfo.contains("master");
                }
            }
            return false;
        }

        public MyClusteredRedisDLockFactory init() {
            super.init();

            JedisCluster jedis = getJedisConnector().getJedisCluster();
            jedis.getClusterNodes().forEach((name, pool) -> {
                try (Jedis node = pool.getResource()) {
                    if (isMaster(name, node)) {
                        node.flushAll();
                    }
                }
            });

            return this;
        }
    }

    @Before
    public void setUp() {
        Map<String, Properties> lockProperties = new HashMap<>();

        MyClusteredRedisDLockFactory factory = new MyClusteredRedisDLockFactory();
        factory.setRedisHostsAndPorts(
                "localhost:7000,localhost:7001,localhost:7002,localhost:7003,localhost:7004,localhost:7005");
        factory.setLockProperties(lockProperties);
        factory.init();
        lockFactory = factory;
    }

}

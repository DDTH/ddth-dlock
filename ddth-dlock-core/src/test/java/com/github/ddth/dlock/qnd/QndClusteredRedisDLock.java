package com.github.ddth.dlock.qnd;

import com.github.ddth.dlock.impl.redis.ClusteredRedisDLock;

public class QndClusteredRedisDLock {

    public static void main(String[] args) throws Exception {
        try (ClusteredRedisDLock lock = new ClusteredRedisDLock("demo")) {
            lock.setRedisHostsAndPorts("localhost:7000");
            lock.init();
            String client1 = "client-" + System.currentTimeMillis();
            String client2 = "client-" + (System.currentTimeMillis() + 1);

            System.out.println(lock.lock(client1, 3600000));
            System.out.println(lock);

            Thread.sleep(1000);
            System.out.println(lock.lock(client1, 3600000));
            System.out.println(lock);

            Thread.sleep(1000);
            System.out.println(lock.lock(client2));
            System.out.println(lock);
        }
    }

}

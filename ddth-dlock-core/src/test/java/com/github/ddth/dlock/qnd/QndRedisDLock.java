package com.github.ddth.dlock.qnd;

import com.github.ddth.dlock.impl.redis.RedisDLock;

public class QndRedisDLock {

    public static void main(String[] args) throws Exception {
        try (RedisDLock lock = new RedisDLock("demo")) {
            lock.init();
            String client1 = "client-" + System.currentTimeMillis();
            String client2 = "client-" + (System.currentTimeMillis() + 1);

            System.out.println(lock.lock(client1, 30000));
            System.out.println(lock);

            Thread.sleep(1000);
            System.out.println(lock.lock(client1, 30000));
            System.out.println(lock);

            Thread.sleep(1000);
            System.out.println(lock.lock(client2));
            System.out.println(lock);
        }
    }

}

package com.github.ddth.dlock.qnd;

import com.github.ddth.dlock.impl.redis.RedisDLock;

public class QndRedisDLock {

    public static void main(String[] args) throws Exception {
        try (RedisDLock lock = new RedisDLock("demo")) {
            lock.init();
            System.out.println(lock.lock("client-1", 2000));
            Thread.sleep(3000);
            System.out.println(lock.unlock("client-2"));
        }
    }

}

package com.github.ddth.dlock.qnd;

import com.github.ddth.dlock.impl.redis.RedisDLock;

public class QndRedisDLock {
    static class MyRedisDLock extends RedisDLock {
        public MyRedisDLock(String name) {
            super(name);
        }

        public void flush() {
            getJedis().flushAll();
        }
    }

    public static void main(String[] args) throws Exception {
        try (MyRedisDLock lock = new MyRedisDLock("demo")) {
            lock.init();
            lock.flush();

            String client1 = "client-1-" + System.currentTimeMillis();
            String client2 = "client-2-" + (System.currentTimeMillis() + 1);

            System.out.println("Lock result: " + lock.lock(client1, 30000) + " / " + lock);

            Thread.sleep(1000);
            System.out.println("Lock result: " + lock.lock(client1, 30000) + " / " + lock);

            Thread.sleep(1000);
            System.out.println("Lock result: " + lock.lock(client2) + " / " + lock);
        }
    }
}

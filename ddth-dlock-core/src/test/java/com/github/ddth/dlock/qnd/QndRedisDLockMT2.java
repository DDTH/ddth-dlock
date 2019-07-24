package com.github.ddth.dlock.qnd;

import com.github.ddth.commons.utils.IdGenerator;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.redis.RedisDLock;

import java.util.Random;

public class QndRedisDLockMT2 {
    static Random RAND = new Random(System.currentTimeMillis());

    static class MyRedisDLock extends RedisDLock {
        public MyRedisDLock(String name) {
            super(name);
        }

        public MyRedisDLock flush() {
            getJedis().flushAll();
            return this;
        }
    }

    static class MyThread extends Thread {
        private String name;
        private IDLock dlock;

        public MyThread(String name, IDLock dlock) {
            this.name = name;
            this.dlock = dlock;
        }

        public void run() {
            IdGenerator idGen = IdGenerator.getInstance(RAND.nextInt());
            String clientId = idGen.generateId64Ascii();
            while (true) {
                long lockDurationMs = RAND.nextInt(1357) + 1;
                LockResult result = dlock.lock(clientId, lockDurationMs);
                if (result == LockResult.SUCCESSFUL) {
                    System.out.println(name + ": " + result + " / " + clientId + " / " + lockDurationMs);
                    try {
                        Thread.sleep(RAND.nextInt(1000));
                    } catch (InterruptedException e) {
                    }
                    dlock.unlock(clientId);
                } else {
                    System.out.println(name + ": " + result + " / " + clientId);
                }
                try {
                    Thread.sleep(RAND.nextInt(1000));
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static void main(String[] args) {
        String lockName = "demo";
        String redisHostAndPort = "localhost:6379";
        RedisDLock lock1 = new MyRedisDLock(lockName).setRedisHostAndPort(redisHostAndPort).init();
        RedisDLock lock2 = new MyRedisDLock(lockName).setRedisHostAndPort(redisHostAndPort).init();
        ((MyRedisDLock) lock1).flush();
        Thread t1 = new MyThread("Worker1", lock1);
        Thread t2 = new MyThread("Worker2", lock2);
        t1.start();
        t2.start();
    }
}

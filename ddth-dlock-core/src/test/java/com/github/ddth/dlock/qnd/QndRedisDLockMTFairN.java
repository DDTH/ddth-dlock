package com.github.ddth.dlock.qnd;

import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.redis.RedisDLock;
import com.google.common.util.concurrent.AtomicDoubleArray;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public class QndRedisDLockMTFairN {
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
        private IDLock dlock;
        private AtomicLong counter;
        private AtomicLongArray hit;
        private int threadIndex;

        public MyThread(int threadIndex, IDLock dlock, AtomicLongArray hit, AtomicLong counter) {
            super("Thread-" + threadIndex);
            this.threadIndex = threadIndex;
            this.dlock = dlock;
            this.counter = counter;
            this.hit = hit;
        }

        public void run() {
            String threadName = getName();
            int waitWeight = 0;
            while (!isInterrupted()) {
                long sleepTimeMs = RAND.nextInt(1000) + 1;
                LockResult result = dlock.lock(waitWeight, threadName, RAND.nextInt(10000) + 1);
                if (result == LockResult.SUCCESSFUL) {
                    hit.incrementAndGet(threadIndex);
                    long numTotal = counter.incrementAndGet();
                    AtomicDoubleArray rate = new AtomicDoubleArray(hit.length());
                    for (int i = 0; i < hit.length(); i++) {
                        double d = hit.get(i) * 100.0 / numTotal;
                        rate.set(i, Math.round(d * 10.0) / 10.0);
                    }
                    System.out.println(
                            getName() + ": I got the lock / Total: " + numTotal + " / Hits: " + hit + "  / Rates: "
                                    + rate);
                    waitWeight = 0; //reset wait weight
                } else {
                    //counter.incrementAndGet();
                    //waitWeight += sleepTimeMs;
                    waitWeight++;
                }
                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException e) {
                }
                if (result == LockResult.SUCCESSFUL) {
                    dlock.unlock(threadName);
                }
            }
        }
    }

    public static void main(String[] args) {
        final int numThreads = 4;

        final IDLock[] LOCKS = new IDLock[numThreads];
        String lockName = "demo";
        String redisHostAndPort = "localhost:6379";
        for (int i = 0; i < numThreads; i++) {
            RedisDLock lock = new MyRedisDLock(lockName).setRedisHostAndPort(redisHostAndPort).init();
            ((MyRedisDLock) lock).flush();
            LOCKS[i] = lock;
        }

        final AtomicLongArray hit = new AtomicLongArray(numThreads);
        final AtomicLong counter = new AtomicLong();
        final Thread[] THREADS = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            THREADS[i] = new MyThread(i, LOCKS[i], hit, counter);
        }
        for (int i = 0; i < numThreads; i++) {
            THREADS[i].start();
        }
    }
}

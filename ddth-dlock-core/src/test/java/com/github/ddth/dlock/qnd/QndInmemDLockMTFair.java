package com.github.ddth.dlock.qnd;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.inmem.InmemDLock;
import com.google.common.util.concurrent.AtomicDoubleArray;

public class QndInmemDLockMTFair {

    static class MyThread extends Thread {
        private IDLock dlock;
        private AtomicLong counter = new AtomicLong();
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
                LockResult result = dlock.lock(waitWeight, threadName, 10000);
                if (result == LockResult.SUCCESSFUL) {
                    hit.incrementAndGet(threadIndex);
                    long numTotal = counter.incrementAndGet();
                    AtomicDoubleArray rate = new AtomicDoubleArray(hit.length());
                    for (int i = 0; i < hit.length(); i++) {
                        double d = hit.get(i) * 100.0 / numTotal;
                        rate.set(i, Math.round(d * 10.0) / 10.0);
                    }
                    System.out.println(getName() + ": I got the lock! [" + threadName + "] / "
                            + numTotal + " / " + hit + "  / " + rate);
                    waitWeight = 0;
                } else {
                    counter.incrementAndGet();
                    waitWeight++;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                if (result == LockResult.SUCCESSFUL) {
                    dlock.unlock(threadName);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final int numThreads = 4;
        final InmemDLock lock = new InmemDLock("demo");
        lock.init();
        final AtomicLongArray hit = new AtomicLongArray(numThreads);
        final AtomicLong counter = new AtomicLong();
        Thread[] THREADS = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            THREADS[i] = new MyThread(i, lock, hit, counter);
        }
        for (int i = 0; i < numThreads; i++) {
            THREADS[i].start();
        }
    }

}

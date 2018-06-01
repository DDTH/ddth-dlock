package com.github.ddth.dlock.qnd;

import java.util.Random;

import com.github.ddth.commons.utils.IdGenerator;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.redis.RedisDLock;

public class QndRedisDLockMT {

    static Random RAND = new Random(System.currentTimeMillis());

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
                LockResult result = dlock.lock(clientId, 1000);
                if (result == LockResult.SUCCESSFUL) {
                    System.out.println(name + ": " + idGen + "\t" + clientId);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    dlock.unlock(clientId);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        RedisDLock lock1 = new RedisDLock("demo").init();
        RedisDLock lock2 = new RedisDLock("demo").init();
        Thread t1 = new MyThread("Worker1", lock1);
        Thread t2 = new MyThread("Worker2", lock2);
        t1.start();
        t2.start();
    }

}

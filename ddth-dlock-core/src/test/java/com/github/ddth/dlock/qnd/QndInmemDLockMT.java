package com.github.ddth.dlock.qnd;

import com.github.ddth.commons.utils.IdGenerator;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.inmem.InmemDLock;

import java.util.Random;

public class QndInmemDLockMT {
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
        IDLock lock = new InmemDLock(lockName).init();
        Thread t1 = new MyThread("Worker1", lock);
        Thread t2 = new MyThread("Worker2", lock);
        t1.start();
        t2.start();
    }
}

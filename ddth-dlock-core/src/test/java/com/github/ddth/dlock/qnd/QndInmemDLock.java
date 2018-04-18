package com.github.ddth.dlock.qnd;

import com.github.ddth.dlock.impl.inmem.InmemDLock;

public class QndInmemDLock {

    public static void main(String[] args) throws Exception {
        try (InmemDLock lock = new InmemDLock("demo")) {
            lock.init();

            System.out.println(lock.lock("client-1", 5000));
            System.out.println(lock);

            Thread.sleep(1000);
            System.out.println(lock.lock("client-1", 5000));
            System.out.println(lock);

            Thread.sleep(1000);
            System.out.println(lock.lock("client-2"));
            System.out.println(lock);
        }
    }

}

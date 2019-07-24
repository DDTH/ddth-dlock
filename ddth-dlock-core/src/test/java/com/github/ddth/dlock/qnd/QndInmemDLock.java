package com.github.ddth.dlock.qnd;

import com.github.ddth.dlock.impl.inmem.InmemDLock;

public class QndInmemDLock {
    public static void main(String[] args) throws Exception {
        try (InmemDLock lock = new InmemDLock("demo")) {
            lock.init();

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

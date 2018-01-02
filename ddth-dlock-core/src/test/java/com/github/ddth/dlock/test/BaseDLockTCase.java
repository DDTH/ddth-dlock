package com.github.ddth.dlock.test;

import static org.junit.Assert.assertEquals;

import org.junit.After;

import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.IDLockFactory;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.AbstractDLockFactory;

/**
 * Base class for DLock test cases.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 */
public abstract class BaseDLockTCase {

    @After
    public void tearDown() {
        ((AbstractDLockFactory) lockFactory).destroy();
    }

    protected static IDLockFactory lockFactory;

    @org.junit.Test
    public void testLockUnlock() {
        IDLock lock = lockFactory.createLock("my-lock");
        try {
            assertEquals(LockResult.SUCCESSFUL, lock.lock("my-client-id"));
        } finally {
            assertEquals(LockResult.SUCCESSFUL, lock.unlock("my-client-id"));
        }
    }

    @org.junit.Test
    public void testLockUnlockAnother() {
        IDLock lock = lockFactory.createLock("my-lock");
        try {
            assertEquals(LockResult.SUCCESSFUL, lock.lock("my-client-id-1"));
        } finally {
            assertEquals(LockResult.HOLD_BY_ANOTHER_CLIENT, lock.unlock("my-client-id-2"));
        }
    }

    @org.junit.Test
    public void testMultipleLocksUnlocks() {
        IDLock lock = lockFactory.createLock("my-lock");
        try {
            assertEquals(LockResult.SUCCESSFUL, lock.lock("my-client-id"));
            try {
                assertEquals(LockResult.SUCCESSFUL, lock.lock("my-client-id"));
            } finally {
                assertEquals(LockResult.SUCCESSFUL, lock.unlock("my-client-id"));
            }
        } finally {
            assertEquals(LockResult.NOT_FOUND, lock.unlock("my-client-id"));
        }
    }

    @org.junit.Test
    public void testLockDiffClients() {
        IDLock lock = lockFactory.createLock("my-lock");
        try {
            assertEquals(LockResult.SUCCESSFUL, lock.lock("my-client-id-1"));
            try {
                assertEquals(LockResult.HOLD_BY_ANOTHER_CLIENT, lock.lock("my-client-id-2"));
            } finally {
                assertEquals(LockResult.HOLD_BY_ANOTHER_CLIENT, lock.unlock("my-client-id-2"));
            }
        } finally {
            assertEquals(LockResult.SUCCESSFUL, lock.unlock("my-client-id-1"));
        }
    }

    @org.junit.Test
    public void testLockDiffClientsTimeout() throws Exception {
        IDLock lock = lockFactory.createLock("my-lock");
        try {
            assertEquals(LockResult.SUCCESSFUL, lock.lock("my-client-id-1", 2000));
            Thread.sleep(3000);
            try {
                assertEquals(LockResult.SUCCESSFUL, lock.lock("my-client-id-2", 2000));
            } finally {
                assertEquals(LockResult.SUCCESSFUL, lock.unlock("my-client-id-2"));
            }
        } finally {
            assertEquals(LockResult.NOT_FOUND, lock.unlock("my-client-id-1"));
        }
    }

}

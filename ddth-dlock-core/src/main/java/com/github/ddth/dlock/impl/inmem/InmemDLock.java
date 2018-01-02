package com.github.ddth.dlock.impl.inmem;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.dlock.DLockException;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.AbstractDLock;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * In-memory implementation of {@link IDLock}.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class InmemDLock extends AbstractDLock {

    private Cache<String, LockEntry> locks = CacheBuilder.newBuilder()
            .expireAfterAccess(24 * 3600, TimeUnit.SECONDS).build();

    private static class LockEntry {
        private String clientId;
        // private long timestampCreated = System.currentTimeMillis();
        // private long ttlMs;
        private long timestampExpiry;

        public LockEntry(String clientId, long ttlMs) {
            this.clientId = clientId;
            setTtl(ttlMs);
        }

        public String getClientId() {
            return clientId;
        }

        public LockEntry setTtl(long ttlMs) {
            // this.ttlMs = ttlMs;
            this.timestampExpiry = System.currentTimeMillis() + ttlMs;
            return this;
        }

        public boolean isExpired() {
            return timestampExpiry < System.currentTimeMillis();
        }
    }

    public InmemDLock(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // TODO

        super.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LockResult lock(String clientId, long lockDurationMs) {
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("Invalid ClientID!");
        }
        if (lockDurationMs <= 0) {
            throw new IllegalArgumentException("Lock duration must be greater than zero!");
        }

        synchronized (this) {
            String key = getName();
            try {
                LockEntry lockEntry = locks.getIfPresent(key);
                if (lockEntry != null && !lockEntry.isExpired()
                        && !StringUtils.equals(lockEntry.getClientId(), clientId)) {
                    return LockResult.HOLD_BY_ANOTHER_CLIENT;
                }
                locks.put(key, new LockEntry(clientId, lockDurationMs));
                return LockResult.SUCCESSFUL;
            } catch (Exception e) {
                throw e instanceof DLockException ? (DLockException) e : new DLockException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LockResult unlock(String clientId) {
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("Invalid ClientID!");
        }
        synchronized (this) {
            String key = getName();
            try {
                LockEntry lockEntry = locks.getIfPresent(key);
                if (lockEntry == null) {
                    return LockResult.NOT_FOUND;
                }
                if (!lockEntry.isExpired()
                        && !StringUtils.equals(lockEntry.getClientId(), clientId)) {
                    return LockResult.HOLD_BY_ANOTHER_CLIENT;
                }
                locks.invalidate(key);
                return LockResult.SUCCESSFUL;
            } catch (Exception e) {
                throw e instanceof DLockException ? (DLockException) e : new DLockException(e);
            }
        }
    }

}

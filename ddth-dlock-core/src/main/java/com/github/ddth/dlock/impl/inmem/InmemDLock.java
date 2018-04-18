package com.github.ddth.dlock.impl.inmem;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.dlock.DLockException;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.AbstractDLock;

/**
 * In-memory implementation of {@link IDLock}.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class InmemDLock extends AbstractDLock {

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
            try {
                if (!StringUtils.isBlank(getClientId())
                        && getTimestampExpiry() >= System.currentTimeMillis()
                        && !StringUtils.equals(getClientId(), clientId)) {
                    return LockResult.HOLD_BY_ANOTHER_CLIENT;
                }
                updateLockHolder(clientId, lockDurationMs);
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
            try {
                if (StringUtils.isBlank(getClientId())) {
                    return LockResult.NOT_FOUND;
                }
                if (getTimestampExpiry() >= System.currentTimeMillis()
                        && !StringUtils.equals(getClientId(), clientId)) {
                    return LockResult.HOLD_BY_ANOTHER_CLIENT;
                }
                updateLockHolder(null, 0);
                return LockResult.SUCCESSFUL;
            } catch (Exception e) {
                throw e instanceof DLockException ? (DLockException) e : new DLockException(e);
            }
        }
    }

}

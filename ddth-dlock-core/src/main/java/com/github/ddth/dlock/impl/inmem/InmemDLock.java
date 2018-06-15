package com.github.ddth.dlock.impl.inmem;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

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

    private static class Token {

        public final String clientId;
        public final int waitWeight;

        public Token(String clientId, int waitWeight) {
            this.clientId = clientId;
            this.waitWeight = waitWeight;
        }
    }

    private Queue<Token> pQueue = new PriorityQueue<Token>(new Comparator<Token>() {
        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(Token t1, Token t2) {
            return t2.waitWeight - t1.waitWeight;
        }
    });

    public InmemDLock(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LockResult lock(int waitWeight, String clientId, long lockDurationMs) {
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("Invalid ClientID!");
        }
        if (lockDurationMs <= 0) {
            throw new IllegalArgumentException("Lock duration must be greater than zero!");
        }

        if (waitWeight >= 0) {
            synchronized (pQueue) {
                pQueue.add(new Token(clientId, waitWeight));
            }
        }
        synchronized (this) {
            try {
                if (waitWeight >= 0) {
                    Token token = pQueue.peek();
                    if (token != null && !StringUtils.equals(token.clientId, clientId)) {
                        return LockResult.HOLD_BY_ANOTHER_CLIENT;
                    }
                }
                if (!StringUtils.isBlank(getClientId())
                        && getTimestampExpiry() >= System.currentTimeMillis()
                        && !StringUtils.equals(getClientId(), clientId)) {
                    return LockResult.HOLD_BY_ANOTHER_CLIENT;
                }
                updateLockHolder(clientId, lockDurationMs);
                if (waitWeight >= 0) {
                    synchronized (pQueue) {
                        pQueue.clear();
                    }
                }
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

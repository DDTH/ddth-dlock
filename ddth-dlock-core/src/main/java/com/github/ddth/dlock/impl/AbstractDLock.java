package com.github.ddth.dlock.impl;

import java.util.Properties;

import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;

/**
 * Abstract implementation of {@link IDLock}.
 * 
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class AbstractDLock implements IDLock, AutoCloseable {
    private String name;
    private Properties lockProps;

    public AbstractDLock(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    public AbstractDLock setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Lock's custom properties.
     * 
     * @param lockProps
     * @return
     */
    public AbstractDLock setLockProperties(Properties lockProps) {
        this.lockProps = lockProps != null ? new Properties(lockProps) : new Properties();
        return this;
    }

    /**
     * Get lock's custom properties.
     * 
     * @return
     */
    protected Properties getLockProperties() {
        return lockProps;
    }

    /**
     * Get lock's custom property.
     * 
     * @param key
     * @return
     */
    protected String getLockProperty(String key) {
        return lockProps != null ? lockProps.getProperty(key) : null;
    }

    public AbstractDLock init() {
        return this;
    }

    public void destroy() {
        // EMPTY
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LockResult lock(String clientId) {
        return lock(clientId, DEFAULT_LOCK_DURATION_MS);
    }
}

package com.github.ddth.dlock.impl;

import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Properties;

/**
 * Abstract implementation of {@link IDLock}.
 *
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class AbstractDLock implements IDLock, AutoCloseable {
    private String name;
    private Properties lockProps;

    private String clientId;
    private long timestampExpiry;

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
     * Update current lock's holder info.
     *
     * @param clientId
     * @param lockDurationMs
     * @since 0.1.1
     */
    protected void updateLockHolder(String clientId, long lockDurationMs) {
        setClientId(clientId);
        setTimestampExpiry(System.currentTimeMillis() + lockDurationMs);
    }

    /**
     * Get client-id who is currently holding the lock.
     *
     * <p>
     * Note: value returned from this method is "estimated".
     * </p>
     *
     * @return
     * @since 0.1.1
     */
    protected String getClientId() {
        return clientId;
    }

    /**
     * Get client-id who is currently holding the lock.
     *
     * @param clientId
     * @return
     * @since 0.1.1
     */
    protected AbstractDLock setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Get timestamp (in milliseconds) when the lock-acquisition expires.
     *
     * <p>
     * Note: value returned from this method is "estimated".
     * </p>
     *
     * @return
     * @since 0.1.1
     */
    protected long getTimestampExpiry() {
        return timestampExpiry;
    }

    /**
     * Get timestamp (in milliseconds) when the lock-acquisition expires.
     *
     * @param timestampExpiry
     * @return
     * @since 0.1.1
     */
    protected AbstractDLock setTimestampExpiry(long timestampExpiry) {
        this.timestampExpiry = timestampExpiry;
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

    /**
     * Init method.
     *
     * @return
     */
    public AbstractDLock init() {
        return this;
    }

    /**
     * Cleanup method.
     */
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

    /**
     * {@inheritDoc}
     *
     * @since 0.1.2
     */
    @Override
    public LockResult lock(int waitWeight, String clientId) {
        return lock(waitWeight, clientId, DEFAULT_LOCK_DURATION_MS);
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.1.2
     */
    @Override
    public LockResult lock(String clientId, long lockDurationMs) {
        return lock(-1, clientId, lockDurationMs);
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.1.1
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("name", name).append("props", lockProps).append("clientId", clientId)
                .append("timestampExpiry", timestampExpiry);
        return tsb.toString();
    }
}

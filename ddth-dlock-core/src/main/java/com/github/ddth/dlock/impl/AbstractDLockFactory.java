package com.github.ddth.dlock.impl;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.IDLockFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Abstract implementation of {@link IDLockFactory} that creates
 * {@link AbstractDLock} instances.
 * 
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class AbstractDLockFactory implements IDLockFactory, AutoCloseable {

    private String lockNamePrefix;

    private Map<String, Properties> lockProperties;
    private Cache<String, AbstractDLock> lockInstances = CacheBuilder.newBuilder()
            .expireAfterAccess(3600, TimeUnit.SECONDS)
            .removalListener(new RemovalListener<String, AbstractDLock>() {
                @Override
                public void onRemoval(RemovalNotification<String, AbstractDLock> notification) {
                    AbstractDLock lock = notification.getValue();
                    lock.destroy();
                }
            }).build();

    public AbstractDLockFactory() {
    }

    public AbstractDLockFactory init() {
        return this;
    }

    public void destroy() {
        lockInstances.invalidateAll();
    }

    public void close() {
        destroy();
    }

    /**
     * Prefix {@link #lockNamePrefix} to {@code lockName} if
     * {@link #lockNamePrefix} is not null.
     * 
     * @param lockName
     * @return
     */
    protected String buildLockName(String lockName) {
        return lockNamePrefix != null ? lockNamePrefix + lockName : lockName;
    }

    public String getLockNamePrefix() {
        return lockNamePrefix;
    }

    /**
     * Name of locks created by this factory will be prefixed by this string.
     * 
     * @param lockNamePrefix
     * @return
     */
    public AbstractDLockFactory setLockNamePrefix(String lockNamePrefix) {
        this.lockNamePrefix = lockNamePrefix;
        return this;
    }

    public AbstractDLockFactory setLockProperties(Map<String, Properties> lockProperties) {
        this.lockProperties = lockProperties;
        return this;
    }

    /**
     * Get all locks properties settings.
     * 
     * @return
     */
    protected Map<String, Properties> getLockPropertiesMap() {
        return lockProperties;
    }

    /**
     * Get a lock's properties
     * 
     * @param name
     * @return
     */
    protected Properties getLockProperties(String name) {
        return lockProperties != null ? lockProperties.get(name) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractDLock createLock(String name) {
        String lockName = buildLockName(name);
        try {
            AbstractDLock lock = lockInstances.get(lockName, new Callable<AbstractDLock>() {
                @Override
                public AbstractDLock call() throws Exception {
                    // yup, use "name" here (not "lockName) is correct and
                    // intended!
                    Properties lockProps = getLockProperties(name);
                    return createAndInitLockInstance(lockName, lockProps);
                }
            });
            return lock;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create and initializes an {@link IDLock} instance, ready for use.
     * 
     * @param name
     * @param lockProps
     * @return
     */
    protected AbstractDLock createAndInitLockInstance(String name, Properties lockProps) {
        AbstractDLock lock = createLockInternal(name, lockProps);
        lock.init();
        return lock;
    }

    /**
     * Create a new lock instance, but does not initialize it. Convenient method
     * for sub-class to override.
     * 
     * @param name
     * @param lockProps
     * @return
     */
    protected abstract AbstractDLock createLockInternal(String name, Properties lockProps);
}

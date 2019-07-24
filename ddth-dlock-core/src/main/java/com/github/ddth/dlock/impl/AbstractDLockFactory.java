package com.github.ddth.dlock.impl;

import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.IDLockFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Abstract implementation of {@link IDLockFactory} that creates
 * {@link AbstractDLock} instances.
 *
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class AbstractDLockFactory implements IDLockFactory, AutoCloseable {

    private Logger LOGGER = LoggerFactory.getLogger(AbstractDLockFactory.class);

    private String lockNamePrefix;

    private Map<String, Properties> lockProperties;
    private Cache<String, AbstractDLock> lockInstances = CacheBuilder.newBuilder()
            .expireAfterAccess(3600, TimeUnit.SECONDS)
            .removalListener((RemovalListener<String, AbstractDLock>) notification -> {
                AbstractDLock lock = notification.getValue();
                try {
                    lock.destroy();
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage(), e);
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
     * Get a lock's properties.
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
            AbstractDLock lock = lockInstances.get(lockName, () -> {
                Properties lockProps = getLockProperties(name); // use "name" here (not "lockName)
                return createAndInitLockInstance(lockName, lockProps);
            });
            return lock;
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
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

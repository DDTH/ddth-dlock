package com.github.ddth.dlock.impl.inmem;

import com.github.ddth.dlock.IDLockFactory;
import com.github.ddth.dlock.impl.AbstractDLockFactory;

import java.util.Properties;

/**
 * In-memory implementation of {@link IDLockFactory} that creates
 * {@link InmemDLock} objects.
 *
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class InmemDLockFactory extends AbstractDLockFactory {
    /**
     * {@inheritDoc}
     *
     * @since 0.1.2
     */
    @Override
    public InmemDLock createLock(String name) {
        return (InmemDLock) super.createLock(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InmemDLock createLockInternal(String name, Properties lockProps) {
        InmemDLock lock = new InmemDLock(name);
        lock.setLockProperties(lockProps);
        return lock;
    }
}

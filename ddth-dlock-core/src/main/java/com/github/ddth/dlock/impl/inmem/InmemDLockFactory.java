package com.github.ddth.dlock.impl.inmem;

import java.util.Properties;

import com.github.ddth.dlock.IDLockFactory;
import com.github.ddth.dlock.impl.AbstractDLockFactory;

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
     */
    @Override
    protected InmemDLock createLockInternal(String name, Properties lockProps) {
        InmemDLock lock = new InmemDLock(name);
        lock.setLockProperties(lockProps);
        return lock;
    }

}

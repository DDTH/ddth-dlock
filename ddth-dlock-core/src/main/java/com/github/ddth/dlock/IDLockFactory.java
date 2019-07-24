package com.github.ddth.dlock;

/**
 * Factory to create {@link IDLock} instances.
 *
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public interface IDLockFactory {
    /**
     * Create a new lock.
     *
     * @param name
     * @return
     */
    IDLock createLock(String name);
}

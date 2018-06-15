package com.github.ddth.dlock;

/**
 * Distributed lock interface.
 * 
 * <p>
 * Implementation:
 * <ul>
 * <li>Each lock is identified by a unique name(space).</li>
 * <li>Within a name(space), only one client is allowed to hold lock as a given
 * time; each client is identified by a unique client-id (within the
 * namespace).</li>
 * </ul>
 * </p>
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public interface IDLock {

    final static long DEFAULT_LOCK_DURATION_MS = 60000;

    /**
     * Get lock's name(space).
     * 
     * @return
     */
    String getName();

    /**
     * Acquire the lock for {@code clientId} with default duration.
     * 
     * <p>
     * Reentrant: lock can be acquired multiple times by the same
     * {@code clientId}. Lock's expiry will be extended accordingly.
     * </p>
     * 
     * @param clientId
     *            within a namespace, only one client is allowed to hold lock as
     *            a given time
     * @return {@link LockResult#SUCCESSFUL} if successful,
     *         {@link LockResult#HOLD_BY_ANOTHER_CLIENT} if lock is currently
     *         hold by another client
     * @throws DLockException
     */
    LockResult lock(String clientId) throws DLockException;

    /**
     * Acquire the lock for {@code clientId} for a duration of
     * {@link lockDurationMs}.
     * 
     * <p>
     * Reentrant: lock can be acquired multiple times by the same
     * {@code clientId}. Lock's expiry will be extended accordingly.
     * </p>
     * 
     * @param clientId
     *            within a namespace, only one client is allowed to hold lock as
     *            a given time
     * @param lockDurationMs
     * @return {@link LockResult#SUCCESSFUL} if successful,
     *         {@link LockResult#HOLD_BY_ANOTHER_CLIENT} if lock is currently
     *         hold by another client
     */
    LockResult lock(String clientId, long lockDurationMs);

    /**
     * Acquire the lock for {@code clientId} with default duration.
     * 
     * <p>
     * Reentrant: lock can be acquired multiple times by the same
     * {@code clientId}. Lock's expiry will be extended accordingly.
     * </p>
     * 
     * 
     * @param waitWeight
     *            "fairness": {@code clientId} with higher {@code waitWeight}
     *            value might have higher chance to acquire the lock, negative
     *            value means "no fairness"
     * @param clientId
     *            within a namespace, only one client is allowed to hold lock as
     *            a given time
     * @return {@link LockResult#SUCCESSFUL} if successful,
     *         {@link LockResult#HOLD_BY_ANOTHER_CLIENT} if lock is currently
     *         hold by another client
     * @since 0.1.2
     */
    LockResult lock(int waitWeight, String clientId);

    /**
     * Acquire the lock for {@code clientId} for a duration of
     * {@link lockDurationMs}.
     * 
     * <p>
     * Reentrant: lock can be acquired multiple times by the same
     * {@code clientId}. Lock's expiry will be extended accordingly.
     * </p>
     * 
     * @param waitWeight
     *            "fairness": {@code clientId} with higher {@code waitWeight}
     *            value might have higher chance to acquire the lock, negative
     *            value means "no fairness"
     * @param clientId
     *            within a namespace, only one client is allowed to hold lock as
     *            a given time
     * @param lockDurationMs
     * @return {@link LockResult#SUCCESSFUL} if successful,
     *         {@link LockResult#HOLD_BY_ANOTHER_CLIENT} if lock is currently
     *         hold by another client
     * @since 0.1.2
     */
    LockResult lock(int waitWeight, String clientId, long lockDurationMs);

    /**
     * Release the lock.
     * 
     * @param clientId
     *            within a namespace, only one client is allowed to hold lock as
     *            a given time
     * @return {@link LockResult#SUCCESSFUL} if successful,
     *         {@link LockResult#HOLD_BY_ANOTHER_CLIENT} if lock is currently
     *         hold by another client, {@link LockResult#NOT_FOUND} if the lock
     *         is not currently hold by any client (not locked before, or
     *         already expired)
     */
    LockResult unlock(String clientId);
}

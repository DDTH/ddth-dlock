package com.github.ddth.dlock;

/**
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public enum LockResult {
    /**
     * Lock operation was successful
     */
    SUCCESSFUL(0),

    /**
     * Lock is currently hold by another client
     */
    HOLD_BY_ANOTHER_CLIENT(1),

    /**
     * Lock is not currently hold by any client (not locked before, or already
     * expired)
     */
    NOT_FOUND(2);

    public final int value;

    LockResult(int value) {
        this.value = value;
    }
}

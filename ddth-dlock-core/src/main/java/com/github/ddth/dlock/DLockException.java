package com.github.ddth.dlock;

/**
 * Throws to indicate there has been an exception while interacting with the
 * underlying system.
 *
 * @author Thanh Ba Nguyen <bnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class DLockException extends RuntimeException {

    private static final long serialVersionUID = "0.1.0".hashCode();

    public DLockException() {
    }

    public DLockException(String message) {
        super(message);
    }

    public DLockException(Throwable cause) {
        super(cause);
    }

    public DLockException(String message, Throwable cause) {
        super(message, cause);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Throws to indicate that the operation is not supported/allowed.
     *
     * @author Thanh Ba Nguyen <bnguyen2k@gmail.com>
     * @since 0.1.0
     */
    public static class OperationNotSupportedException extends DLockException {
        private static final long serialVersionUID = "0.1.0".hashCode();

        public OperationNotSupportedException() {
        }

        public OperationNotSupportedException(String message) {
            super(message);
        }
    }
}

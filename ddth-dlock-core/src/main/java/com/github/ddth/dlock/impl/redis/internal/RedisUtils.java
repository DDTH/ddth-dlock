package com.github.ddth.dlock.impl.redis.internal;

import com.github.ddth.commons.redis.JedisConnector;

/**
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 * @since 1.0.0
 */
public class RedisUtils {
    /**
     * Close the supplied {@link JedisConnector} instance.
     *
     * @param jc
     * @param canClose
     * @return
     */
    public static JedisConnector closeJedisConnector(JedisConnector jc, boolean canClose) {
        if (canClose) {
            jc.close();
            return null;
        }
        return jc;
    }
}

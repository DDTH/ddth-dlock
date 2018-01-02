package com.github.ddth.dlock.test.inmem;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;

import com.github.ddth.dlock.impl.inmem.InmemDLockFactory;
import com.github.ddth.dlock.test.BaseDLockTCase;

/**
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class InmemDLockTCase extends BaseDLockTCase {

    @Before
    public void setUp() {
        Map<String, Properties> lockProperties = new HashMap<>();

        InmemDLockFactory factory = new InmemDLockFactory();
        factory.setLockProperties(lockProperties);
        factory.init();
        lockFactory = factory;
    }

}

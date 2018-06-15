[![Build Status](https://travis-ci.org/DDTH/ddth-dlock.svg?branch=master)](https://travis-ci.org/DDTH/ddth-dlock)

# ddth-dlock

DDTH's Distributed Lock library.

Project home:
[https://github.com/DDTH/ddth-dlock](https://github.com/DDTH/ddth-dlock)

**`ddth-dlock` requires Java 8+**


## License

See LICENSE.txt for details. Copyright (c) 2018 Thanh Ba Nguyen.

Third party libraries are distributed under their own licenses.


## Installation

Latest release version: `0.1.1.4`. See [RELEASE-NOTES.md](RELEASE-NOTES.md).

Maven dependency: if only a sub-set of `ddth-dlock` functionality is used, choose the corresponding
dependency artifact(s) to reduce the number of unused jar files.

*ddth-dlock-core*: in-memory caches, all other dependencies are *optional*.

```xml
<dependency>
	<groupId>com.github.ddth</groupId>
	<artifactId>ddth-dlock-core</artifactId>
	<version>0.1.1.4</version>
</dependency>
```

*ddth-dlock-redis*: include all *ddth-dlock-core* and Redis dependencies.

```xml
<dependency>
    <groupId>com.github.ddth</groupId>
    <artifactId>ddth-dlock-redis</artifactId>
    <version>0.1.1.4</version>
    <type>pom</type>
</dependency>
```


## Usage

This library is a simple distributed implementation:

- Each lock is identified by a unique name(space).
- Within a name(space), only one client is allowed to hold lock as a given time; each client is identified by a unique client-id (within the namespace).

Notes:

- Two threads or processes can hold a same lock if they use the same client-id.
- Lock can be hold for a specific duration. When the duration has passed, lock is expired.
- Reentrant: call method `lock(...)` again before expired time to renew the lock.


### 1. Obtain the lock factory

```java
// in-memory lock factory, with default settings
IDLockFactory factory = new InmemDLockFactory().init();

// lock factory with some settings
IDLockFactory factory = new InmemDLockFactory()
    .setLockNamePrefix("some-prefix-")
    .init();

// Redis lock factory
IDLockFactory factory = new RedisDLockFactory()
    .setRedisHostAndPort("localhost:6379")
    .setRedisPassword("secret")
    .init();

// Clustered-Redis lock factory
IDLockFactory factory = new ClusteredRedisDLockFactory()
    .setRedisHostsAndPorts("localhost:6379,host2:port2,host3:port3")
    .setRedisPassword("secret")
    .init();

// A lock factory with some specific settings.
Map<String, Properties> lockProps = new HashMap<String, Properties>();
Properties propLock1 = new Properties();
propCache1.put("dlock.redis_host_and_port", "host1:6379");
lockProps.put("lockName1", propLock1);

Properties propLock2 = new Properties();
propCache2.put("dlock.redis_host_and_port", "host2:6379");
lockProps.put("lockName2", propLock2s);

IDLockFactory factory = new RedisDLockFactory()
    .setLockProperties(lockProps)
    .setRedisHostAndPort("localhost:6379")
    .setRedisPassword("secret")
    .init();
```

**Lock Settings & Custom Properties:**

Custom properties for Redis-based locks:

- `dlock.redis_password` : (string) password to connect to Redis server

Custom properties for `ClusteredRedisDLock`:

- `dlock.hosts_and_ports`: (string) Redis cluster's hosts and ports in format `host1:port1,host2:port2,host3:port3`

Custom properties for `RedisDLock`:

- `dlock.host_and_port`  : (string) Redis server's host and port in format `host:port`

### 2. Obtain the lock object

```java
IDLock lock = factory.createLock("my-lock-name");

//lock with default duration
if ( lock.lock("my-client-id")==LockResult.SUCCESSFUL ) {
    try {
        //I now acquire the lock
    } finally {
        //donot forget to unlock
        //if unlock is not called, the acquired lock will expire when duration has passed
        lock.unlock("my-client-id");
    }
}

//lock for 10 seconds
if ( lock.lock("my-client-id", 10000)==LockResult.SUCCESSFUL ) {
    try {
        //I now acquire the lock
    } finally {
        //donot forget to unlock
        //if unlock is not called, the acquired lock will expire when duration has passed
        lock.unlock("my-client-id");
    }
}
```

### 3. Destroy the factory when done

```java
((AbstractDLockFactory)factory).destroy();
```

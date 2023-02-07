package cn.redis;

import redis.clients.jedis.JedisPool;

/**
 * @since 2018/5/2
 */
public class DistributedLockWrapper extends DistributedLock {

    String lockName;
    String identifier;

    public DistributedLockWrapper(JedisPool jedisPool, String lockName) {
        super(jedisPool);
        this.lockName = lockName;
    }

    @Override
    public String lockWithTimeout(String locaName, Long acquireTimeout, int timeout) {
        this.identifier = super.lockWithTimeout(locaName, acquireTimeout, timeout);
        return this.identifier;
    }

    public boolean releaseLock() {
        if (this.identifier == null) {
            return true;
        }
        return super.releaseLock(this.lockName, this.identifier);
    }
}

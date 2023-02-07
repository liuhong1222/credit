package cn.redis;

import cn.utils.JacksonUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Component
public class RedisClient {

    private final static Logger logger = LoggerFactory.getLogger(RedisClient.class);

    @Autowired
    private JedisPool jedisPool;

    // 存对象
    public void setObject(String key, Object obj, int expireOfSeconds) throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            ObjectOutputStream oos = null;  //对象输出流
            ByteArrayOutputStream bos = null;  //内存缓冲流
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            byte[] byt = bos.toByteArray();
            jedis.set(key.getBytes(), byt);
            jedis.expire(key, expireOfSeconds);
            bos.close();
            oos.close();
        } catch (Exception e) {
            logger.error("jedis setObject 出错,key[" + key + "],obj[" + JacksonUtil.toJson(obj) + "]", e);
            throw new RuntimeException(e);
        }
        //返还到连接池

    }

    // 取对象
    public Object getObject(String key) throws Exception {
        Object obj = null;
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] byt = jedis.get(key.getBytes());
            if (byt != null) {
                ObjectInputStream ois = null;  //对象输入流
                ByteArrayInputStream bis = null;   //内存缓冲流
                bis = new ByteArrayInputStream(byt);
                ois = new ObjectInputStream(bis);
                obj = ois.readObject();
                bis.close();
                ois.close();
            }
        } catch (Exception e) {
            logger.error("jedis getObject 出错,key[" + key + "]", e);
            throw new RuntimeException(e);
        }
        //返还到连接池
        return obj;
    }

    public void set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
//            jedis.expire(key, 30 * 60 * 1000);
        } catch (Exception e) {
            logger.error("jedis set 出错,key[" + key + "],value[" + value + "]", e);
            throw new RuntimeException(e);
        }
        //返还到连接池
    }

    public void set(String key, String value, int expire) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
            jedis.expire(key, expire);
        } catch (Exception e) {
            logger.error("jedis set 出错,key[" + key + "],value[" + value + "],expire[" + expire + "]", e);
            throw new RuntimeException(e);
        }
        //返还到连接池
    }

    public String get(String key) {

        String value = "";
        try (Jedis jedis = jedisPool.getResource()) {
            value = jedis.get(key);
        } catch (Exception e) {
            logger.error("jedis set 出错,key[" + key + "],value[" + value + "]", e);
            throw new RuntimeException(e);
        }
        //返还到连接池

        return value;
    }

    public void remove(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            logger.error("jedis remove 出错,key[" + key + "]", e);
            throw new RuntimeException(e);
        }
        //返还到连接池
    }
    
    public void incr(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.incr(key);
        } catch (Exception e) {
            logger.error("jedis incr 出错,key[" + key + "]", e);
            throw new RuntimeException(e);
        }
        //返还到连接池
    }
    
    public void incrBy(String key,int value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.incrBy(key, value);
        } catch (Exception e) {
            logger.error("jedis incrBy 出错,key[" + key + "]", e);
            throw new RuntimeException(e);
        }
        //返还到连接池
    }
    
    public void decr(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.decr(key);
        } catch (Exception e) {
            logger.error("jedis decr 出错,key[" + key + "]", e);
            throw new RuntimeException(e);
        }
        //返还到连接池
    }
    
    public void decrBy(String key,int value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.decrBy(key, value);
        } catch (Exception e) {
            logger.error("jedis decrBy 出错,key[" + key + "]", e);
            throw new RuntimeException(e);
        }
        //返还到连接池
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.retwis.util;

import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author siyu
 */
public class JedisPoolFactory {
    
    public static JedisPool jedisPool;

    public static JedisPool getJedisPool() {
        
         if(jedisPool==null){
            Config config = new JedisPoolConfig();
            config.maxActive = 500;
            config.maxIdle = 100;
            config.maxWait = 5000;
            config.testOnBorrow = true;
            jedisPool =  new JedisPool(config,"180.186.22.98");
//            jedisPool = new JedisPool(config, "192.168.1.253");
//            jedisPool = new JedisPool(config, "192.168.1.168");
        }
        return jedisPool;        
    }
    
    @Autowired
    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

}

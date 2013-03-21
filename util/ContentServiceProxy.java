/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.retwis.util;

import com.retwis.service.ContentServiceImpl;
import com.retwis.service.IContentService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

/**
 *
 * @author siyu
 */
public class ContentServiceProxy implements InvocationHandler{
    
    private JedisPool jedisPool;
    private ContentServiceImpl  contentServiceImpl;

    public ContentServiceProxy(JedisPool jedisPool, ContentServiceImpl contentServiceImpl) {
        this.jedisPool = jedisPool;
        this.contentServiceImpl = contentServiceImpl;
    }
    
    public static IContentService newInstance(JedisPool jedisPool, ContentServiceImpl contentServiceImpl){
        return (IContentService)Proxy.newProxyInstance(ContentServiceImpl.class.getClassLoader(), ContentServiceImpl.class.getInterfaces(), new ContentServiceProxy(jedisPool, contentServiceImpl));
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result;
                Jedis jedis = obtainJedis();
                args[0] = jedis;
                try {
                        result = method.invoke(this.contentServiceImpl, args);
                } catch (InvocationTargetException e) {
                        throw e.getTargetException();
                } catch (Exception e) {
                        throw new JedisException("Unexpected proxy invocation exception: " + e.getMessage(), e);
                } finally {
                        returnJedis(jedis);
                }
                return result;
    }
    
        private Jedis obtainJedis() {
                Jedis jedis;
                jedis = jedisPool.getResource();
                return jedis;
        }

        private void returnJedis(Jedis jedis) {
                try {
                        if (jedis.isConnected()) {
                                jedis.ping();
                                jedisPool.returnResource(jedis);
                        } else {
                                jedisPool.returnBrokenResource(jedis);
                        }
                } catch (JedisException e) {
                        jedisPool.returnBrokenResource(jedis);
                }
        }
    
}

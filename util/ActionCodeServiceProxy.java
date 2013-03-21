/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.retwis.util;

import com.retwis.service.ActionCodeService;
import com.retwis.service.ActionCodeServiceImpl;
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
public class ActionCodeServiceProxy implements InvocationHandler{
    private JedisPool jedisPool;
    private ActionCodeServiceImpl actionCodeServiceImpl;

    public ActionCodeServiceProxy(JedisPool jedisPool, ActionCodeServiceImpl actionCodeServiceImpl) {
        this.jedisPool = jedisPool;
        this.actionCodeServiceImpl = actionCodeServiceImpl;
    }
    
    public static ActionCodeService newInstance(JedisPool jedisPool, ActionCodeServiceImpl actionCodeServiceImpl){
        return (ActionCodeService)Proxy.newProxyInstance(ActionCodeServiceImpl.class.getClassLoader(), ActionCodeServiceImpl.class.getInterfaces(), new ActionCodeServiceProxy(jedisPool, actionCodeServiceImpl));
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result;
                Jedis jedis = obtainJedis();
                args[0] = jedis;
                try {
                        result = method.invoke(this.actionCodeServiceImpl, args);
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

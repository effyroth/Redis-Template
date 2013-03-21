/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.retwis.util;

/**
 *
 * @author Administrator
 */

import com.retwis.service.IStatusService;

import com.retwis.service.StatusServiceImpl;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


import redis.clients.jedis.Jedis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

public class StatusServiceProxy implements InvocationHandler {

        private final JedisPool jedisPool;
        private final StatusServiceImpl statusServiceImpl;

        public StatusServiceProxy(JedisPool pool,StatusServiceImpl statusServiceImpl) {
                this.statusServiceImpl = statusServiceImpl;
                this.jedisPool = pool;
        }

        public static IStatusService newInstance(JedisPool pool, StatusServiceImpl  statusServiceImpl) {
                return  (IStatusService)Proxy.newProxyInstance(StatusServiceImpl.class.getClassLoader(), StatusServiceImpl.class.getInterfaces(), new  StatusServiceProxy(pool, statusServiceImpl));
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                Object result;
                Jedis jedis = obtainJedis();
                args[0] = jedis;
                try {
                        result = m.invoke(this.statusServiceImpl, args);
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

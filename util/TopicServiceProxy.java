/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.retwis.util;

/**
 *
 * @author Administrator
 */

import com.retwis.service.ITopicService;

import com.retwis.service.TopicServiceImpl;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


import redis.clients.jedis.Jedis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

public class TopicServiceProxy implements InvocationHandler {

        private final JedisPool jedisPool;
        private final TopicServiceImpl topicServiceImpl;

        public TopicServiceProxy(JedisPool pool,TopicServiceImpl topicServiceImpl) {
                this.topicServiceImpl = topicServiceImpl;
                this.jedisPool = pool;
        }

        public static ITopicService newInstance(JedisPool pool, TopicServiceImpl  topicServiceImpl) {
                return  (ITopicService)Proxy.newProxyInstance(TopicServiceImpl.class.getClassLoader(), TopicServiceImpl.class.getInterfaces(), new  TopicServiceProxy(pool, topicServiceImpl));
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                Object result;
                Jedis jedis = obtainJedis();
                args[0] = jedis;
                try {
                        result = m.invoke(this.topicServiceImpl, args);
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

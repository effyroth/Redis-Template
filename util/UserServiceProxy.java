/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.retwis.util;

/**
 *
 * @author Administrator
 */
import com.retwis.service.IUserService;
import com.retwis.service.UserServiceImpl;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

public class UserServiceProxy implements InvocationHandler {

        private final JedisPool jedisPool;
        private final UserServiceImpl userServiceImpl;

        public UserServiceProxy(JedisPool pool,UserServiceImpl userServiceImpl) {
                this.userServiceImpl = userServiceImpl;
                this.jedisPool = pool;
        }

        public static IUserService newInstance(JedisPool pool,UserServiceImpl userServiceImpl) {
                return  (IUserService)Proxy.newProxyInstance(UserServiceImpl.class.getClassLoader(), UserServiceImpl.class.getInterfaces(), new UserServiceProxy(pool,userServiceImpl));
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                Object result;
                Jedis jedis = null;
                try{
                    jedis = obtainJedis();
                }catch(Exception e){
                    return null;
                }
                if(jedis == null){
                    return null;
                }
                args[0] = jedis;
                try {
                        result = m.invoke(this.userServiceImpl, args);
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
                                //jedis.ping();
                                jedisPool.returnResource(jedis);
                        } else {
                                jedisPool.returnBrokenResource(jedis);
                        }
                } catch (JedisException e) {
                        jedisPool.returnBrokenResource(jedis);
                }
        }
}

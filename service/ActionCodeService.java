/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.retwis.service;

import redis.clients.jedis.Jedis;

/**
 *
 * @author Administrator
 */
public interface ActionCodeService {
       public void addActionCode(Jedis jedis, String actionCode, Integer project);
       public String getActionCode(Jedis jedis, Integer project);
}

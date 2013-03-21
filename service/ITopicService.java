/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.retwis.service;

import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Administrator
 */
public interface ITopicService {

    public String getIdByName(Jedis jedis ,String topicname);

    public void addTopicMention(Jedis jedis,String topicname,Integer commentid);

    public String addTopic(Jedis jedis,String topicname);

    public List<String> getTopicMention(Jedis jedis,Integer topicid,Integer page);

    public Long getTopicMentionCount(Jedis jedis,Integer topicid);

    public Map setTopic(Jedis jedis, String topicname , Map m);
            
}

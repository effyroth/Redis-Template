/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.retwis.service;

import com.retwis.service.base.BaseServiceImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Administrator
 */
public class TopicServiceImpl extends BaseServiceImpl implements ITopicService{
    

    private void createTopic(Jedis jedis,String topicname,Long topicid){
        jedis.set(String.format(TOPIC_NAME, topicname),topicid.toString() );
        Long time = System.currentTimeMillis();
        Map m = new HashMap();
        m.put("name", topicname);
        m.put("time", time.toString());
        jedis.hmset(String.format(TOPIC_ID, topicid), m);
    }

    public synchronized String addTopic(Jedis jedis,String topicname){
        if(!existTopic(jedis,topicname)){
            Long id = jedis.incr(GLOBAL_TOPIC_ID);
            createTopic(jedis,topicname,id);
            jedis.lpush(NEW_TOPIC_LIST, id.toString());
            jedis.zadd(HOT_TOPIC_ZSET, 0, id.toString());
            return id.toString();
        }
        return getIdByName(jedis,topicname);
    }

    public String getIdByName(Jedis jedis ,String topicname){
        return jedis.get(String.format(TOPIC_NAME,topicname.toLowerCase()));
    }

    public String getNameById(Jedis jedis ,String topicid){
        return jedis.hget(String.format(TOPIC_ID,topicid),"name");
    }

    public void addTopicMention(Jedis jedis,String topicname,Integer commentid){
        String id = addTopic(jedis,topicname);
        //getIdByName(jedis,topicname);
        jedis.lpush(String.format(TOPIC_MENTION_LIST,id), commentid.toString());
        jedis.zincrby(HOT_TOPIC_ZSET, 1, id);
    }

    public List<String> getTopicMention(Jedis jedis,Integer topicid,Integer page){
        return (List<String>)getTopic(jedis,TOPIC_MENTION_LIST,topicid,page,10);
    }

    public List<String> getNewTopic(Jedis jedis,Integer page){
        return (List<String>)getTopic(jedis,NEW_TOPIC_LIST,null,page,10);
    }

    public Set<String> getHotTopic(Jedis jedis,Integer page){
        return (Set<String>)getTopic(jedis,HOT_TOPIC_ZSET,null,page,10);
    }

    public Map setTopic(Jedis jedis, String topicname , Map m){
        Map topic = new HashMap();
        topic.put("name",topicname);
        String id = getIdByName(jedis,topicname);
        if(id!=null){
            topic.put("id",id);
            topic.put("totalcomment",getTopicMentionCount(jedis,Integer.parseInt(id)));
        }else{
            topic.put("id",-1);
            topic.put("totalcomment",0);
        }
        m.put("topic", topic);
        setHotTopic(jedis,m);
        setNewTopic(jedis,m);
        return m;
    }

    public Map setNewTopic(Jedis jedis , Map m){
        
        List<String> l = getNewTopic(jedis,1);
        List newtopiclist = new ArrayList();

        for(String s:l){
            List<String> list = jedis.hmget(String.format(TOPIC_ID,s),"name","time");
            Map map = new HashMap();
            map.put("name",list.get(0));
            Long time = System.currentTimeMillis()-Long.parseLong(list.get(1));
            String t = gettime(time);
            map.put("time",t);
            newtopiclist.add(map);
        }
        
        m.put("newtopiclist", newtopiclist);
        return m;
    }

    public Map setHotTopic(Jedis jedis , Map m){

        Set<String> set = getHotTopic(jedis,1);
        List hottopiclist = new ArrayList();

        for(String s:set){
            String name = getNameById(jedis,s);
            Long totalcount = getListLength(jedis,String.format(TOPIC_MENTION_LIST,s));
            Map map = new HashMap();
            map.put("name",name);
            map.put("totalcount",totalcount);
            hottopiclist.add(map);
        }
        m.put("hottopiclist", hottopiclist);
        
        return m;
    }

    public Object getTopic(Jedis jedis,String type, Integer topicid,Integer page,Integer pagesize){
        if (page < 1)
            page = 1;
        Integer start = (page-1)*pagesize;
        Integer end = page*pagesize-1;
        if(type==HOT_TOPIC_ZSET){
            return jedis.zrevrangeByScore(type,"+inf", "-inf", start, end);
        }else{
            return jedis.lrange(String.format(type, topicid), start, end);
        }
    }

    public Long getTopicMentionCount(Jedis jedis,Integer topicid){
        return jedis.llen(String.format(TOPIC_MENTION_LIST, topicid));
    }

    public boolean existTopic(Jedis jedis,String topicname){
        return jedis.get(String.format(TOPIC_NAME,topicname))!=null;
    }

    public void deleteTopic(){

    }

    public boolean checkTopic(Jedis jedis,Integer userid,Integer topicid){
        return jedis.sismember(String.format(TOPIC_FOLLOWED_SET, topicid), userid.toString());
    }
    
    public void followTopic(Jedis jedis,Integer userid,Integer topicid){
        if(!checkTopic(jedis,userid,topicid)){
            jedis.sadd(String.format(TOPIC_FOLLOWED_SET, topicid), userid.toString());
        }
    }

    public void unfollowTopic(Jedis jedis,Integer userid,Integer topicid){
        if(checkTopic(jedis,userid,topicid)){
            jedis.srem(String.format(TOPIC_FOLLOWED_SET, topicid), userid.toString());
        }
    }

    public void pushTopic(Jedis jedis,String topicname,Integer commentid){
        Set<String> sub = jedis.smembers(String.format(TOPIC_FOLLOWED_SET,topicname));
        for(String userid:sub){
            jedis.lpush(String.format(USER_TOPIC_MENTION_LIST, userid), commentid.toString());
        }
    }

    private String gettime(Long time){
	time =time / 1000;
	if (time > 31536000) {
		return time/31536000 + "年前";
	} else if (time > 2592000) {
		return time / 2592000 + "个月前";
	} else if (time > 86400) {
		return time / 86400 + "天前";
	} else if (time > 3600) {
		return time / 3600 + "小时前";
	} else if (time > 60) {
		return time / 60 + "分钟前";
	} else {
		return time + "秒前";
	}
    }

    private static final String TOPIC_NAME = "topic:name:%s";
    private static final String TOPIC_ID = "topic:id:%s";
    private static final String TOPIC_FOLLOWED_SET = "topic:id:%s:followed:set";
    private static final String TOPIC_MENTION_LIST = "topic:id:%s:mention:list";
    private static final String USER_TOPIC_MENTION_LIST = "user:id:%s:topicmention:list";
    private static final String NEW_TOPIC_LIST = "newtopic:list";
    private static final String HOT_TOPIC_ZSET = "hottopic:zset";
    private static final String GLOBAL_TOPIC_ID = "global:nexttopicid";
}

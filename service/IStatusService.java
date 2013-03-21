package com.retwis.service;

import java.util.List;

import com.retwis.Status;
import redis.clients.jedis.Jedis;

/**
 * 
 * @author yongboy
 * @date 2011-4-4
 * @version 1.0
 */
public interface IStatusService {
	void save(Jedis jedis,long userId, String value, String userIp);

	Status load(long id);

	void init();
	
	List<Status> timeline(Jedis jedis,int page);

	List<Status> timeline(Jedis jedis,long userId, int page);

	List<Status> mentions(Jedis jedis,long userId, int page);

	List<Status> mentions(Jedis jedis,String userName, int page);

	List<Status> posts(Jedis jedis,long userId, int page);

        List<String> mentionsidlist(Jedis jedis,int targetId, int page);

        int mentionscount(Jedis jedis,int targetId);

    
}
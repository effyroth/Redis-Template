package com.retwis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.retwis.Status;
import com.retwis.service.base.BaseServiceImpl;
import com.retwis.util.RegUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

/**
 * 
 * @author yongboy
 * @date 2011-4-4
 * @version 1.0
 */
public class StatusServiceImpl extends BaseServiceImpl<Status> implements
		IStatusService {
        @Autowired
	private IUserService userService;

	public void save(Jedis jedis,long userId, String value, String userIp) {
		Status status = new Status();
		status.setId(getNextId(jedis));
		status.setUid(userId);
		status.setIp(userIp);
		status.setContent(value);

		super.save(getFormatId(status.getId()), status);

		// add to self's timeline
		super.addHeadList(getTimelineId(userId), Long.toString(status.getId()));
		// add to self's posts
		super.addHeadList(getPostsId(userId), Long.toString(status.getId()));

		// add to follower's timeline
		Set<String> followed = userService.getFollowed(jedis,userId,0);
		for (String name : followed) {
			long uid = userService.loadIdByName(jedis,name);
			if (uid < 1L)
				continue;

			super.addHeadList(getTimelineId(uid), Long.toString(status.getId()));
		}

		// add to mentions
		List<String> userNames = RegUtils.getValuesListByOne(value, "@(\\w+)",
				1);//

		if (!userNames.isEmpty()) {
			for (String name : userNames) {
				long targetUserId = this.userService.loadIdByName(jedis,name);

				if (targetUserId < 1L) {
					continue;
				}

				super.addHeadList(getMentionsId(targetUserId),
						Long.toString(status.getId()));
			}
		}

		// add to the global timeline
		super.addHeadList(GLOBAL_TIMELINE_FORMAT, Long.toString(status.getId()));
	}

	public Status load(long id) {
		return super.get(getFormatId(id));
	}

	public void init() {
		String currStatusId = getStr(GLOBAL_STATUS_ID);
		long currUid = 0L;
		if (currStatusId == null || currStatusId.length() == 0)
			currUid = -1L;

		if (currUid < 1000L) {
			saveStr(GLOBAL_STATUS_ID, Long.toString(1000L));
		}
	}

	public List<Status> timeline(Jedis jedis,int page) {
		return timeline(jedis,GLOBAL_TIMELINE_FORMAT, page);
	}

	public List<Status> timeline(Jedis jedis,long userId, int page) {
		return timeline(jedis,getTimelineId(userId), page);
	}

	public List<Status> mentions(Jedis jedis,long userId, int page) {
		return timeline(jedis,getMentionsId(userId), page);
	}

	public List<Status> posts(Jedis jedis,long userId, int page) {
		return timeline(jedis,getPostsId(userId), page);
	}

	public List<Status> mentions(Jedis jedis,String userName, int page) {
		long userId = userService.loadIdByName(jedis,userName);

		if (userId < 1L)
			return null;

		return mentions(jedis,userId, page);
	}

	private String getFormatId(long id) {
		return String.format(STATUS_ID_FORMAT, id);
	}

	private long getNextId(Jedis jedis) {
		return super.incr(jedis,GLOBAL_STATUS_ID);
	}

	private String getTimelineId(long userId) {
		return String.format(TIMELINE_FORMAT, userId);
	}

	private String getPostsId(long userId) {
		return String.format(POSTS_FORMAT, userId);
	}

	private String getMentionsId(long userId) {
		return String.format(MENTIONS_FORMAT, userId);
	}

        private List<Status> timeline(Jedis jedis,String targetId, int page) {
		if (page < 1)
			page = 1;

		int startIndex = (page - 1) * 10;
		int endIndex = page * 10;

		List<String> idList = ListRange(targetId, startIndex, endIndex);

		if (idList.isEmpty())
			return new ArrayList<Status>(0);

		List<Status> statusList = new ArrayList<Status>(idList.size());
		for (String id : idList) {
                    //
			Status status = load(Long.valueOf(id));

			if (status == null)
				continue;

			status.setUser(userService.load(jedis,status.getUid()));

			statusList.add(status);
		}

		return statusList;
	}
        //取出redis中的mentions'page 内容为@评论的id
	public List<String> mentionsidlist(Jedis jedis,int targetId, int page) {
		if (page < 1)
			page = 1;

		int startIndex = (page - 1) * 10;
		int endIndex = page * 10-1;

		List<String> idList = ListRange(jedis,String.format(MENTIONS_FORMAT, targetId), startIndex, endIndex);
		return idList;
	}
        
        public int mentionscount(Jedis jedis,int targetId){
            long count = getListLength(jedis,getFormatKeyStr(MENTIONS_FORMAT, targetId));
            
            return (int) count;
        }

	private static final String GLOBAL_STATUS_ID = "global:nextStatusId";
	private static final String STATUS_ID_FORMAT = "status:id:%d";
	private static final String POSTS_FORMAT = "user:id:%d:posts";
	private static final String TIMELINE_FORMAT = "user:id:%d:timeline";
	private static final String MENTIONS_FORMAT = "user:id:%d:mentions";
	private static final String GLOBAL_TIMELINE_FORMAT = "timeline";
}
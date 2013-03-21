package com.retwis.service;

import java.util.List;
import java.util.Set;

import com.retwis.User;
import redis.clients.jedis.Jedis;
import java.util.Map;

/**
 * 
 * @author yongboy
 * @date 2011-4-4
 * @version 1.0
 */
public interface IUserService {

	void save(Jedis jedis,User user);

	User load(Jedis jedis,long id);

	long loadIdByName(Jedis jedis,String userName);

	User loadByName(Jedis jedis,String userName);

	boolean checkExistByName(Jedis jedis,String userName);

	User checkLogin(Jedis jedis,String username, String password);

	void init(Jedis jedis);

	Set<String> getFollowed(Jedis jedis,long userId,int page);

	Set<String> getFollowing(Jedis jedis,long userId,int page);

	List<String> getNewUsers(Jedis jedis,int page);

        public List<String> getSubContent(Jedis jedis,int userId, int page);

	boolean checkFollowing(Jedis jedis,long currUserId, long targeUserId);

	void addFollowing(Jedis jedis,Long currUserId, Long followeeUserId);

        void addFollowing(Jedis jedis,Integer currUserId, Integer followeeUserId);

	void addFollowing(Jedis jedis,String currUserName, String targetUserName);

	void removeFollowing(Jedis jedis,String currUserName, String targetUserName);

        void removeFollowing(Jedis jedis,Integer currUserName, Integer targetUserName);

        public void addMention(Jedis jedis,String mentionsId, String toString);

        public void addMention(Jedis jedis,Integer targetUserId, Integer cid);

        public void save(Jedis jedis,String username, Integer userid);

        public int getFollowingCount(Jedis jedis,String userId);

        public int getFollowedCount(Jedis jedis,String userId);

        public int getFollowingCount(Jedis jedis,Integer userId);

        public int getFollowedCount(Jedis jedis,Integer userId);

        public int unreadMentionCount(Jedis jedis,Integer id);

        public long incrUnreadMentionCount(Jedis jedis,long id);

        public void clearUnreadMentionCount(Jedis jedis,Integer userId);

        public void PublishContent(Jedis jedis,String contentId, String publishuserId);

        public void PublishContent(Jedis jedis,Integer contentId, Integer publishuserId);

        public long getSubContentCount(Jedis jedis,Integer id);


        public Set<String> SimilarFollowing(Jedis jedis,String targetuserId);
        
        public Set<String> IndirectFollowing(Jedis jedis,String userId, String targetuserId);
        
        public Set<String> CommomFollowing(Jedis jedis,String userId, String targetuserId);


        public void clearUnreadContentCount(Jedis jedis,Integer userId);

        public int unreadContentCount(Jedis jedis,Integer id);

        public void addMention(Jedis jedis,Long targetUserId, String s2);

        public List<Boolean> getUserCheck(Jedis jedis,int userId,Set<String> ids);

        public List<Boolean> getUserCheck(Jedis jedis,int userId,List<String> idlist);

        public void addAccessLog(Jedis jedis,Integer accessuserId, Integer hostId);
        


        public Map getAccessLog(Jedis jedis,Integer userId);

        public long getAccessCount(Jedis jedis,Integer hostId);
         
        public int getNewFollowed(Jedis jedis,String userId);

        public int getNewFollowed(Jedis jedis,Integer userId);

        public List<String> getNewFollowedList(Jedis jedis,int userId, int page);

        public void clearNewFollowed(Jedis jedis,Integer userId);

        public void register(Jedis jedis,String username, Integer id);

        public Set<String> InterestingFollowing(Jedis jedis,String toString);

        public String loadNameById(Jedis jedis,String id);

        public Map unread(Jedis jedis,Integer id);

        public Map setUser(Jedis jedis , Map m);

        public List<String> getNewUser(Jedis jedis,Integer page);

        public Set<String> getHotUser(Jedis jedis,Integer page);

        public void rename(Jedis jedis,String name , String newname, Integer userid);
        
        //用户中心投稿信息统计
        public void changeContributeCount(Jedis jedis, Integer id, boolean isIncrement);
        public void changeContributeViews(Jedis jedis, Integer id, boolean isIncrement);
        public void changeContributeStows(Jedis jedis, Integer id, boolean isIncrement);
        public void changeContributeComments(Jedis jedis, Integer id, boolean isIncrement);
        
        public Long getContributeCount(Jedis jedis, Integer id);
        public Long getContributeViews(Jedis jedis, Integer id);
        public Long getContributeStows(Jedis jedis, Integer id);
        public Long getContributeComments(Jedis jedis, Integer id);
        //同步数据
        public void SyncContributeInfo(Jedis jedis, Integer id, Integer count, Integer stows, Integer views, Integer comments);
        //删除投稿信息同步
        public void DeleteContrbuteCountProcess(Jedis jedis, Integer id, Integer stows, Integer views, Integer comments);
        
}
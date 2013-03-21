package com.retwis.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import com.retwis.util.JedisProxy;
import com.retwis.User;
import com.retwis.service.base.BaseServiceImpl;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ZParams;

/**
 * 
 * @author yongboy
 * @date 2011-4-4
 * @version 1.0
 */
public class UserServiceImpl extends BaseServiceImpl<User> implements
		IUserService {

        private static final String GLOBAL_USER_ID = "global:nextUserId";
	private static final String USER_ID_FORMAT = "user:id:%s";
        private static final String USER_ID_NAME_FORMAT = "user:id:%s:name";
	private static final String USER_NAME_FORMAT = "user:name:%s";
	private static final String FOLLOWED_SET_FORMAT = "user:id:%s:followed:set";
	private static final String FOLLOWING_SET_FORMAT = "user:id:%s:following:set";
	private static final String FOLLOWED_ZSET_FORMAT = "user:id:%s:followed:zset";
	private static final String FOLLOWING_ZSET_FORMAT = "user:id:%s:following:zset";
        private static final String FOLLOWED_COUNT_FORMAT = "user:id:%s:followed:count";
	private static final String FOLLOWING_COUNT_FORMAT = "user:id:%s:following:count";
        private static final String USER_LIST = "user:list";
        private static final String USER_ZSET = "user:zset";
        private static final double DEFAULT_SCORE = 1;
        private static final String CLEAR_COUNT = "0";
        private static final String UNREAD_MENTION_COUNT = "user:id:%s:unreadmentioncount";
        private static final String UNREAD_CONTENT_COUNT = "user:id:%s:unreadcontentcount";
        private static final String SUB_CONTENT = "user:id:%s:subcontent";

        private static final String SIMILAR_FOLLOWING = "user:id:%s:similarfollowing";
        private static final String SIMILAR_FOLLOWING_CACHE = "user:id:%s:similarfollowing:cache";
        private static final String INTERESTING_FOLLOWING = "user:id:%s:interestingfollowing";
        private static final String INTERESTING_FOLLOWING_CACHE = "user:id:%s:interestingfollowing:cache";
        private static final String MENTIONS_FORMAT = "user:id:%d:mentions";
        private static final Integer DEFAULT_INIT = 0x0001574f;
        private static final String ACCESSLOG_LIST_FORMAT = "user:id:%s:accesslog:list";
        private static final String ACCESSLOG_COUNT_FORMAT = "user:id:%s:accesslog:count";
        private static final String ACCESSLOG_TIME_FORMAT = "user:id:%s:accesslog:time:accessuser:id:%s";
        private static final String NEW_FOLLOWED = "user:id:%s:newfollowed";
        private static final String NEW_FOLLOWED_LIST = "user:id:%s:newfollowed:list";
        
        private static final String USER_CONTRIBUTE_COUNT = "user:id:%s:contribute:count";
        private static final String USER_CONTRIBUTE_STOWS_COUNT = "user:id:%s:contribute:stow:count";
        private static final String USER_CONTRIBUTE_COMMENTS_COUNT = "user:id:%s:contribute:comments:count";
        private static final String USER_CONTRIBUTE_VIEWS_COUNT = "user:id:%s:contribute:views:count";

        public Map setUser(Jedis jedis , Map m){
            
            List<String> l = getNewUser(jedis,1);
            List newuserlist = new ArrayList();

            for(String s:l){
                String name = loadNameById(jedis,s);
                Map map = new HashMap();
                map.put("name",name);
                
                newuserlist.add(map);
            }
            Set<String> set = getHotUser(jedis,1);
            List hotuserlist = new ArrayList();

            for(String s:set){
                String name = loadNameById(jedis,s);
                String totalcount = getStr(jedis,String.format(FOLLOWED_COUNT_FORMAT,s));
                Map map = new HashMap();
                map.put("name",name);
                map.put("totalcount",totalcount);
                hotuserlist.add(map);
            }
            m.put("hottopiclist", hotuserlist);
            m.put("newtopiclist", newuserlist);
            return m;
        }

        public Object getUser(Jedis jedis,String type, Integer userid,Integer page,Integer pagesize){
            if (page < 1)
                page = 1;
            Integer start = (page-1)*pagesize;
            Integer end = page*pagesize-1;
            if(type==USER_ZSET){
                return jedis.zrevrangeByScore(type,"+inf", "-inf", start, end);
            }else{
                return jedis.lrange(String.format(type, userid), start, end);
            }
        }
        
        public List<String> getNewUser(Jedis jedis,Integer page){
            return (List<String>)getUser(jedis,USER_LIST,null,page,10);
        }

        public Set<String> getHotUser(Jedis jedis,Integer page){
            return (Set<String>)getUser(jedis,USER_ZSET,null,page,10);
        }

	public void save(Jedis jedis,User user) {
		user.setId(getNextUid(jedis));
		//user.setPass(MD5.encode(user.getPass()));
		super.save(jedis,getId(user.getId()), user);

		super.saveStr(jedis,getFormatKeyStr(USER_NAME_FORMAT, user.getName()),
				Long.toString(user.getId()));

		super.addHeadList(jedis,USER_LIST, Long.toString(user.getId()));
	}

	public User load(Jedis jedis,long id) {
		return super.get(jedis,getId(id));
	}

	private String getId(long id) {
		return getFormatKeyStr(USER_ID_FORMAT, id);
	}

	private String getName(String name) {
		return getFormatKeyStr(USER_NAME_FORMAT, name);
	}

	private long getNextUid(Jedis jedis) {
		return incr(jedis,GLOBAL_USER_ID);
	}

	public void init(Jedis jedis) {
		String currUserId = getStr(jedis,GLOBAL_USER_ID);
		long currUid = 0L;
		if (currUserId == null || currUserId.length() == 0)
			currUid = -1L;

		if (currUid < 0L) {//BUG
			saveStr(jedis,GLOBAL_USER_ID, Long.toString(1000L));
		}
	}

	public long loadIdByName(Jedis jedis,String userName) {
		String id = getStr(jedis,getName(userName));

		if (id == null || id.equals(""))
			return -1L;

		return Long.valueOf(id);
	}

        public String loadNameById(Jedis jedis,String id) {
		String name = getStr(jedis,String.format(USER_ID_NAME_FORMAT,id));

		if (name == null || name.equals(""))
			return null;

		return name;
	}

	public boolean checkExistByName(Jedis jedis,String userName) {
		return loadIdByName(jedis,userName) > 0L;
	}

	public User checkLogin(Jedis jedis,String username, String password) {
		long userId = loadIdByName(jedis,username);

		if (userId < 1L)
			return null;

		return this.load(jedis,userId);
	}

        public void addFollowing(Jedis jedis,Integer UserId, Integer followingUserId){
            addFollowing(jedis,UserId.longValue(),followingUserId.longValue());
        }

	public void addFollowing(Jedis jedis,Long UserId, Long followingUserId) {
		if (UserId.equals(followingUserId) || jedis.scard(String.format(FOLLOWING_SET_FORMAT, UserId))>=1024)
			return;
                if(jedis.sismember(getFormatKeyStr(FOLLOWING_SET_FORMAT, UserId),
				followingUserId.toString())){
                }else{
                    jedis.sadd(getFormatKeyStr(FOLLOWING_SET_FORMAT, UserId),
                                    followingUserId.toString());
                    jedis.zadd(getFormatKeyStr(FOLLOWING_ZSET_FORMAT, UserId),
                                    DEFAULT_SCORE,followingUserId.toString());
                    jedis.incr(getFormatKeyStr(FOLLOWING_COUNT_FORMAT, UserId));

                    jedis.sadd(getFormatKeyStr(FOLLOWED_SET_FORMAT, followingUserId),
                                    UserId.toString());
                    jedis.zadd(getFormatKeyStr(FOLLOWED_ZSET_FORMAT, followingUserId),
                                    DEFAULT_SCORE,UserId.toString());
                    jedis.incr(getFormatKeyStr(FOLLOWED_COUNT_FORMAT, followingUserId));

                    incr(jedis,String.format(NEW_FOLLOWED,followingUserId));

                    jedis.zincrby(USER_ZSET, 1, followingUserId.toString());

                    jedis.lpush(String.format(NEW_FOLLOWED_LIST, followingUserId),UserId.toString());
                }
            
                
            
        }

	public Set<String> getFollowed(Jedis jedis,long userId,int page) {
		return getFollowUserIds(jedis,FOLLOWED_ZSET_FORMAT, userId,page);
	}

	public Set<String> getFollowing(Jedis jedis,long userId,int page) {
                saveStr(jedis,String.format(NEW_FOLLOWED,userId),CLEAR_COUNT);
		return getFollowUserIds(jedis,FOLLOWING_ZSET_FORMAT, userId,page);
	}

        public List<Boolean> getUserCheck(Jedis jedis,int userId,Set<String> ids){
            List<Boolean> checks = new ArrayList();
            
            for(String id:ids){
                if(id==null){
                    checks.add(null);
                }else{
                    Boolean b = jedis.sismember(getFormatKeyStr(FOLLOWING_SET_FORMAT, userId), id);
                    checks.add(b);
                }
            }
            
            return checks;
        }

        public List<Boolean> getUserCheck(Jedis jedis,int userId,List<String> idlist){
            List<Boolean> checks = new ArrayList();

            for(String id:idlist){
                if(id==null){
                    checks.add(null);
                }else{
                    Boolean b = jedis.sismember(getFormatKeyStr(FOLLOWING_SET_FORMAT, userId), id);
                    checks.add(b);
                }
            }

            return checks;
        }

	public Set<String> getFollowUserIds(Jedis jedis,String targetId, long userId,int page) {
                if (page < 1)
			page = 1;

		int startIndex = (page - 1) * 10;
		int endIndex = page * 10-1;

		//Set<String> idSet = super.jedis
				//.zrange(getFormatKeyStr(MENTIONS_FORMAT, targetId), startIndex, endIndex);
		//return idList;
		String followedId = getFormatKeyStr(targetId, userId);

		Set<String> idSet = SortSetRange(jedis,followedId, startIndex, endIndex);

		
		return idSet;

	}

	public List<String> getNewUsers(Jedis jedis,int page) {
		int start = (page - 1) * 10;
		int end = page * 10-1;
		List<String> idList = ListRange(jedis,USER_LIST, start, end);

		if (idList.isEmpty())
			return idList;

		List<String> nameSet = new ArrayList<String>(idList.size());
		for (String uid : idList) {
			User user = load(jedis,Long.valueOf(uid));

			if (user == null)
				continue;

			nameSet.add(user.getName());
		}

		return nameSet;
	}

	public User loadByName(Jedis jedis,String userName) {
		long userId = loadIdByName(jedis,userName);

		if (userId < 1L)
			return null;

		return load(jedis,userId);
	}

	public boolean checkFollowing(Jedis jedis,long currUserId, long targeUserId) {
		return isSetMember(jedis,
				getFormatKeyStr(FOLLOWING_SET_FORMAT, currUserId),
				Long.toString(targeUserId));
	}

	public void addFollowing(Jedis jedis,String currUserName, String targetUserName) {
		if (currUserName == null || targetUserName == null
				|| currUserName.equals(targetUserName))
			return;

		Long currUserId = loadIdByName(jedis,currUserName);
		Long targetUserId = loadIdByName(jedis,targetUserName);

		if (currUserId < 1L || targetUserId < 1L)
			return;
                //incr(String.format(NEW_FOLLOWED,targetUserId));
		addFollowing(jedis,currUserId, targetUserId);
	}

	public void removeFollowing(Jedis jedis,String currUserName, String targetUserName) {
		if (currUserName == null || targetUserName == null
				|| currUserName.equals(targetUserName))
			return;

		long currUserId = loadIdByName(jedis,currUserName);
		long targetUserId = loadIdByName(jedis,targetUserName);

		if (currUserId < 1L || targetUserId < 1L)
			return;

		this.removeFollowing(jedis,currUserId, targetUserId);
	}

        public void removeFollowing(Jedis jedis,Integer currUserId, Integer targetUserId){
            this.removeFollowing(jedis,currUserId.longValue(), targetUserId.longValue());
        }

        public void removeFollowing(Jedis jedis,Long currUserId, Long targetUserId){
            

            if(jedis.sismember(getFormatKeyStr(FOLLOWING_SET_FORMAT, currUserId),
				Long.toString(targetUserId))){
                
                jedis.srem(getFormatKeyStr(FOLLOWING_SET_FORMAT, currUserId),
                                    Long.toString(targetUserId));
                jedis.zrem(getFormatKeyStr(FOLLOWING_ZSET_FORMAT, currUserId),
                                    Long.toString(targetUserId));
                jedis.decr(getFormatKeyStr(FOLLOWING_COUNT_FORMAT, currUserId));
                jedis.srem(getFormatKeyStr(FOLLOWED_SET_FORMAT, targetUserId),
                                    Long.toString(currUserId));
                jedis.zrem(getFormatKeyStr(FOLLOWED_ZSET_FORMAT, targetUserId),
                                    Long.toString(currUserId));
                jedis.decr(getFormatKeyStr(FOLLOWED_COUNT_FORMAT, targetUserId));

                if(getStr(jedis,String.format(NEW_FOLLOWED, targetUserId))!=null && Integer.parseInt( getStr(jedis,String.format(NEW_FOLLOWED, targetUserId)))>0 ){
                     jedis.decr(String.format(NEW_FOLLOWED,targetUserId));
                }
                jedis.zincrby(USER_ZSET, -1, Long.toString(currUserId));

                jedis.lrem(String.format(NEW_FOLLOWED_LIST, targetUserId),1,currUserId.toString());
            }
            
        }

        public int getFollowingCount(Jedis jedis,String userId){
            String s = getStr(jedis,getFormatKeyStr(FOLLOWING_COUNT_FORMAT, userId));
            if(s==null){
                return 0;
            }
            return Integer.parseInt(s);
        }

        public int getFollowingCount(Jedis jedis,Integer userId){
            return getFollowingCount(jedis,userId.toString());
        }

        public int getFollowedCount(Jedis jedis,String userId){
            String s = getStr(jedis,getFormatKeyStr(FOLLOWED_COUNT_FORMAT, userId));
            if(s==null){
                return 0;
            }
            return Integer.parseInt(s);
        }

        public int getFollowedCount(Jedis jedis,Integer userId){
            return getFollowedCount(jedis,userId.toString());
        }

        public int getNewFollowed(Jedis jedis,String userId){
            String s = getStr(jedis,String.format(NEW_FOLLOWED, userId));
            if(s==null){
                return 0;
            }
            return Integer.parseInt(s);
        }

        public int getNewFollowed(Jedis jedis,Integer userId){
            return getNewFollowed(jedis,userId.toString());
        }
	


    public void addMention(Jedis jedis,String mentionId, String toString) {
        addHeadList(jedis,mentionId, toString);
    }

    public void addMention(Jedis jedis,Long targetUserId, String cid){
        
        jedis.lpush(getFormatKeyStr(MENTIONS_FORMAT,targetUserId), cid);
        jedis.incr(getFormatKeyStr(UNREAD_MENTION_COUNT, targetUserId));
        
    }

    public void addMention(Jedis jedis,Integer targetUserId, Integer cid){
        
        jedis.lpush(getFormatKeyStr(MENTIONS_FORMAT,targetUserId), cid.toString());
        jedis.incr(getFormatKeyStr(UNREAD_MENTION_COUNT, targetUserId));
        
    }

    public void addSubContent(Jedis jedis,String contentId, String toString) {
        addHeadList(jedis,contentId, toString);
    }

    public void PublishContent(Jedis jedis,Integer contentId, Integer publishuserId){
        PublishContent(jedis,contentId.toString(),publishuserId.toString());
    }

    public void PublishContent(Jedis jedis,String contentId, String publishuserId) {
        
        Pipeline p = jedis.pipelined();
        
        Set<String> ids = jedis.smembers(getFormatKeyStr(FOLLOWED_SET_FORMAT,publishuserId));
        for(String id:ids){
            p.incr(getFormatKeyStr(UNREAD_CONTENT_COUNT,id));
            p.lpush(getFormatKeyStr(SUB_CONTENT,id), contentId);
        }
        
    }

    public List<String> getSubContent(Jedis jedis,int userId, int page){
                if (page < 1)
			page = 1;
		int startIndex = (page - 1) * 10;
		int endIndex = page * 10-1;
		String followedId = getFormatKeyStr(SUB_CONTENT, userId);
		//List<String> idlist = ListRange(followedId, startIndex, endIndex);                
                List<String> values = jedis.lrange(followedId, startIndex, endIndex);
                return values;
		//return idlist;
    }

    public List<String> getNewFollowedList(Jedis jedis,int userId, int page){
                if (page < 1)
			page = 1;
		int startIndex = (page - 1) * 10;
		int endIndex = page * 10-1;
		String followedId = getFormatKeyStr(NEW_FOLLOWED_LIST, userId);
		//List<String> idlist = ListRange(followedId, startIndex, endIndex);
                List<String> values = jedis.lrange(followedId, startIndex, endIndex);
                return values;
		//return idlist;
    }

    public List<String> getSubuser(Jedis jedis,int userId, int page){
                if (page < 1)
			page = 1;
		int startIndex = (page - 1) * 10;
		int endIndex = page * 10-1;
		String followedId = getFormatKeyStr(SUB_CONTENT, userId);
		//List<String> idlist = ListRange(followedId, startIndex, endIndex);
                List<String> values = jedis.lrange(followedId, startIndex, endIndex);
                return values;
		//return idlist;
    }

//共同关注了谁
    public Set<String> CommomFollowing(Jedis jedis,String userId, String targetuserId) {
        
        Set<String> s = jedis.sinter(getFormatKeyStr(FOLLOWING_SET_FORMAT,userId),
                getFormatKeyStr(FOLLOWING_SET_FORMAT,targetuserId));
        
        return s;
    }
//关注他的人也关注了谁
    public Set<String> SimilarFollowing(Jedis jedis,String targetuserId) {
        if(jedis.ttl(getFormatKeyStr(SIMILAR_FOLLOWING_CACHE,targetuserId)) <= -1){
            jedis.set(getFormatKeyStr(SIMILAR_FOLLOWING_CACHE,targetuserId), "1");
            jedis.expire(getFormatKeyStr(SIMILAR_FOLLOWING_CACHE,targetuserId), 600);
            Set<String> s = jedis.smembers(getFormatKeyStr(FOLLOWED_SET_FORMAT,targetuserId));
            //int[] i = {2,2};
            if(s.isEmpty()){
                return null;
            }
            ZParams z = new ZParams();
            String[] str = new String[s.size()];
            Iterator it = s.iterator();
            for(int i = 0;i<str.length;i++){
                str[i]= getFormatKeyStr(FOLLOWING_ZSET_FORMAT,it.next());
            }
            long l = jedis.zunionstore(getFormatKeyStr(SIMILAR_FOLLOWING,targetuserId), z, str);
            jedis.zrem(getFormatKeyStr(SIMILAR_FOLLOWING,targetuserId), targetuserId);
            
        }
        
        Set<String> s2 = jedis.zrevrangeByScore(getFormatKeyStr(SIMILAR_FOLLOWING,targetuserId), "+inf", "3", 0, 6);
        
        //s2.remove(targetuserId);
        return s2;
    }

    //你关注的人同时关注了
    public Set<String> InterestingFollowing(Jedis jedis,String targetuserId) {
        if(jedis.ttl(getFormatKeyStr(INTERESTING_FOLLOWING_CACHE,targetuserId)) <= -1){
            jedis.set(getFormatKeyStr(INTERESTING_FOLLOWING_CACHE,targetuserId), "1");
            jedis.expire(getFormatKeyStr(INTERESTING_FOLLOWING_CACHE,targetuserId), 600);
            Set<String> s = jedis.smembers(getFormatKeyStr(FOLLOWING_SET_FORMAT,targetuserId));
            //int[] i = {2,2};
            if(s.isEmpty()){
                return null;
            }
            ZParams z = new ZParams();
            String[] str = new String[s.size()];
            Iterator it = s.iterator();
            for(int i = 0;i<str.length;i++){
                str[i]= getFormatKeyStr(FOLLOWING_ZSET_FORMAT,it.next());
            }
            long l = jedis.zunionstore(getFormatKeyStr(INTERESTING_FOLLOWING,targetuserId), z, str);

            z.weights(1,0);
            z.aggregate(ZParams.Aggregate.MIN);
            jedis.zunionstore(getFormatKeyStr(INTERESTING_FOLLOWING,targetuserId), z, getFormatKeyStr(INTERESTING_FOLLOWING,targetuserId),getFormatKeyStr(FOLLOWING_ZSET_FORMAT,targetuserId));
            jedis.zrem(getFormatKeyStr(INTERESTING_FOLLOWING,targetuserId), targetuserId);
            
        }


        Set<String> s2 = jedis.zrevrangeByScore(getFormatKeyStr(INTERESTING_FOLLOWING,targetuserId), "+inf", "3", 0, 6);
        
        //s2.remove(targetuserId);
        return s2;
    }

    //你关注的人中关注他的人是谁（间接关注）这些人也收听了他
    public Set<String> IndirectFollowing(Jedis jedis,String userId, String targetuserId) {
        
        Set<String> s = jedis.sinter(getFormatKeyStr(FOLLOWING_SET_FORMAT,userId),
                getFormatKeyStr(FOLLOWED_SET_FORMAT,targetuserId));
        
        //s.remove(userId);
        return s;
    }

    public void save(Jedis jedis,String username, Integer userid) {
        saveStr(jedis,getFormatKeyStr(USER_NAME_FORMAT, username),userid.toString());
        saveStr(jedis,getFormatKeyStr(USER_ID_NAME_FORMAT, userid),username);
    }

    public void register(Jedis jedis,String username, Integer userid) {
        save(jedis,username,userid);
        jedis.lpush(USER_LIST, userid.toString());
        jedis.zadd(USER_ZSET, 0, userid.toString());
        addFollowing(jedis,userid,DEFAULT_INIT);
    }

    public void rename(Jedis jedis,String name , String newname, Integer userid){
        jedis.rename(String.format(USER_NAME_FORMAT,name),String.format(USER_NAME_FORMAT, newname));
        jedis.set(String.format(USER_ID_NAME_FORMAT,userid),newname);
    }

    public Map unread(Jedis jedis,Integer id) {
        Map m = new HashMap();
        m.put("mention", unreadMentionCount(jedis,id));
        m.put("content", unreadContentCount(jedis,id));
        m.put("newfollowed", getNewFollowed(jedis,id));
        return m;
    }

    public int unreadMentionCount(Jedis jedis,Integer id) {
        String s = getFormatKeyStr(UNREAD_MENTION_COUNT,id);
        String ss = getStr(jedis,s);
        if(ss==null){
            ss = "0";
        }
        Integer i = Integer.parseInt(ss);
        if(i==null||i<0){
            i=0;
        }
        return i;
    }

    public int unreadContentCount(Jedis jedis,Integer id) {
        String s = getFormatKeyStr(UNREAD_CONTENT_COUNT,id);
        String ss = getStr(jedis,s);
        if(ss==null){
            ss = "0";
        }
        Integer i = Integer.parseInt(ss);
        if(i==null||i<0){
            i=0;
        }
        return i;
    }

    public long incrUnreadMentionCount(Jedis jedis,long id) {
        String s = getFormatKeyStr(UNREAD_MENTION_COUNT, id);
        return incr(jedis,s);
        //this.incr("user:id:87887:unreadmentioncount");
    }

    public void clearUnreadMentionCount(Jedis jedis,Integer userId) {
        saveStr(jedis,getFormatKeyStr(UNREAD_MENTION_COUNT, userId), CLEAR_COUNT);
    }

    public void clearNewFollowed(Jedis jedis,Integer userId) {
        saveStr(jedis,getFormatKeyStr(NEW_FOLLOWED, userId), CLEAR_COUNT);
    }

    public void clearUnreadContentCount(Jedis jedis,Integer userId) {
        saveStr(jedis,getFormatKeyStr(UNREAD_CONTENT_COUNT, userId), CLEAR_COUNT);
    }

    public long getSubContentCount(Jedis jedis,Integer id) {
        return getListLength(jedis,getFormatKeyStr(SUB_CONTENT,id));
    }
    
//获得空间来访
    /*
    public List<String> getAccessLog(Jedis jedis,Integer userId) {
        List<String> list = jedis.lrange(getFormatKeyStr(ACCESSLOG_LIST_FORMAT, userId), 0, -1);
        if(list!=null && list.isEmpty()){
            
            return null;
        }
        List<String> accessloglist = new ArrayList<String>();
        for(String a:list){
            accessloglist.add(a+":"+jedis.get(getFormatKeyStr(ACCESSLOG_TIME_FORMAT, userId,a)));
        }
        
        return accessloglist;
        
    }
     * 
     */

    public Map getAccessLog(Jedis jedis,Integer userId) {

        List<String> list = jedis.lrange(getFormatKeyStr(ACCESSLOG_LIST_FORMAT, userId), 0, -1);
        if(list!=null && list.isEmpty()){

            return null;
        }
        Map<String,String> map = new HashMap();
        for(String a:list){
            map.put(a, jedis.get(getFormatKeyStr(ACCESSLOG_TIME_FORMAT, userId,a)));
        }
        
        return map;

    }

//空间来访记录

    public void addAccessLog(Jedis jedis,Integer accessuserId, Integer hostId) {

        Long time = System.currentTimeMillis();
        //访问数+1
        jedis.incr(getFormatKeyStr(ACCESSLOG_COUNT_FORMAT, hostId));
        //添加来访者信息  
        Long accessexist = jedis.lrem(getFormatKeyStr(ACCESSLOG_LIST_FORMAT, hostId), 0, accessuserId.toString());
        Long len = jedis.llen(getFormatKeyStr(ACCESSLOG_LIST_FORMAT,hostId));
        if(accessexist != 0 ){
                    jedis.lpush(getFormatKeyStr(ACCESSLOG_LIST_FORMAT,hostId),accessuserId.toString());
                    jedis.mset(getFormatKeyStr(ACCESSLOG_TIME_FORMAT, hostId,accessuserId), time.toString());
        }
        else if(len >= 10){
                    //处理accessloglist
                    String deluser = jedis.rpop(getFormatKeyStr(ACCESSLOG_LIST_FORMAT,hostId));
                    jedis.lpush(getFormatKeyStr(ACCESSLOG_LIST_FORMAT,hostId),accessuserId.toString());
                    //处理accesslog time key
                    if(deluser != null){
                    jedis.del(getFormatKeyStr(ACCESSLOG_TIME_FORMAT, hostId,deluser));
                    jedis.mset(getFormatKeyStr(ACCESSLOG_TIME_FORMAT, hostId,accessuserId), time.toString());
                    }
        }else {
                    jedis.lpush(getFormatKeyStr(ACCESSLOG_LIST_FORMAT,hostId),accessuserId.toString());
                    jedis.mset(getFormatKeyStr(ACCESSLOG_TIME_FORMAT, hostId,accessuserId), time.toString());
        }
    }

    public long getAccessCount(Jedis jedis,Integer hostId){
            String s = getStr(jedis,getFormatKeyStr(ACCESSLOG_COUNT_FORMAT, hostId));
            if(s==null){
                return 0;
            }
            return Long.parseLong(s);
        }
    
        //用户中心投稿信息统计
        public void changeContributeCount(Jedis jedis, Integer id, boolean isIncrement){
            if(isIncrement){
                jedis.incr(getFormatKeyStr(USER_CONTRIBUTE_COUNT,id));
            }
            else{
                jedis.decr(getFormatKeyStr(USER_CONTRIBUTE_COUNT,id));
            }
        }
        public void changeContributeViews(Jedis jedis, Integer id, boolean isIncrement){
            if(isIncrement){
                jedis.incr(getFormatKeyStr(USER_CONTRIBUTE_VIEWS_COUNT, id));
            }
            else{
                jedis.decr(getFormatKeyStr(USER_CONTRIBUTE_VIEWS_COUNT, id));
            }
        }
        public void changeContributeStows(Jedis jedis, Integer id, boolean isIncrement){
            if(isIncrement){
                jedis.incr(getFormatKeyStr(USER_CONTRIBUTE_STOWS_COUNT, id));
            }
            else{
                jedis.decr(getFormatKeyStr(USER_CONTRIBUTE_STOWS_COUNT, id));
            }
        }
        public void changeContributeComments(Jedis jedis, Integer id, boolean isIncrement){
            if(isIncrement){
                jedis.incr(getFormatKeyStr(USER_CONTRIBUTE_COMMENTS_COUNT, id));
            }
            else{
                jedis.decr(getFormatKeyStr(USER_CONTRIBUTE_COMMENTS_COUNT, id));
            }
        }
        
        public Long getContributeCount(Jedis jedis, Integer id){
            String s = jedis.get(getFormatKeyStr(USER_CONTRIBUTE_COUNT, id));
            return s == null? 0:Long.parseLong(s);
        }
        public Long getContributeViews(Jedis jedis, Integer id){
            String s = jedis.get(getFormatKeyStr(USER_CONTRIBUTE_VIEWS_COUNT, id));
            return s == null? 0:Long.parseLong(s);
        }
        public Long getContributeStows(Jedis jedis, Integer id){
            String s = jedis.get(getFormatKeyStr(USER_CONTRIBUTE_STOWS_COUNT, id));
            return s == null? 0:Long.parseLong(s);
        }
        public Long getContributeComments(Jedis jedis, Integer id){
            String s = jedis.get(getFormatKeyStr(USER_CONTRIBUTE_COMMENTS_COUNT, id));
            return s == null? 0:Long.parseLong(s);
        }
        //同步数据
        public void SyncContributeInfo(Jedis jedis, Integer id, Integer count, Integer stows, Integer views, Integer comments){
            if(count!=0){
                jedis.mset(getFormatKeyStr(USER_CONTRIBUTE_COUNT, id), count.toString());
            }
            if(stows!=0){
                jedis.mset(getFormatKeyStr(USER_CONTRIBUTE_STOWS_COUNT, id), stows.toString());
            }
            if(comments!=0){
                jedis.mset(getFormatKeyStr(USER_CONTRIBUTE_COMMENTS_COUNT, id), comments.toString());
            }
            if(views!=0){
                jedis.mset(getFormatKeyStr(USER_CONTRIBUTE_VIEWS_COUNT, id), views.toString());
            }
        }
        public void DeleteContrbuteCountProcess(Jedis jedis, Integer id, Integer stows, Integer views, Integer comments){
            jedis.decr(getFormatKeyStr(USER_CONTRIBUTE_COUNT, id));
            jedis.decrBy(getFormatKeyStr(USER_CONTRIBUTE_STOWS_COUNT, id), stows);
            jedis.decrBy(getFormatKeyStr(USER_CONTRIBUTE_COMMENTS_COUNT, id), comments);
            jedis.decrBy(getFormatKeyStr(USER_CONTRIBUTE_VIEWS_COUNT, id), views);
        }
}
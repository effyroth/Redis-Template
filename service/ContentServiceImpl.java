/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.retwis.service;

import com.retwis.service.base.BaseServiceImpl;
import java.util.Map;
import redis.clients.jedis.Jedis;

/**
 *
 * @author siyu
 */
public class ContentServiceImpl extends BaseServiceImpl implements IContentService{
    //随机视频
    private static String RANDOM_CONTENT_SET = "recommendtype:id:%s:content:id:set";
    //免审
    private static String UNCHECKED_USER_SET = "uncheckeduser:id:set";
    private static String UNCHECKED_USER_CHANNEL = "uncheckeduser:id:%s:channel:set";
    //自动补全
    private static String CONTENT_ID_TITLE = "content:for:auto:id:%s";

    @Override
    public Long addRecommendContent(Jedis jedis, Integer contentId, Integer recommendTypeId) {
        return jedis.sadd(getFormatKeyStr(RANDOM_CONTENT_SET, recommendTypeId), contentId.toString());
    }

    @Override
    public void removeContent(Jedis jedis, Integer contentId, Integer recommendTypeId) {
        jedis.srem(getFormatKeyStr(RANDOM_CONTENT_SET, recommendTypeId), contentId.toString());
    }

    @Override
    public Long getRandomContent(Jedis jedis, Integer recommendTypeId) {
        return Long.parseLong(jedis.srandmember(getFormatKeyStr(RANDOM_CONTENT_SET, recommendTypeId)));
    }
    
    @Override
    public void addUncheckedUser(Jedis jedis, Integer userId, Integer channelId) {
        jedis.sadd(getFormatKeyStr(UNCHECKED_USER_SET), userId.toString());
        jedis.sadd(getFormatKeyStr(UNCHECKED_USER_CHANNEL, userId), channelId.toString());
    }
    
    @Override
    public boolean isUncheckedUser(Jedis jedis, Integer userId, Integer channelId) {
        if(jedis.sismember(getFormatKeyStr(UNCHECKED_USER_SET),userId.toString()) 
                && jedis.sismember(getFormatKeyStr(UNCHECKED_USER_CHANNEL,userId), channelId.toString())){
                return true;
        }
        return false;
    }
    
    @Override
    public void deleteUncheckedUser(Jedis jedis, Integer userId, Integer channelId) {
        if(this.isUncheckedUser(jedis, userId, channelId)){
            jedis.srem(getFormatKeyStr(UNCHECKED_USER_CHANNEL, userId.toString()), channelId.toString());
            jedis.srem(getFormatKeyStr(UNCHECKED_USER_SET), userId.toString());
        }
    }
    
        //自动补完
        public void addContent(Jedis jedis, Integer contentId, String contentTitle){
                jedis.mset(getFormatKeyStr(CONTENT_ID_TITLE, contentId),contentTitle);
        }
        
        public void inputContentForScript(Jedis jedis, Map map){
                if(map.isEmpty()){
                    return;
                }
                for(Object o:map.keySet()){
                    jedis.mset(getFormatKeyStr(CONTENT_ID_TITLE, o.toString()),map.get(o).toString());
                }
        }
        
        public void rejectContent(Jedis jedis, Integer contentId){
                jedis.del(getFormatKeyStr(CONTENT_ID_TITLE, contentId.toString()));
        }
        
        public void updateContent(Jedis jedis, Integer contentId, String contentTitle){
                jedis.set(getFormatKeyStr(contentTitle, contentId), contentTitle);
        }
        
        public String[] getContentTitle(Jedis jedis, Integer[] contentIds){
                if(contentIds.length <= 0){
                    return null;
                }
                String[] contentArr = new String[contentIds.length];
                for(int i=0;i<contentIds.length;i++){
                    contentArr[i] = jedis.get(getFormatKeyStr(CONTENT_ID_TITLE, contentIds[i]));
                }
                return contentArr;
        }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.retwis.service;

import java.util.Map;
import redis.clients.jedis.Jedis;

/**
 *
 * @author siyu
 */
public interface IContentService {
        //随机视频
        public Long addRecommendContent(Jedis jedis, Integer contentId, Integer recommendTypeId);
        
        public void removeContent(Jedis jedis, Integer contentId, Integer recommendTypeId);
        
        public Long getRandomContent(Jedis jedis, Integer recommendTypeId);
        //免审
        public boolean isUncheckedUser(Jedis jedis, Integer userId, Integer channelId);
        
        public void addUncheckedUser(Jedis jedis, Integer userId, Integer channelId);
        
        public void deleteUncheckedUser(Jedis jedis, Integer userId, Integer channelId);
        //自动补完
        public void addContent(Jedis jedis, Integer contentId, String contentTitle);
        
        public void inputContentForScript(Jedis jedis, Map map);
        
        public void rejectContent(Jedis jedis, Integer contentId);
        
        public void updateContent(Jedis jedis, Integer contentId, String contentTitle);
        
        public String[] getContentTitle(Jedis jedis, Integer[] contentIds);
}

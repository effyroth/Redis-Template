/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.retwis.service;

import com.retwis.service.base.BaseServiceImpl;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;

/**
 *
 * @author siyu
 */
public class ActionCodeServiceImpl extends BaseServiceImpl implements ActionCodeService{

    private static String ACTIONCODE_SET = "actioncode:project:%s:set";
    private static String path = "C:\\Users\\Administrator\\Desktop\\新手卡1.txt";
    
    @Override
    public void addActionCode(Jedis jedis, String actionCode, Integer project) {
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(path));
            String s = new String();
            while ((s = input.readLine()) != null) { //判断是否读到了最后一行
                String arr[] = s.split("	");
//                System.out.println(arr[1]);
                jedis.sadd(getFormatKeyStr(ACTIONCODE_SET, project), arr[1]);
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public String getActionCode(Jedis jedis, Integer project) {
        return jedis.spop(getFormatKeyStr(ACTIONCODE_SET, project));
    }
    
}

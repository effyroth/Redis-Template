/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author effyroth
 */
package com.retwis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegUtils {

	public static String getWholeValue(String source, String regStr) {
		Pattern pattern = Pattern.compile(regStr);
		Matcher matcher = pattern.matcher(source);
		if (matcher.find()) {
			return matcher.group();
		}

		return null;
	}

	public static String getValue(String source, String regStr, int index) {
		Pattern pattern = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(source);
		if (matcher.find()) {
			return matcher.group(index);
		}

		return null;
	}

	public static String getValue(String source, String regStr) {
		return getValue(source, regStr, 1);
	}

	public static List<String> getValues(String source, String regStr) {
		Pattern pattern = Pattern.compile(regStr);
		Matcher matcher = pattern.matcher(source);
		List<String> strList = new ArrayList<String>();
		if (matcher.find()) {
			int groupName = matcher.groupCount();
			for (int i = 1; i <= groupName; i++) {
				strList.add(matcher.group(i));
			}
		}

		return strList;
	}

	public static List<String> getValuesListByOne(String source, String regStr) {
		Pattern pattern = Pattern.compile(regStr);
		Matcher matcher = pattern.matcher(source);
		List<String> strList = null;
		strList = new ArrayList<String>();
		while (matcher.find()) {
			strList.add(matcher.group());
		}

		return strList;
	}

	public static List<String> getValuesListByOne(String source, String regStr,
			int groupNum) {
		Pattern pattern = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(source);
		List<String> strList = null;
		strList = new ArrayList<String>();
		while (matcher.find()) {
			strList.add(matcher.group(groupNum));
		}

		return strList;
	}
}


package com.retwis;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author yongboy
 * @date 2011-4-3
 * @version 1.0
 */
public class Status implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private String content;
	private long time = System.currentTimeMillis();
	private long uid;
	private String ip;

	private transient User user;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getSaveDate() {
		return new Date(time);
	}

	public String getTimeAgoInWords() {
		long leftTime = System.currentTimeMillis() - time;

		if (leftTime >= 0 && leftTime < 10) {
			return "just now";
		} else if (leftTime >= 10 && leftTime <= 60) {
			return "less than a minute ago";
		}

		long leftMin = leftTime / 60L;

		if (leftMin >= 0 && leftMin <= 1) {
			return "a minute ago";
		} else if (leftMin >= 2 && leftMin <= 45) {
			return leftMin + " minutes ago";
		} else if (leftMin >= 46 && leftMin <= 89) {
			return "about an hour ago";
		} else if (leftMin >= 90 && leftMin <= 1439) {
			return (leftMin / 60L) + " hours ago";
		} else if (leftMin >= 1440 && leftMin <= 2879) {
			return "about a day ago";
		} else if (leftMin >= 2880 && leftMin <= 43199) {
			return (leftMin / 1440L) + " days ago";
		} else if (leftMin >= 43200 && leftMin <= 86399) {
			return "about a month ago";
		} else if (leftMin >= 86400 && leftMin <= 525599) {
			return (leftMin / 43200L) + " months ago";
		} else if (leftMin >= 525600 && leftMin <= 1051199) {
			return "about a year ago";
		} else {
			return "over " + (leftMin / 525600L) + " years ago";
		}
	}

}
package com.retwis;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author yongboy
 * @date 2011-4-3
 * @version 1.0
 */
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private String name;
	private String pass;
	private long date = System.currentTimeMillis();

	public User() {
	}

	public User(long id, String name, String pass, long date) {
		this.id = id;
		this.name = name;
		this.pass = pass;
		this.date = date;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public Date getSaveDate() {
		return new Date(date);
	}

	public int hashCode() {
		return super.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof User))
			return false;

		return this.getId() == ((User) obj).getId();
	}

	public String toString() {
		return "User[id:" + getId() + "; name:" + getName() + "; date:"
				+ getDate() + "]";
	}
}
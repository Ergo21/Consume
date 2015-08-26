package com.ergo21.consume;

import java.io.Serializable;
import java.util.ArrayList;

public class SaveData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8079935936374119154L;

	enum Upgrade {
		MHEL, MMAN, MRR, FIRE, ICE
	};

	private String name;
	private String date;
	private ArrayList<Integer> levelsComplete;
	private ArrayList<Upgrade> upgrades;

	public SaveData(String n, String d) {
		name = n;
		date = d;
		levelsComplete = new ArrayList<Integer>();
		upgrades = new ArrayList<Upgrade>();
	}

	public String getName() {
		return name;
	}

	public String getDate() {
		return date;
	}

	public ArrayList<Integer> getCompletedLevels() {
		return levelsComplete;
	}

	public ArrayList<Upgrade> getUpgrades() {
		return upgrades;
	}

}
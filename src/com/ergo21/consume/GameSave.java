package com.ergo21.consume;

import java.io.Serializable;
import java.util.ArrayList;

import com.almasb.consume.Types.Element;

public class GameSave implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4385540615635239374L;
	
	private String name;
	protected String sSheet;

	private int maxHealth;
	private int curHealth;
	private int maxMana;
	private int curMana;
	private int manaReg;
	private Element curElement;
	private ArrayList<Element> resists;
	private ArrayList<Element> weaks;
	private ArrayList<Element> powers;
	private int currentLevel;
	private ArrayList<Integer> upgradesCollected;
	private ArrayList<Integer> levelsCompleted;
	
	public GameSave(Player p){
		name = p.getName();
		sSheet = p.getSpritesheet();
		maxHealth = p.MaxHealthProperty().get();
		curHealth = p.CurrentHealthProperty().get();
		maxMana = p.MaxManaProperty().get();
		curMana = p.CurrentManaProperty().get();
		manaReg = p.ManaRegenRateProperty().get();
		curElement = p.ElementProperty().get();
		resists = p.getResistances();
		weaks = p.getWeaknesses();
		powers = p.getPowers();
		currentLevel = p.getCurrentLevel();
		upgradesCollected = p.getUpgrades();
		levelsCompleted = p.getLevsComp();
	}
	
	public String getName(){
		return name;
	}
	
	public String getSSheet(){
		return sSheet;
	}
	
	public int getMaxHealth(){
		return maxHealth;
	}
	
	public int getCurHealth(){
		return curHealth;
	}
	
	public int getMaxMana(){
		return maxMana;
	}
	
	public int getCurMana(){
		return curMana;
	}
	
	public int getManaReg(){
		return manaReg;
	}
	
	public Element getCurElement(){
		return curElement;
	}
	
	public ArrayList<Element> getResists(){
		return resists;
	}
	
	public ArrayList<Element> getWeaks(){
		return weaks;
	}
	
	public ArrayList<Element> getPowers(){
		return powers;
	}
	
	public int getCurLevel(){
		return currentLevel;
	}

	public ArrayList<Integer> getUpgrades() {
		return upgradesCollected;
	}
	
	public ArrayList<Integer> getLevsComp() {
		return levelsCompleted;
	}
}
package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

public class Enemy{
	
	enum ETags{NONE, FIRE, ICE};
	private String name;
	private String sSheet;
	private int mHealth;
	private int cHealth;
	private int mMana;
	private int cMana;
	private int manaR;
	private ArrayList<ETags> resists;
	private ArrayList<ETags> weaks;
	
	public Enemy(List<String> prop){
		for(String p : prop){
			String val = p.substring(p.indexOf('"') + 1, p.lastIndexOf('"'));
			switch(p.substring(0, p.indexOf("="))){
				case "Name":{
					name = val;
					break;
				}
				case "Health":{
					mHealth = Integer.parseInt(val);
					cHealth = mHealth;
					break;
				}
				case "Mana":{
					mMana = Integer.parseInt(val);
					cMana = mMana;
					break;
				}
				case "ManaR":{
					manaR = Integer.parseInt(val);
					break;
				}
				case "Resists":{
					resists = getRWs(p);
					break;
				}
				case "Weak":{
					weaks = getRWs(p);
					break;
				}
				case "Spritesheet":{
					sSheet = val;
					break;
				}
			}
		}
	}

	private ArrayList<ETags> getRWs(String p) {
		ArrayList<ETags> rws = new ArrayList<ETags>();

		while(p.contains("\"")){
			p = p.substring(p.indexOf('"') + 1);
			String val = p.substring(0, p.indexOf('"'));
			p = p.substring(p.indexOf('"') + 1);
			switch(val){
				case "FIRE":{
					rws.add(ETags.FIRE);
					break;
				}
				case "ICE":{
					rws.add(ETags.ICE);
					break;
				}
			}
			
		}
		
		return rws;
	}
	
	
	public String getName(){
		return name;
	}
	public String getSpritesheet(){
		return sSheet;
	}
	public int getMaxHealth(){
		return mHealth;
	}
	public int getCurrentHealth(){
		return cHealth;
	}
	public void setCurrentHealth(int cH){
		cHealth = cH;
	}
	public int getMaxMana(){
		return mMana;
	}
	public int getCurrentMana(){
		return cMana;
	}
	public void setCurrentMana(int cM){
		cMana = cM;
	}
	public int getManaRegenRate(){
		return manaR;
	}
	public ArrayList<ETags> getResistances(){
		return resists;
	}
	public ArrayList<ETags> getWeaknesses(){
		return weaks;
	}
}
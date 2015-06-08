package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

public class Player extends Enemy{
	enum Power{NORMAL, FIRE, ICE};
	private ArrayList<Power> powers;
	private Power curPower;

	public Player(List<String> prop) {
		super(prop);
		powers.add(Power.NORMAL);
		curPower = Power.NORMAL;
	}
	
	public void setMaxHealth(int mH){
		mHealth = mH;
	}
	
	public void setMaxMana(int mM){
		mMana = mM;
	}
	
	public void setManaRegenRate(int mR){
		manaR = mR;
	}
	
	public ArrayList<Power> getPowers(){
		return powers;
	}
	
	public void setCurrentPower(Power p){
		curPower = p;
		switch(curPower){
			case NORMAL:{
				resists.clear();
				weaks.clear();
				break;
			}
			case FIRE:{
				resists.clear();
				resists.add(ETags.FIRE);
				weaks.clear();
				weaks.add(ETags.ICE);
				break;
			}
			case ICE:{
				resists.clear();
				resists.add(ETags.ICE);
				weaks.clear();
				weaks.add(ETags.FIRE);
				break;
			}
			default:{
				resists.clear();
				weaks.clear();
				break;
			}
		}
	}
	
}
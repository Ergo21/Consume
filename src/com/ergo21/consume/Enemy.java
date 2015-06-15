package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

import com.almasb.consume.Types.Element;

public class Enemy{

	protected String name;
	protected String sSheet;
	protected int mHealth;
	protected int cHealth;
	protected int mMana;
	protected int cMana;
	protected int manaR;
	protected ArrayList<Element> resists;
	protected ArrayList<Element> weaks;

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

	private ArrayList<Element> getRWs(String p) {
		ArrayList<Element> rws = new ArrayList<Element>();

		while(p.contains("\"")){
			p = p.substring(p.indexOf('"') + 1);
			String val = p.substring(0, p.indexOf('"'));
			p = p.substring(p.indexOf('"') + 1);
			switch(val){
				case "FIRE":{
					rws.add(Element.FIRE);
					break;
				}
				case "ICE":{
					rws.add(Element.ICE);
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
	public List<Element> getResistances(){
		return resists;
	}
	public List<Element> getWeaknesses(){
		return weaks;
	}

	/**
	 * Restores health of enemy/player by given value in % ratio
	 * 1.0 = 100%
	 * 0.5 = 50%
	 *
	 * Will NOT overheal the character
	 *
	 * @param percent
	 */
	public void restoreHealth(double percent) {
        int restored = (int)(mHealth * percent);
        setCurrentHealth(Math.min(mHealth, cHealth + restored));
	}

    /**
     * Restores mana of enemy/player by given value in % ratio
     * 1.0 = 100%
     * 0.5 = 50%
     *
     * Will NOT increase mana over 100%
     *
     * @param percent
     */
    public void restoreMana(double percent) {
        int restored = (int)(mMana * percent);
        setCurrentMana(Math.min(mMana, cMana + restored));
    }

    /**
     * Regen "tick", restores mana according to
     * character's mana regeneration rate
     */
    public void regenMana() {
        restoreMana(manaR / 100.0);
    }

    public void takeDamage(int value) {
        cHealth -= value;
    }

	@Override
    public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append(name).append(",").append(sSheet);
	    sb.append(",health:").append(cHealth).append("/").append(mHealth);
	    sb.append(",mana:").append(cMana).append("/").append(mMana);
	    sb.append("(").append(manaR).append("%").append(")");
	    return sb.toString();
	}
}
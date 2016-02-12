package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import com.almasb.consume.Types.Element;

public class Enemy {

	protected String name;
	protected String sSheet;

	protected IntegerProperty maxHealth = new SimpleIntegerProperty();
	protected IntegerProperty curHealth = new SimpleIntegerProperty();
	protected IntegerProperty maxMana = new SimpleIntegerProperty();
	protected IntegerProperty curMana = new SimpleIntegerProperty();
	protected IntegerProperty manaReg = new SimpleIntegerProperty();
	protected ObjectProperty<Element> curElement = new SimpleObjectProperty<Element>();
	protected ArrayList<Element> resists;
	protected ArrayList<Element> weaks;
	
	protected Enemy(){
	}

	public Enemy(List<String> prop) {
		for (String p : prop) {
			String val = p.substring(p.indexOf('"') + 1, p.lastIndexOf('"'));
			switch (p.substring(0, p.indexOf("="))) {
			case "Name": {
				name = val;
				break;
			}
			case "Health": {
				maxHealth.set(Integer.parseInt(val));
				curHealth.set(maxHealth.get());
				break;
			}
			case "Mana": {
				maxMana.set(Integer.parseInt(val));
				curMana.set(maxMana.get());
				break;
			}
			case "ManaR": {
				manaReg.set(Integer.parseInt(val));
				break;
			}
			case "Element": {
				switch (val) {
				case "FIRE": {
					curElement.set(Element.FIRE);
					break;
				}
				case "LIGHTING": {
					curElement.set(Element.LIGHTNING);
					break;
				}
				case "EARTH": {
					curElement.set(Element.EARTH);
					break;
				}
				case "METAL": {
					curElement.set(Element.METAL);
					break;
				}
				case "DEATH": {
					curElement.set(Element.DEATH);
					break;
				}
				case "CONSUME": {
					curElement.set(Element.CONSUME);
					break;
				}
				default: {
					curElement.set(Element.NEUTRAL);
				}
				}
			}
			case "Resists": {
				resists = getRWs(p);
				break;
			}
			case "Weak": {
				weaks = getRWs(p);
				break;
			}
			case "Spritesheet": {
				sSheet = val;
				break;
			}
			}
		}
	}

	private ArrayList<Element> getRWs(String p) {
		ArrayList<Element> rws = new ArrayList<Element>();

		while (p.contains("\"")) {
			p = p.substring(p.indexOf('"') + 1);
			String val = p.substring(0, p.indexOf('"'));
			p = p.substring(p.indexOf('"') + 1);
			switch (val) {
				case "FIRE": {
					rws.add(Element.FIRE);
					break;
				}
				case "EARTH": {
					rws.add(Element.EARTH);
					break;
				}
				case "METAL": {
					rws.add(Element.METAL);
					break;
				}
				case "LIGHTING": {
					rws.add(Element.LIGHTNING);
					break;
				}
				case "DEATH": {
					rws.add(Element.DEATH);
					break;
				}
				case "CONSUME": {
					rws.add(Element.CONSUME);
					break;
				}
			}
		}

		return rws;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String s){
		name = s;
	}

	public String getSpritesheet() {
		return sSheet;
	}
	
	public void setSpritesheet(String s){
		sSheet = s;
	}

	public IntegerProperty MaxHealthProperty() {
		return maxHealth;
	}

	public int getMaxHealth() {
		return maxHealth.get();
	}

	public void setMaxHealth(int mH) {
		maxHealth.set(mH);
	}

	public IntegerProperty CurrentHealthProperty() {
		return curHealth;
	}

	public int getCurrentHealth() {
		return curHealth.get();
	}

	public void setCurrentHealth(int cH) {
		curHealth.set(cH);
	}

	public IntegerProperty MaxManaProperty() {
		return maxMana;
	}

	public int getMaxMana() {
		return maxMana.get();
	}

	public void setMaxMana(int mH) {
		maxMana.set(mH);
	}

	public IntegerProperty CurrentManaProperty() {
		return curMana;
	}

	public int getCurrentMana() {
		return curMana.get();
	}

	public void setCurrentMana(int cM) {
		curMana.set(cM);
	}

	public IntegerProperty ManaRegenRateProperty() {
		return manaReg;
	}

	public int getManaRegenRate() {
		return manaReg.get();
	}

	public void setManaRegenRate(int mR) {
		manaReg.set(mR);
	}

	public ObjectProperty<Element> ElementProperty() {
		return curElement;
	}

	public Element getElement() {
		return curElement.get();
	}

	public void setElement(Element e) {
		curElement.set(e);
	}

	public ArrayList<Element> getResistances() {
		return resists;
	}

	public ArrayList<Element> getWeaknesses() {
		return weaks;
	}

	/**
	 * Restores health of enemy/player by given value in % ratio 1.0 = 100% 0.5
	 * = 50%
	 *
	 * Will NOT overheal the character
	 *
	 * @param percent
	 */
	public void restoreHealth(double percent) {
		int restored = (int) (maxHealth.get() * percent);
		setCurrentHealth(Math.min(maxHealth.get(), curHealth.get() + restored));
	}

	/**
	 * Restores mana of enemy/player by given value in % ratio 1.0 = 100% 0.5 =
	 * 50%
	 *
	 * Will NOT increase mana over 100%
	 *
	 * @param percent
	 */
	public void restoreMana(int value) {
		setCurrentMana(Math.min(maxMana.get(), curMana.get() + value));
	}

	/**
	 * Regen "tick", restores mana according to character's mana regeneration
	 * rate
	 */
	public void regenMana() {
		restoreMana(manaReg.get());
	}

	public void takeDamage(int value) {
		curHealth.set(curHealth.get() - value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(",").append(sSheet);
		sb.append(",health:").append(curHealth.get()).append("/").append(maxHealth.get());
		sb.append(",mana:").append(curMana.get()).append("/").append(maxMana.get());
		sb.append("(").append(manaReg.get()).append("%").append(")");
		return sb.toString();
	}
}
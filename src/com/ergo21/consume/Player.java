package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

import com.almasb.consume.Types.Element;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Player extends Enemy{

	private ArrayList<Element> powers = new ArrayList<Element>();
	private ArrayList<Integer> upgradesCollected;
	private ArrayList<Integer> levelsCompleted;
	private int currentLevel;
	private BooleanProperty weaponThrown;

	public Player(List<String> prop) {
		super(prop);
		powers.add(Element.NEUTRAL);
		curElement.set(powers.get(0));
		currentLevel = 0;
		upgradesCollected = new ArrayList<Integer>();
		levelsCompleted = new ArrayList<Integer>();
		weaponThrown = new SimpleBooleanProperty(false);
	}
	
	public Player(GameSave g){
		super();
		curElement = new SimpleObjectProperty<Element>(g.getCurElement());
		curHealth = new SimpleIntegerProperty(g.getCurHealth());
		currentLevel = g.getCurLevel();
		curMana = new SimpleIntegerProperty(g.getCurMana());
		manaReg = new SimpleIntegerProperty(g.getManaReg());
		maxHealth = new SimpleIntegerProperty(g.getMaxHealth());
		maxMana = new SimpleIntegerProperty(g.getMaxMana());
		weaponThrown = new SimpleBooleanProperty(false);
		name = g.getName();
		powers = g.getPowers();
		resists = g.getResists();
		sSheet = g.getSSheet();
		weaks = g.getWeaks();
		upgradesCollected = g.getUpgrades();
		levelsCompleted = g.getLevsComp();
	}
	
	public void increaseMaxHealth(int value) {
		maxHealth.set(maxHealth.get() + value);
	}

	public void increaseMaxMana(int value) {
		maxMana.set(maxMana.get() + value);
	}

	public void increaseManaRegen(int value) {
		manaReg.set(manaReg.get() + value);
	}

	public Element getCurrentPower() {
		return curElement.get();
	}

	public ArrayList<Element> getPowers() {
		return powers;
	}

	public void setCurrentPower(Element p) {
		curElement.set(p);
		switch (curElement.get()) {
		case NEUTRAL:
		case NEUTRAL2:
		case CONSUME: {
			resists.clear();
			weaks.clear();
			break;
		}
		case FIRE: {
			resists.clear();
			resists.add(Element.FIRE);
			weaks.clear();
			weaks.add(Element.EARTH);
			break;
		}
		case EARTH: {
			resists.clear();
			resists.add(Element.EARTH);
			weaks.clear();
			weaks.add(Element.METAL);
			break;
		}
		case METAL: {
			resists.clear();
			resists.add(Element.METAL);
			weaks.clear();
			weaks.add(Element.LIGHTNING);
			break;
		}
		case LIGHTNING: {
			resists.clear();
			resists.add(Element.LIGHTNING);
			weaks.clear();
			weaks.add(Element.DEATH);
			break;
		}
		case DEATH: {
			resists.clear();
			resists.add(Element.DEATH);
			weaks.clear();
			weaks.add(Element.FIRE);
			break;
		}
		default: {
			resists.clear();
			weaks.clear();
			break;
		}
		}
	}
	
	public boolean getWeaponThrown(){
		return weaponThrown.get();
	}
	
	public void setWeaponThrown(ReadOnlyBooleanProperty b){
		weaponThrown.bind(b);
	}
	
	public int getCurrentLevel(){
		return currentLevel;
	}
	
	public void setCurrentLevel(int l){
		currentLevel = l;
	}

	public ArrayList<Integer> getUpgrades() {
		return upgradesCollected;
	}

	public ArrayList<Integer> getLevsComp() {
		return levelsCompleted;
	}

}
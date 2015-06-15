package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

import com.almasb.consume.Types.Element;

public class Player extends Enemy {
	
    private ArrayList<Element> powers = new ArrayList<Element>();
    private Element curPower;

    public Player(List<String> prop) {
        super(prop);
        powers.add(Element.NEUTRAL);
        curPower = powers.get(0);
    }

    public void setMaxHealth(int mH) {
        mHealth = mH;
    }

    public void setMaxMana(int mM) {
        mMana = mM;
    }

    public void setManaRegenRate(int mR) {
        manaR = mR;
    }

    public void increaseMaxHealth(int value) {
        mHealth += value;
    }

    public void increaseMaxMana(int value) {
        mMana += value;
    }

    public void increaseManaRegen(int value) {
        manaR += value;
    }

    public ArrayList<Element> getPowers() {
        return powers;
    }

    public void setCurrentPower(Element p) {
        curPower = p;
        switch (curPower) {
            case NEUTRAL: {
                resists.clear();
                weaks.clear();
                break;
            }
            case FIRE: {
                resists.clear();
                resists.add(Element.FIRE);
                weaks.clear();
                weaks.add(Element.ICE);
                break;
            }
            case ICE: {
                resists.clear();
                resists.add(Element.ICE);
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

}
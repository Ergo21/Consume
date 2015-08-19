package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

import com.almasb.consume.Types.Element;

public class Player extends Enemy {

    private ArrayList<Element> powers = new ArrayList<Element>();

    public Player(List<String> prop) {
        super(prop);
        powers.add(Element.NEUTRAL);
        curElement = powers.get(0);
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

    public Element getCurrentPower() {
        return curElement;
    }

    public ArrayList<Element> getPowers() {
        return powers;
    }

    public void setCurrentPower(Element p) {
        curElement = p;
        switch (curElement) {
            case NEUTRAL: {
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
            case CONSUME: {
            	resists.clear();
                weaks.clear();
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
package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

import com.almasb.consume.Types.Element;

public class Player extends Enemy {

    private ArrayList<Element> powers = new ArrayList<Element>();

    public Player(List<String> prop) {
        super(prop);
        powers.add(Element.NEUTRAL);
        curElement.set(powers.get(0));
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
            case NEUTRAL: case NEUTRAL2: case CONSUME:{
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

}
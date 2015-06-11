package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

import com.ergo21.consume.powers.Power;

public class Player extends Enemy {
    public enum PowerElem {
        NORMAL, FIRE, ICE
    };

    private List<Power> powers = new ArrayList<>();
    private Power curPower;

    public Player(List<String> prop) {
        super(prop);
        powers.add(new Power(this, PowerElem.NORMAL));
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

    public List<Power> getPowers() {
        return powers;
    }

    public void setCurrentPower(Power p) {
        curPower = p;
        switch (curPower.getElement()) {
            case NORMAL: {
                resists.clear();
                weaks.clear();
                break;
            }
            case FIRE: {
                resists.clear();
                resists.add(ETags.FIRE);
                weaks.clear();
                weaks.add(ETags.ICE);
                break;
            }
            case ICE: {
                resists.clear();
                resists.add(ETags.ICE);
                weaks.clear();
                weaks.add(ETags.FIRE);
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
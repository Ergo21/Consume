package com.ergo21.consume;

public class Powerups{
	
	private boolean healthIncrease;
	
	private double restoreValue;
	
	public Powerups(boolean helInc, double resVal){
		healthIncrease = helInc;
		restoreValue = resVal;
	}
	
	public boolean isIncreaseHealth(){
		return healthIncrease;
	}
	
	public double restoreValue(){
		return restoreValue;
	}
}
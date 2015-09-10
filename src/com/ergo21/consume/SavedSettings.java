package com.ergo21.consume;

import java.io.Serializable;
import java.util.HashMap;

import com.almasb.consume.Types.Actions;

import javafx.scene.input.KeyCode;

public class SavedSettings implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2564696803280047023L;

	private double backVol;
	private double sfxVol;
	private HashMap<Actions, KeyCode> controls;
	private String lastSave;
	
	public SavedSettings(){
		backVol = 0.75;
		sfxVol = 0.75;
		controls = new HashMap<Actions, KeyCode>(); 
		lastSave = "";
	}
	
	public double getBackMusicVolume(){
		return backVol;
	}
	public void setBackMusicVolume(double v){
		backVol = v;
	}
	
	public double getSFXVolume(){
		return sfxVol;
	}
	public void setSFXVolume(double v){
		sfxVol = v;
	}
	
	public HashMap<Actions, KeyCode> getControls(){
		return controls;
	}
	
	public void setControls(HashMap<Actions, KeyCode> c){
		controls = c;
	}
	
	public String getLastSave(){
		return lastSave;
	}
	
	public void setLastSave(String ls){
		lastSave = ls;
	}
}
package com.ergo21.consume;

import com.almasb.fxgl.asset.Texture;


public class SceneLine{
	private String name; 
	private Texture icon; 
	private String sentence;
	
	public SceneLine(String nam, Texture texture, String sen){
		name = nam;
		icon = texture;
		sentence = sen;
	}
	
	public String getName(){
		return name;
	}
	public Texture getIcon(){
		return icon;
	}
	public String getSentence(){
		return sentence;
	}
}
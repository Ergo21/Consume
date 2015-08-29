package com.ergo21.consume;

import java.util.HashMap;

import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.asset.Music;

import javafx.scene.media.AudioClip;


public class SoundManager{
	private Music backgroundMusic;
	private HashMap<String, AudioClip> loadedEffects;
	private ConsumeApp app;
	
	public SoundManager(ConsumeApp a){
		app = a;
		
	}
	
	public void setBackgroundMusic(String name){
		backgroundMusic = app.assets.getMusic(name);
	}
	
	public Music getBackgroundMusic(){
		return backgroundMusic;
	}
	
	public void playSFX(String name){
		if(loadedEffects.containsKey(name)){
			loadedEffects.get(name).play(app.sfxVolume);
		}
		else if(app.assets.getAudio(name) != null){
			loadedEffects.put(name, app.assets.getAudio(name));
			loadedEffects.get(name).play(app.sfxVolume);
		}
	}
}
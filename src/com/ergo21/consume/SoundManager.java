package com.ergo21.consume;

import java.util.HashMap;
import java.util.function.BiConsumer;

import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.asset.Music;

import javafx.scene.media.AudioClip;


public class SoundManager{
	private Music backgroundMusic;
	private HashMap<String, AudioClip> loadedEffects;
	private ConsumeApp app;
	private boolean backPaused;
	
	public SoundManager(ConsumeApp a){
		app = a;
		backPaused = false;
		loadedEffects = new HashMap<String, AudioClip>();
	}
	
	public void setBackgroundMusic(String name){
		backgroundMusic = app.assets.getMusic(name);
		backPaused = false;
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
	
	public void pauseBackMusic(){
		if(backgroundMusic == null){
			return;
		}
		
		backgroundMusic.pause();
		backPaused = true;
	}
	
	public void resumeBackMusic(){
		if(backgroundMusic == null){
			return;
		}
		backgroundMusic.resume();
		backPaused = false;
	}

	public boolean isBackMusicPaused() {
		return backPaused;
	}
	
	public void stopAll(){
		backgroundMusic.stop();
		loadedEffects.forEach(new BiConsumer<String, AudioClip>(){
			@Override
			public void accept(String name, AudioClip clip) {
				clip.stop();
			}
		});
	}
}
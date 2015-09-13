package com.ergo21.consume;

import java.util.HashMap;
import java.util.Map;

import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.asset.Music;
import com.almasb.fxgl.asset.Sound;

public class SoundManager {
	private Music backgroundMusic;
	private Map<String, Sound> loadedEffects;
	private ConsumeApp app;
	private boolean backPaused;

	public SoundManager(ConsumeApp a){
		app = a;
		backPaused = false;
		loadedEffects = new HashMap<>();
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
		    app.getAudioManager().playSound(loadedEffects.get(name));
		}
		else if(app.assets.getSound(name) != null){
			loadedEffects.put(name, app.assets.getSound(name));
			app.getAudioManager().playSound(loadedEffects.get(name));
		}
	}

	public void pauseBackMusic(){
		if(backgroundMusic == null){
			return;
		}

		app.getAudioManager().pauseMusic(backgroundMusic);
		backPaused = true;
	}

	public void resumeBackMusic(){
		if(backgroundMusic == null){
			return;
		}
		app.getAudioManager().resumeMusic(backgroundMusic);
		backPaused = false;
	}

	public boolean isBackMusicPaused() {
		return backPaused;
	}

	public void stopAll(){
	    app.getAudioManager().stopMusic(backgroundMusic);
		loadedEffects.forEach((name, sound) -> app.getAudioManager().stopSound(sound));
	}
}
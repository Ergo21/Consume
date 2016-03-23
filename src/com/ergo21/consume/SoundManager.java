package com.ergo21.consume;

import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.asset.Music;

public class SoundManager {
	private Music backgroundMusic;
	private ConsumeApp app;
	private boolean backPaused;

	public SoundManager(ConsumeApp a){
		app = a;
		backPaused = false;
	}

	public void setBackgroundMusic(String name){
		backgroundMusic = app.assets.getMusic(name);
		backPaused = false;
	}

	public Music getBackgroundMusic(){
		return backgroundMusic;
	}

	public void playSFX(String name){
		app.getAudioManager().playSound(app.assets.getSound(name));
	}
	
	public void playBackgroundMusic(){
		if(backgroundMusic == null){
			return;
		}
		
		app.getAudioManager().playMusic(backgroundMusic);
	}

	public void pauseBackgroundMusic(){
		if(backgroundMusic == null){
			return;
		}

		app.getAudioManager().pauseMusic(backgroundMusic);
		backPaused = true;
	}

	public void resumeBackgroundMusic(){
		if(backgroundMusic == null){
			return;
		}
		app.getAudioManager().resumeMusic(backgroundMusic);
		backPaused = false;
	}

	public boolean isBackgroundMusicPaused() {
		return backPaused;
	}

	public void stopAll(){
	    app.getAudioManager().stopAllMusic();
		app.getAudioManager().stopAllSounds();
	}
}
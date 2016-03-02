package com.ergo21.consume;

import com.almasb.fxgl.asset.Texture;

public class SceneLine {
	private String name;
	private Texture icon;
	private String sentence;
	private Runnable runnable;

	public SceneLine(String nam, Texture texture, String sen, Runnable r) {
		name = nam;
		icon = texture;
		sentence = sen;
		runnable = r;
	}

	public String getName() {
		return name;
	}

	public Texture getIcon() {
		return icon;
	}

	public String getSentence() {
		return sentence;
	}
	
	public Runnable getRunnable(){
		return runnable;
	}
}
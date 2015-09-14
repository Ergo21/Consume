package com.ergo21.consume;

import com.almasb.consume.ConsumeApp;

import javafx.animation.AnimationTimer;

public class IndependentLoop extends AnimationTimer{
	private ConsumeApp consApp;
	public IndependentLoop(ConsumeApp cA){
		consApp = cA;
	}

	@Override
	public void handle(long now) {
		
	}

}
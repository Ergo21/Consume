package com.almasb.consume.ai;

import javafx.animation.FadeTransition;
import javafx.scene.Group;
import javafx.util.Duration;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class LightningControl extends AbstractControl {

	private long created;
	private boolean nPlayed;
	private Group ligGraphics;

	public LightningControl(Group gr) {
		created = 0;
		nPlayed = true;
		ligGraphics = gr;
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
	}

	private int frames = 10;
	@Override
	public void onUpdate(Entity entity, long now){
		frames++;
		if(frames >= 5){
			actualUpdate(entity, now);
			frames = 0;
		}
	}
	
	public void actualUpdate(Entity entity, long now) {
		//PhysicsControl pc = entity.getControl(PhysicsControl.class);
		if (created == 0) {
			created = now;
		}

		if (now - created > TimerManager.toNanos(Config.LIGHTNING_DECAY) && nPlayed) {
			nPlayed = false;
			entity.setCollidable(false);
			FadeTransition ft = new FadeTransition(Duration.seconds(0.4), ligGraphics);
			ft.setFromValue(1);
			ft.setToValue(0);
			ft.setOnFinished(evt -> {
				entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
			});
			ft.play();
		} else if (now - created > TimerManager.toNanos(Config.LIGHTNING_DELAY)) {
			entity.setVisible(true);
			entity.setCollidable(true);
		}

	}

}

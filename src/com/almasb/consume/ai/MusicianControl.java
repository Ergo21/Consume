package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;
import com.ergo21.consume.FileNames;

public class MusicianControl extends AbstractControl {

	private ConsumeApp consApp;
	private Entity target;
	private int vel = 0;
	private int jump;
	private long lastJumped = -1; // Stores when the enemy first sees the player
	private boolean played;

	public MusicianControl(ConsumeApp cA, Entity tar, int jum) {
		consApp = cA;
		target = tar;
		jump = jum;
		played = false;
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
		if (entity.getControl(PhysicsControl.class).getJump() != jump) {
			entity.getControl(PhysicsControl.class).setJump(jump);
		}
		if (lastJumped == -1) {
			lastJumped = now;
		}

		if(!(boolean) entity.getProperty("jumping") && !played && isTargetInRange()){
			entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
		}
		else if (now - lastJumped > TimerManager.toNanos(Config.ENEMY_JUMP_DELAY.add(Config.ENEMY_SOUND_PLAY_DELAY))) {
			if (entity.getPosition().getX() > target.getPosition().getX()) {
				entity.getControl(PhysicsControl.class).moveX(-Speed.ENEMY_PATROL);
			} else {
				entity.getControl(PhysicsControl.class).moveX(Speed.ENEMY_PATROL);
			}
			entity.getControl(PhysicsControl.class).jump();
			lastJumped = now;
			consApp.soundManager.playSFX(FileNames.JUMP);
			setPlayed(false);
		} else if (!(boolean) entity.getProperty("jumping")) {
			entity.getControl(PhysicsControl.class).moveX(0);
		}
		
		if(target.getPosition().getX() > entity.getPosition().getX()){
			entity.setProperty("facingRight", true);
		}
		else{
			entity.setProperty("facingRight", false);
		}
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub

	}

	public int getVelocity() {
		return vel;
	}
	
	public void setPlayed(boolean b){
		played = b;
		entity.setProperty("attacking", b);
	}
	
	private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE/2;
	}

}
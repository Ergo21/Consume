package com.almasb.consume.ai;

import java.util.Random;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;
import com.ergo21.consume.FileNames;

import javafx.geometry.Point2D;
import javafx.util.Duration;

public class AnubisControl extends AbstractControl {

	private Entity target;
	private Random ran;
	private enum BossActions {
		NONE, MOVE, JUMP, ATTACK, JATTACK
	}
	private BossActions curAction;
	private Point2D startPos;
	private Point2D curPos;
	private boolean start = false;
	private boolean attacking = false;
	private int vel = 0;
	private long chooseDelay = -1; 
	private long moveStart = -1;
	private int cycle = 0;
	
	private ConsumeApp consApp;

	public AnubisControl(ConsumeApp cA, Entity target) {
		this.target = target;
		ran = new Random();
		curAction = BossActions.NONE;
		consApp = cA;
	}

	@Override
	protected void initEntity(Entity entity) {
		
	}

	private int frames = 10;
	@Override
	public void onUpdate(Entity entity, long now){
		if(consApp.gScene.isVisible()){
			return;
		}
		frames++;
		if(frames >= 5){
			actualUpdate(entity, now);
			frames = 0;
		}
	}
	
	public void actualUpdate(Entity entity, long now) {
		if(startPos == null || curPos == null){
			if(entity != null && entity.getPosition() != null){
				startPos = entity.getPosition();
				curPos = entity.getPosition();
			}
		}
		
		if(start){
			return;
		}
		
		switch(curAction){
			case NONE:{
				if(chooseDelay == -1){
					chooseDelay = now;
				}
				
				if(now - chooseDelay >= TimerManager.toNanos(Duration.seconds(0.5))){
					switch(ran.nextInt(3)){
						case 0:{
							curAction = BossActions.MOVE;
							break;
						}
						case 1:{
							curAction = BossActions.JUMP;
							break;
						}
						case 2:{
							curAction = BossActions.JATTACK;
							break;
						}
					}
					chooseDelay = -1;
				}
				else if(isTargetInRange(1)){
					curAction = BossActions.ATTACK;
					chooseDelay = -1;
				}
				break;
			}
			case MOVE:{
				if(moveStart == -1){
					moveStart = now;
				}
				
				if(isTargetInRange(0.75)){
					curAction = BossActions.ATTACK;
					moveStart = -1;
					cycle = 0;
				} 
				else if(now - moveStart >= TimerManager.toNanos(Duration.seconds(3)) && cycle == 3){				
					curAction = BossActions.NONE;
					moveStart = -1;
					cycle = 0;
				}
				else if((now - moveStart >= TimerManager.toNanos(Duration.seconds(2)) && cycle == 2) ||
						(now - moveStart >= TimerManager.toNanos(Duration.seconds(1)) && cycle == 1) ||
						(now - moveStart < TimerManager.toNanos(Duration.seconds(1)) && cycle == 0)){
					if(entity.getPosition().getX() >= target.getPosition().getX()){
						entity.getControl(PhysicsControl.class).moveX(-Speed.PLAYER_MOVE);
						entity.setProperty("facingRight", false);
					}
					else{
						entity.getControl(PhysicsControl.class).moveX(Speed.PLAYER_MOVE);
						entity.setProperty("facingRight", true);	
					}
					cycle++;
				}
				break;
			}
			case JUMP:{
				if(moveStart == -1){
					moveStart = now;
				}
				if(!(boolean)entity.getProperty("jumping") && now - moveStart >= TimerManager.toNanos(Duration.seconds(3))){
					moveStart = -1;
					curAction = BossActions.NONE;
				}
				else if(!(boolean)entity.getProperty("jumping") && now - moveStart < TimerManager.toNanos(Duration.seconds(3))){
					int spd = calculateJumpSpeed(entity.getPosition(), target.getPosition());
					entity.getControl(PhysicsControl.class).moveX(spd);
					entity.setProperty("facingRight", spd >= 0);
					
					entity.getControl(PhysicsControl.class).jump();
					consApp.soundManager.playSFX(FileNames.JUMP);
				}				
				break;
			}
			case ATTACK:{
				if(!attacking){
					entity.setProperty("facingRight", entity.getPosition().getX() < target.getPosition().getX());
					entity.getControl(PhysicsControl.class).moveX(0);
					entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
					attacking = true;
				}
				break;
			}
			case JATTACK:{
				
				if(!(boolean)entity.getProperty("jumping") && (cycle == 0 || cycle == 3)){ //Jump to start
					int spd = calculateJumpSpeed(entity.getPosition(), startPos);
					entity.getControl(PhysicsControl.class).moveX(spd);
					entity.setProperty("facingRight", spd >= 0);
					
					entity.getControl(PhysicsControl.class).jump();
					consApp.soundManager.playSFX(FileNames.JUMP);
					cycle++;
				}
				else if(!(boolean)entity.getProperty("jumping") && cycle == 1){ //Jump to opposite side
					int spd = calculateJumpSpeed(entity.getPosition(), startPos.subtract(Config.BLOCK_SIZE*7, 0));
					entity.getControl(PhysicsControl.class).moveX(spd);
					entity.setProperty("facingRight", spd >= 0);
					
					entity.getControl(PhysicsControl.class).jump();
					consApp.soundManager.playSFX(FileNames.JUMP);
					cycle++;
				}
				else if((boolean)entity.getProperty("jumping") && (cycle == 2 || cycle == 4)){ //Attack during jump*3
					if(!attacking){
						entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
						attacking = true;
						cycle++;
					}
				}
				else if(!(boolean)entity.getProperty("jumping") && cycle == 5){ //End Jump Attack
					entity.getControl(PhysicsControl.class).moveX(0);
					cycle = 0;
					curAction = BossActions.NONE;
				}
					
					
				
				
				break;
			}
		}
	}
	
	public void setAttackComplete(boolean a, boolean res){
		if(a){
			attacking = false;
		}
		if(res){
			curAction = BossActions.NONE;
		}
	}
	
	public void startFight(boolean fight){
		start = fight;
	}
	
	private int calculateJumpSpeed(Point2D start, Point2D end){
		double dist = end.getX() - start.getX();
		if(dist < Speed.ENEMY_PATROL*60 && dist > -Speed.ENEMY_PATROL*60){
			if((int) dist/60 == 0 && dist > 0){
				return 1;
			}
			else if((int) dist/60 == 0 && dist < 0){
				return -1;
			}
			return (int) dist/60;
		}
		else if(dist >= Speed.ENEMY_PATROL*60){
			return Speed.ENEMY_PATROL;
		}
		else{
			return -Speed.ENEMY_PATROL;
		}
	}
	
	private boolean isTargetInRange(double mod){
		return target.getPosition().distance(entity.getPosition()) <= Config.BLOCK_SIZE*mod;
	}
	
	public int getVelocity() {
		return vel;
	}
}

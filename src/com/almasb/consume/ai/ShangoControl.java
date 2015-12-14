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

public class ShangoControl extends AbstractControl {

	private Entity target;
	private Random ran;
	private enum BossActions {
		NONE, JUMP, ATTACK, JATTACK
	}
	private BossActions curAction;
	private Point2D startPos;
	private Point2D curPos;
	private Point2D jumpPos;
	private boolean start = false;
	private boolean attacking = false;
	private int vel = 0;
	private long chooseDelay = -1; 
	private long moveStart = -1;
	private int cycle = 0;
	
	private ConsumeApp consApp;

	public ShangoControl(ConsumeApp cA, Entity target) {
		this.target = target;
		ran = new Random();
		curAction = BossActions.NONE;
		consApp = cA;
	}

	@Override
	protected void initEntity(Entity entity) {
		
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		if(startPos == null || curPos == null){
			if(entity != null && entity.getPosition() != null){
				startPos = entity.getPosition();
				curPos = entity.getPosition();
				jumpPos = startPos.subtract(Config.BLOCK_SIZE*7, 0);
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
				
				if(now - chooseDelay >= TimerManager.toNanos(Duration.seconds(1))){
					switch(ran.nextInt(3)){
						case 0:{
							curAction = BossActions.JATTACK;
							break;
						}
						case 1:
						case 2:{
							curAction = BossActions.ATTACK;
							break;
						}
					}
					chooseDelay = -1;
				}
				else if(isTargetInRange(1.75)){
					curAction = BossActions.JUMP;
					chooseDelay = -1;
				}
				break;
			}
			case JUMP:{
				if(!(boolean)entity.getProperty("jumping") && moveStart == -1){			
					moveStart = 1;
					int spd = calculateJumpSpeed(entity.getPosition(), chooseJumpPoint());
					entity.getControl(PhysicsControl.class).moveX(spd);
					entity.setProperty("facingRight", spd >= 0);
					
					entity.getControl(PhysicsControl.class).jump();
					consApp.soundManager.playSFX(FileNames.JUMP);
				}		
				else if(!(boolean)entity.getProperty("jumping") && moveStart != -1){
					moveStart = -1;
					entity.getControl(PhysicsControl.class).moveX(0);
					curAction = BossActions.NONE;
					entity.setProperty("facingRight", target.getPosition().getX() >= entity.getPosition().getX());
				}
				break;
			}
			case ATTACK:{
				if(!attacking){
					entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
					attacking = true;
				}
				break;
			}
			case JATTACK:{
				
				if(!(boolean)entity.getProperty("jumping") && (cycle == 0 || cycle == 3)){
					int spd = calculateJumpSpeed(entity.getPosition(), startPos);
					entity.getControl(PhysicsControl.class).moveX(spd);
					entity.setProperty("facingRight", spd >= 0);
					
					entity.getControl(PhysicsControl.class).jump();
					consApp.soundManager.playSFX(FileNames.JUMP);
					cycle++;
				}
				else if(!(boolean)entity.getProperty("jumping") && cycle == 1){
					int spd = calculateJumpSpeed(entity.getPosition(), jumpPos);
					entity.getControl(PhysicsControl.class).moveX(spd);
					entity.setProperty("facingRight", spd >= 0);
					
					entity.getControl(PhysicsControl.class).jump();
					consApp.soundManager.playSFX(FileNames.JUMP);
					cycle++;
				}
				else if((boolean)entity.getProperty("jumping") && (cycle == 2 || cycle == 4)){
					if(!attacking){
						entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
						attacking = true;
						cycle++;
					}
				}
				else if(!(boolean)entity.getProperty("jumping") && cycle == 5){
					entity.getControl(PhysicsControl.class).moveX(0);
					cycle = 0;
					curAction = BossActions.NONE;
					entity.setProperty("facingRight", target.getPosition().getX() >= entity.getPosition().getX());
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
	
	private Point2D chooseJumpPoint(){
		if(target.getPosition().distance(startPos) > target.getPosition().distance(jumpPos)){
			return startPos;
		}
		else{
			return jumpPos;
		}
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

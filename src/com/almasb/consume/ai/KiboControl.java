package com.almasb.consume.ai;

import java.util.Random;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;
import com.ergo21.consume.FileNames;

import javafx.geometry.Point2D;
import javafx.util.Duration;

public class KiboControl extends AbstractControl {

	private Entity target;
	private Random ran;
	private enum BossActions {
		NONE, ATTACK, MATTACK,
	}
	private BossActions curAction;
	private Point2D startPos;
	private Point2D curPos;
	private boolean start = false;
	private boolean attacking = false;
	private int atType = 0;
	private int vel = 0;
	private long chooseDelay = -1; 
	
	private ConsumeApp consApp;

	public KiboControl(ConsumeApp cA, Entity target) {
		this.target = target;
		ran = new Random();
		curAction = BossActions.NONE;
		consApp = cA;
		vel = Config.Speed.PLAYER_MOVE*2/3;
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
						case 0:
						case 1:{
							curAction = BossActions.ATTACK;
							atType = ran.nextInt(2);
							break;
						}
						case 2:{
							curAction = BossActions.MATTACK;
							atType = 2;
							break;
						}
					}
					chooseDelay = -1;
				}
				else if(isTargetInRange(1.75)){
					curAction = BossActions.MATTACK;
					chooseDelay = -1;
				}
				break;
			}
			case ATTACK:{
				if(!attacking){
					entity.setProperty("facingRight", target.getPosition().getX() >= entity.getPosition().getX());
					entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
					attacking = true;
				}
				break;
			}
			case MATTACK:{
				if(chooseDelay == -1){
					chooseDelay = now;
				}
				
				if(now - chooseDelay >= TimerManager.toNanos(Duration.seconds(3))){
					chooseDelay = -1;
					entity.getControl(PhysicsControl.class).moveX(0);
					curAction = BossActions.NONE;
					entity.setProperty("facingRight", target.getPosition().getX() >= entity.getPosition().getX());
				}
				else if(!(boolean)entity.getProperty("jumping") && ran.nextInt(150) == 0){
					if(target.getPosition().getX() >= entity.getPosition().getX()){
						entity.getControl(PhysicsControl.class).moveX(vel);
						entity.setProperty("facingRight", true);
					}
					else{
						entity.getControl(PhysicsControl.class).moveX(-vel);
						entity.setProperty("facingRight", false);
					}
					entity.getControl(PhysicsControl.class).jump();
					consApp.soundManager.playSFX(FileNames.JUMP);
				}
				else if(!(boolean)entity.getProperty("jumping")){
					if(target.getPosition().getX() >= entity.getPosition().getX()){
						entity.getControl(PhysicsControl.class).moveX(vel);
						entity.setProperty("facingRight", true);
					}
					else{
						entity.getControl(PhysicsControl.class).moveX(-vel);
						entity.setProperty("facingRight", false);
					}
				}
				
				if(!attacking){
					entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
					attacking = true;
				}
				break;
			}
		}
	}
	
	public int getAttackType(){
		return atType; //0: Aimed Fireball, 1: Fireball Spread, 2: Straight Fireball
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
	
	private boolean isTargetInRange(double mod){
		return target.getPosition().distance(entity.getPosition()) <= Config.BLOCK_SIZE*mod;
	}
	
}

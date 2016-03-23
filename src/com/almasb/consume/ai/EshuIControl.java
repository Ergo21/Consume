package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.Types.Property;
import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;
import com.ergo21.consume.Enemy;
import com.ergo21.consume.FileNames;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class EshuIControl extends AbstractControl {

	private Entity target;
	private enum BossActions {
		NONE, RETURN, JUMP, ATTACK, CATTACK, JATTACK
	}
	private BossActions curAction;
	private Point2D startPos;
	private Point2D curPos;
	private Point2D jumpPos;
	private boolean start = false;
	private boolean attacking = false;
	private int vel = 0;
	private int cycle = 0;
	private long chooseDelay = -1; 
	private long moveStart = -1;
	
	private ConsumeApp consApp;

	public EshuIControl(ConsumeApp cA, Entity target) {
		this.target = target;
		curAction = BossActions.NONE;
		consApp = cA;
		
		target.<Enemy>getProperty(Property.DATA).CurrentHealthProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if(arg2.intValue() <= 1){
					target.<Enemy>getProperty(Property.DATA).setCurrentHealth(1);
					moveStart = -1;
					entity.getControl(PhysicsControl.class).moveX(0);
					curAction = BossActions.RETURN;
				}
			}
		});;
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
				jumpPos = startPos.subtract(Config.BLOCK_SIZE*7, 0);
				
				entity.<Enemy>getProperty(Property.DATA).CurrentHealthProperty().addListener(new ChangeListener<Number>(){
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						if(newValue.intValue() < entity.<Enemy>getProperty(Property.DATA).getMaxHealth()){
							entity.<Enemy>getProperty(Property.DATA).setCurrentHealth(entity.<Enemy>getProperty(Property.DATA).getMaxHealth());
						}
					}
				});
			}
		}
		
		if(start){
			return;
		}
		
		entity.<Enemy>getProperty(Property.DATA).setCurrentHealth(entity.<Enemy>getProperty(Property.DATA).getMaxHealth());
		
		switch(curAction){
			case NONE:{
				if(chooseDelay == -1){
					chooseDelay = now;
					entity.setProperty("attacking", false);
				}
				
				if(now - chooseDelay >= TimerManager.toNanos(Duration.seconds(1))){
					switch(consApp.getRandom().nextInt(3)){
						case 0:{
							curAction = BossActions.ATTACK;
							break;
						}
						case 1:{
							curAction = BossActions.CATTACK;
							break;
						}
						case 2:{
							curAction = BossActions.JATTACK;
							break;
						}
					}
					chooseDelay = -1;
				}
				else if(isTargetInRange(1.75)){
					if(consApp.getRandom().nextBoolean()){
						curAction = BossActions.CATTACK;
					}
					else{
						curAction = BossActions.JUMP;
					}
					chooseDelay = -1;
				}
				break;
			}
			case RETURN:{
				if(target != null && target.getControl(PhysicsControl.class) != null ){
					target.setProperty("stunned", true);
					target.getControl(PhysicsControl.class).moveX(-4);
				}
				if(!(boolean)entity.getProperty("jumping") && moveStart == -1){			
					moveStart = 1;
					attacking = false;
					entity.setProperty("attacking", false);
					int spd = calculateJumpSpeed(entity.getPosition(), startPos);
					entity.getControl(PhysicsControl.class).moveX(spd);
					entity.setProperty("facingRight", spd >= 0);
					
					entity.getControl(PhysicsControl.class).jump();
					consApp.soundManager.playSFX(FileNames.JUMP);
				}		
				else if(!(boolean)entity.getProperty("jumping") && moveStart != -1){
					moveStart = -1;
					entity.getControl(PhysicsControl.class).moveX(0);
					curAction = BossActions.NONE;
					start = false;
					target.setProperty("stunned", false);
					target.getControl(PhysicsControl.class).moveX(0);
					entity.setProperty("facingRight", target.getPosition().getX() >= entity.getPosition().getX());
					consApp.gScene.changeScene(consApp.assets.getText("dialogue/scene_1_2.txt"));
					consApp.gScene.playScene();
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
			case JATTACK:{
				if(!(boolean)entity.getProperty("jumping") && moveStart == -1){			
					moveStart = now;
					int spd = calculateJumpSpeed(entity.getPosition(), chooseJumpAttackPoint());
					entity.getControl(PhysicsControl.class).moveX(spd);
					entity.setProperty("facingRight", spd >= 0);
					
					entity.getControl(PhysicsControl.class).jump();
					consApp.soundManager.playSFX(FileNames.JUMP);
				}		
				else if((boolean)entity.getProperty("jumping") && now - moveStart >= TimerManager.toNanos(Duration.seconds(0.4))){
					if(!attacking){
						attacking = true;
						entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
						entity.setProperty("attacking", true);
					}
				}
				else if(!(boolean)entity.getProperty("jumping") && moveStart != -1){
					moveStart = -1;
					attacking = false;
					entity.setProperty("attacking", false);
					entity.getControl(PhysicsControl.class).moveX(0);
					curAction = BossActions.NONE;
					entity.setProperty("facingRight", target.getPosition().getX() >= entity.getPosition().getX());
				}
				break;
			}
			case ATTACK:{
				if(!attacking){
					attacking = true;
					entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
					entity.setProperty("attacking", true);
				}
				break;
			}
			case CATTACK:{
				if(moveStart == -1){
					moveStart = now;
					entity.setProperty("attacking", true);
				}
				
				if(now - moveStart >= TimerManager.toNanos(Duration.seconds(5))){
					entity.getControl(PhysicsControl.class).moveX(0);
					moveStart = -1;
					curAction = BossActions.JUMP;
					entity.setProperty("facingRight", target.getPosition().getX() >= entity.getPosition().getX());
					attacking = false;
					cycle = 0;
					entity.setProperty("attacking", false);
				}
				else if(now - moveStart >= TimerManager.toNanos(Duration.seconds(0.9*cycle))){
					if(target.getPosition().getX() >= entity.getPosition().getX()){
						vel = Config.ESHU_MOVE;
					}
					else{
						vel = -Config.ESHU_MOVE;
					}
					entity.getControl(PhysicsControl.class).moveX(vel);
					entity.setProperty("facingRight", target.getPosition().getX() >= entity.getPosition().getX());
					cycle++;
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
	
	private Point2D chooseJumpAttackPoint(){
		if(target.getPosition().distance(startPos) < target.getPosition().distance(jumpPos)){
			return startPos;
		}
		else{
			return jumpPos;
		}
	}
	
	private int calculateJumpSpeed(Point2D start, Point2D end){
		double dist = end.getX() - start.getX();
		if(dist < Config.ESHU_MOVE*60 && dist > -Config.ESHU_MOVE*60){
			if((int) dist/60 == 0 && dist > 0){
				return 1;
			}
			else if((int) dist/60 == 0 && dist < 0){
				return -1;
			}
			return (int) dist/60;
		}
		else if(dist >= Config.ESHU_MOVE*60){
			return Config.ESHU_MOVE;
		}
		else{
			return -Config.ESHU_MOVE;
		}
	}
	
	private boolean isTargetInRange(double mod){
		return target.getPosition().distance(entity.getPosition()) <= Config.BLOCK_SIZE*mod;
	}
	
	public int getVelocity() {
		return vel;
	}
	
	public boolean isShortThrow(){
		if(target.getPosition().distance(entity.getPosition()) < Config.ENEMY_FIRE_RANGE*3/4){
			return true;
		}
		return false;
	}
	
}

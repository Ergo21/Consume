package com.almasb.consume.ai;


import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SandBossControl extends AbstractControl {

	private ConsumeApp consApp;
	private Entity target;
	private enum BossActions {
		NONE, MERGE, FORM, BLOW, UATK
	}
	private BossActions curAction;
	private Point2D startPos;
	private Point2D curPos;
	private boolean start = true;
	private boolean attacking = false;
	private int vel = 0;
	private long chooseDelay = -1; 
	private long mergeDelay = -1;
	private int cycle = 0;
	private boolean underground;
	private int form = -1;

	public SandBossControl(ConsumeApp cA, Entity target) {
		this.target = target;
		consApp = cA;
		curAction = BossActions.FORM;
		underground = true;
	}

	@Override
	protected void initEntity(Entity entity) {
		
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
		if(startPos == null || curPos == null){
			if(entity != null && entity.getPosition() != null){
				startPos = entity.getPosition();
				curPos = entity.getPosition();
			}
		}
		
		switch(curAction){
			case NONE:{
				if(chooseDelay == -1){
					chooseDelay = now;
				}
				
				if(now - chooseDelay >= TimerManager.toNanos(Duration.seconds(1))){
					if(underground){
						if(consApp.getRandom().nextBoolean()){
							curAction = BossActions.FORM;
						}
						else{
							curAction = BossActions.UATK;
						}
					}
					else if(entity.<Boolean>getProperty("facingRight") != (target.getPosition().getX() > entity.getPosition().getX())){
						curAction = BossActions.MERGE;
					}
					else{
						if(consApp.getRandom().nextBoolean()){
							curAction = BossActions.MERGE;
						}
						else{
							curAction = BossActions.BLOW;
						}
					}
					
					chooseDelay = -1;
				}
				break;
			}
			case FORM:{
				if(mergeDelay == -1){
					mergeDelay = now;
					entity.setProperty("facingRight", target.getPosition().getX() > entity.getPosition().getX());
					form = 0;					
				}
				
				if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.75))){
					form = 3;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.5))){
					form = 2;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.25))){
					form = 1;
				}
				
				if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(1)) && cycle == 2){
					Rectangle rect = new Rectangle(60, 60);
					rect.setFill(Color.RED);
					entity.setPosition(entity.getPosition().subtract(0, 20));
					entity.setGraphics(rect);
					curAction = BossActions.NONE;
					mergeDelay = -1;
					cycle = 0;
					form = -1;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.66)) && cycle == 1){
					Rectangle rect = new Rectangle(60, 40);
					rect.setFill(Color.RED);
					entity.setPosition(entity.getPosition().subtract(0, 20));
					entity.setGraphics(rect);
					entity.setCollidable(true);
					cycle = 2;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.33)) && cycle == 0){
					entity.setVisible(true);
					if(start){
						start = false;
					}
					cycle = 1;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.05)) && underground){
					underground = false;
				}
				
				break;
			}
			case MERGE:{
				if(mergeDelay == -1){
					mergeDelay = now;
					form = 3;
				}
				
				if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.75)) && cycle == 2){
					form = 0;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.5)) && cycle == 1){
					form = 1;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.25)) && cycle == 0){
					form = 2;
				}
				
				if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(1)) && cycle == 2){
					entity.setVisible(false);
					underground = true;
					if(!start){
						switch(consApp.getRandom().nextInt(3)){
							case 0:{
								curPos = startPos; //7.5 of 10
								break;
							}
							case 1:{
								curPos = startPos.subtract(100, 0); // 5 of 10
								break;
							}
							case 2:{
								curPos = startPos.subtract(200, 0); // 2.5 of 10
								break;
							}
						}
						entity.setPosition(curPos);
					}
						
					curAction = BossActions.NONE;
					mergeDelay = -1;
					cycle = 0;
					form = -1;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.9)) && cycle == 2){
					underground = true;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.66)) && cycle == 1){
					Rectangle rect = new Rectangle(60, 20);
					rect.setFill(Color.RED);
					entity.setGraphics(rect);
					entity.setCollidable(false);
					cycle = 2;
					entity.setPosition(entity.getPosition().add(0, 20));
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.33)) && cycle == 0){
					Rectangle rect = new Rectangle(60, 40);
					rect.setFill(Color.RED);
					entity.setGraphics(rect);
					cycle = 1;
					entity.setPosition(entity.getPosition().add(0, 20));
				}

				break;
			}
			case UATK:
			case BLOW:{
				if(!attacking){
					entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
					attacking = true;
				}
				break;
			}
		}
		
		entity.setVisible(false);
	}
	
	public boolean isUnderground(){
		return underground;
	}
	
	public int getForm(){
		return form;
	}
	
	public void setAttackComplete(boolean b){
		if(b){
			attacking = false;
			curAction = BossActions.NONE;
		}
	}
	
	public int getVelocity() {
		return vel;
	}
}

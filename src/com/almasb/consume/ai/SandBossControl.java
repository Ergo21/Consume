package com.almasb.consume.ai;

import java.util.Random;

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

	private Entity target;
	private Random ran;
	private enum BossActions {
		NONE, MERGE, BLOW
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

	public SandBossControl(Entity target) {
		this.target = target;
		ran = new Random();
		curAction = BossActions.MERGE;
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
		
		switch(curAction){
			case NONE:{
				if(chooseDelay == -1){
					chooseDelay = now;
				}
				
				if(now - chooseDelay >= TimerManager.toNanos(Duration.seconds(1))){
					if(ran.nextBoolean()){
						curAction = BossActions.MERGE;
					}
					else{
						curAction = BossActions.BLOW;
					}
					chooseDelay = -1;
				}
				break;
			}
			case MERGE:{
				if(mergeDelay == -1){
					mergeDelay = now;
				}
				
				if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(3)) && cycle == 5){
					Rectangle rect = new Rectangle(30, 30);
					rect.setFill(Color.RED);
					entity.setPosition(entity.getPosition().subtract(0, 10));
					entity.setGraphics(rect);
					curAction = BossActions.NONE;
					mergeDelay = -1;
					cycle = 0;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(2.66)) && cycle == 4){
					Rectangle rect = new Rectangle(30, 20);
					rect.setFill(Color.RED);
					entity.setPosition(entity.getPosition().subtract(0, 10));
					entity.setGraphics(rect);
					entity.setCollidable(true);
					cycle = 5;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(2.33)) && cycle == 3){
					entity.setProperty("facingRight", target.getPosition().getX() > entity.getPosition().getX());
					entity.setVisible(true);
					if(start){
						start = false;
					}
					cycle = 4;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(1)) && cycle == 2){
					entity.setVisible(false);
					cycle = 3;
					if(!start){
						switch(ran.nextInt(3)){
							case 0:{
								curPos = startPos; //7.5 of 10
								System.out.println(1);
								break;
							}
							case 1:{
								curPos = startPos.subtract(100, 0); // 5 of 10
								System.out.println(2);
								break;
							}
							case 2:{
								curPos = startPos.subtract(200, 0); // 2.5 of 10
								System.out.println(3);
								break;
							}
						}
						entity.setPosition(curPos);
					}
						
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.66)) && cycle == 1){
					Rectangle rect = new Rectangle(30, 10);
					rect.setFill(Color.RED);
					entity.setGraphics(rect);
					entity.setCollidable(false);
					cycle = 2;
				}
				else if(now - mergeDelay >= TimerManager.toNanos(Duration.seconds(0.33)) && cycle == 0){
					Rectangle rect = new Rectangle(30, 20);
					rect.setFill(Color.RED);
					entity.setGraphics(rect);
					cycle = 1;
				}
				break;
			}
			case BLOW:{
				if(!attacking){
					entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
					attacking = true;
				}
				break;
			}
		}
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

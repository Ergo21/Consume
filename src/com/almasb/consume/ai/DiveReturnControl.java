package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.time.TimerManager;

public class DiveReturnControl extends AbstractControl {

	private Entity target;
	private int vel = 0;
	private double dive = 0;
	private double originalY;
	private long startWait = 0;
	
	private int state = 0; //0 = Hover, 1 = Drop, 2 = Rise 
	private boolean onRight = true;

	public DiveReturnControl(Entity target, double oY) {
		this.target = target;
		originalY = oY;
	}

	@Override	
	public void onUpdate(Entity entity, long now) {
		double diveHeight = originalY + entity.getHeight() + Config.BLOCK_SIZE*2;
		if(diveHeight + Config.BLOCK_SIZE <  target.getPosition().getY() + target.getHeight()/2){
			//Move down
			originalY += Config.BLOCK_SIZE;
		}
		else if(diveHeight >  target.getPosition().getY() + target.getHeight()/2){
			//Move up
			originalY -= Config.BLOCK_SIZE;
		}
		
		if(state == 0){	
			if(startWait == 0){
				startWait = now;
			}
			if(now - startWait >= TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY)){
				dive = 0;
				state = 1;
				startWait = 0;
				onRight = !onRight;
			}
		}
		else if(state == 1){
			dive += Config.ENEMY_DIVEBOMB_ACC;

			if (target.getTranslateY() <= entity.getTranslateY()) {
				state = 2;
			}
					
			if(Math.abs(dive) > Speed.PLAYER_MOVE){
				dive = Speed.PLAYER_MOVE * (dive < 0 ? -1: 1);
			}
		}
		else{
			dive -= Config.ENEMY_DIVEBOMB_ACC;
			
			if(Math.abs(dive) > Speed.PLAYER_MOVE){
				dive = Speed.PLAYER_MOVE * (dive < 0 ? -1: 1);
			}
			
			if(entity.getTranslateY() <= originalY){
				dive = 0;
				state = 0;
			}
		}

		if(onRight){
			if(entity.getTranslateX() - target.getTranslateX() <= 200){
				vel = (int) accelerate(vel, (Speed.ENEMY_PATROL + 1));
			}
			else if(entity.getTranslateX() - target.getTranslateX() >= 250){
				vel = (int) accelerate(vel, -(Speed.ENEMY_PATROL + 1));
			}
			else{
				vel = (int) accelerate(vel, 0);
			}
		}
		else{
			if(entity.getTranslateX() - target.getTranslateX() >= -200){
				vel = (int) accelerate(vel, -(Speed.ENEMY_PATROL + 1));
			}
			else if(entity.getTranslateX() - target.getTranslateX() <= -250){
				vel = (int) accelerate(vel, (Speed.ENEMY_PATROL + 1));
			}
			else{
				vel = (int) accelerate(vel, 0);
			}
		}
		
		
		entity.setTranslateX(entity.getTranslateX() + vel);
		entity.setTranslateY(entity.getTranslateY() + dive);
	}
	
	private double accelerate(int ori, int goal){
		if(Math.abs(goal - ori) <= 1){
			return goal;
		}
		
		return ori + (goal > ori ? 1 : -1);
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
	}

	/*private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE;
	}*/

	public int getVelocity() {
		return vel;
	}

}
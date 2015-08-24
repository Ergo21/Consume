package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;

public class SimpleJumpControl extends AbstractControl{
	
	private Entity target;
    private int vel = 0;
    private int jump;
    private long lastJumped = -1;		//Stores when the enemy first sees the player

    public SimpleJumpControl(Entity tar, int jum) {
    	target = tar;
    	jump = jum;
    }

	@Override
	public void onUpdate(Entity entity, long now) {
		if(entity.getControl(PhysicsControl.class).getJump() != jump){
			entity.getControl(PhysicsControl.class).setJump(jump);
		}
		if(lastJumped == -1){
			lastJumped = now;
		}
		
		if(now - lastJumped > Config.ENEMY_JUMP_DELAY){
			if(entity.getPosition().getX() > target.getPosition().getX()){
				entity.getControl(PhysicsControl.class).moveX(-Speed.ENEMY_PATROL);
			}
			else{
				entity.getControl(PhysicsControl.class).moveX(Speed.ENEMY_PATROL);
			}
			entity.getControl(PhysicsControl.class).jump();
			lastJumped = now;
		}
		else if(!(boolean)entity.getProperty("jumping")){
			entity.getControl(PhysicsControl.class).moveX(0);
		}
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
		
	}
	
	public int getVelocity() {
        return vel;
    }
	
}
package com.almasb.consume.ai;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class SpearProjectileControl extends AbstractControl {
	private boolean facingRight;
    private Entity player;

    public SpearProjectileControl(boolean fR, Entity pl) {
        facingRight = fR;
    	player = pl;
    }

    @Override
    public void onUpdate(Entity entity, long now) {    	
        if (Math.abs(entity.getTranslateX() - player.getTranslateX()) >= 350) {
            entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
        }
    }

	@Override
	protected void initEntity(Entity entity) {
		PhysicsControl pc = entity.getControl(PhysicsControl.class);
		pc.moveX(facingRight ? Speed.PROJECTILE : -Speed.PROJECTILE);
		pc.moveY(-Speed.PROJECTILE);
	}
}

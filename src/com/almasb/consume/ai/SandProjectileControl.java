package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class SandProjectileControl extends AbstractControl {

    private boolean facingRight;
    private Entity player;
    private boolean diagonal;
    private long created;

    public SandProjectileControl(Entity player, boolean diag) {
        this.facingRight = player.getProperty("facingRight");
        this.player = player;
        diagonal = diag;
        created = 0;
    }

    @Override
    protected void initEntity(Entity entity) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onUpdate(Entity entity, long now) {
        PhysicsControl control = entity.getControl(PhysicsControl.class);
        if(created == 0){
        	created = now;
        }
        if(now - created > Config.SAND_DELAY){
        	if(diagonal){
            	control.moveX(facingRight ? (Speed.PLAYER_MOVE + 1): -(Speed.PLAYER_MOVE + 1));
            	control.moveY(-Speed.PROJECTILE/4);
            }
            else{
            	control.moveX(facingRight ? (Speed.PLAYER_MOVE + 1): -(Speed.PLAYER_MOVE + 1));
            }
        }
        
        if (Math.abs(entity.getTranslateX() - player.getTranslateX()) >= 350 || now - created > Config.SAND_DECAY) {
            entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
        }
    }
}

package com.almasb.consume.ai;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class ProjectileControl extends AbstractControl {

    private boolean facingRight;

    public ProjectileControl(boolean facingRight) {
        this.facingRight = facingRight;
    }

    @Override
    protected void initEntity(Entity entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdate(Entity entity, long now) {
        entity.translate(facingRight ? Speed.PROJECTILE : -Speed.PROJECTILE, 0);

        if (entity.getTranslateX() >= 5000 || entity.getTranslateX() < -5000) {
            entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
        }
    }
}

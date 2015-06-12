package com.almasb.consume.ai;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.ConsumeApp.Physics;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;

public class SeekControl implements Control {

    private Entity target;

    public SeekControl(Entity target) {
        this.target = target;
    }

    @Override
    public void onUpdate(Entity entity, long now) {
        if (target.getPosition().distance(entity.getPosition()) <= 200) {
            if (Math.abs(target.getTranslateY() - entity.getTranslateY()) <= 10) {
                boolean right = target.getTranslateX() - entity.getTranslateX() > 0;

                Physics physics = entity.getProperty("physics");
                boolean canMove = physics.moveX(entity, right ? Speed.ENEMY_PATROL : -Speed.ENEMY_PATROL);
            }
        }
    }
}

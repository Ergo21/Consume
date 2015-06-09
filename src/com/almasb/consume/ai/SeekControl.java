package com.almasb.consume.ai;

import javafx.geometry.Point2D;

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


                Point2D velocity = entity.getProperty("velocity");
                velocity = velocity.add(0, 1);
                if (velocity.getY() > 10)
                    velocity = new Point2D(velocity.getX(), 10);

                entity.setProperty("velocity", velocity);

                physics.moveY(entity, (int)velocity.getY());
            }
        }
    }
}

package com.almasb.consume.ai;

import javafx.geometry.Point2D;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.ConsumeApp.Physics;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;

public class ChargeControl implements Control {

    private Entity target;
    private int vel = 0;

    public ChargeControl(Entity target) {
        this.target = target;
    }

    @Override
    public void onUpdate(Entity entity, long now) {
        if (target.getPosition().distance(entity.getPosition()) <= 600
                && Math.abs(target.getTranslateY() - entity.getTranslateY()) <= 10) {

            boolean right = target.getTranslateX() - entity.getTranslateX() > 0;

            vel += right ? Speed.ENEMY_SEEK_ACCEL : -Speed.ENEMY_SEEK_ACCEL;

            if (Math.abs(vel) >= Speed.ENEMY_SEEK_MAX)
                vel = (int)Math.signum(vel) * Speed.ENEMY_SEEK_MAX;

            Physics physics = entity.getProperty("physics");
            boolean canMove = physics.moveX(entity, vel);

            if (!canMove)
                vel = 0;

            Point2D velocity = entity.getProperty("velocity");
            velocity = velocity.add(0, Speed.GRAVITY_ACCEL);
            if (velocity.getY() > Speed.GRAVITY_MAX)
                velocity = new Point2D(velocity.getX(), Speed.GRAVITY_MAX);

            entity.setProperty("velocity", velocity);

            physics.moveY(entity, (int)velocity.getY());
        }
        else {
            if (vel == 0)
                return;

            if (vel > 0)
                vel -= Speed.ENEMY_SEEK_DECEL;
            else
                vel += Speed.ENEMY_SEEK_DECEL;

            Physics physics = entity.getProperty("physics");
            boolean canMove = physics.moveX(entity, vel);

            if (!canMove)
                vel = 0;
        }

    }
}

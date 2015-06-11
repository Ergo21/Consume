package com.almasb.consume.ai;

import javafx.geometry.Point2D;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.ConsumeApp.Physics;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class ChargeControl extends AbstractControl {

    private Entity target;
    private int vel = 0;
    private long lastTimeSaw = -1;

    public ChargeControl(Entity target) {
        this.target = target;
    }

    @Override
    protected void initEntity(Entity entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdate(Entity entity, long now) {
        if (isTargetInRange()) {
            if (lastTimeSaw == -1) {
                lastTimeSaw = now;
                entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_SAW_PLAYER));
            }
        }
        else {
            lastTimeSaw = -1;
        }

        if (lastTimeSaw != -1 && now - lastTimeSaw >= Config.ENEMY_CHARGE_DELAY) {
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

    private boolean isTargetInRange() {
        return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_CHARGE_RANGE
                && Math.abs(target.getTranslateY() - entity.getTranslateY()) <= 10;
    }
}

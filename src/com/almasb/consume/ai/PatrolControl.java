package com.almasb.consume.ai;

import javafx.geometry.Point2D;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.ConsumeApp.Physics;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;

public class PatrolControl implements Control {

    private boolean movingRight = true;
    private Point2D patrolPoint;

    public PatrolControl(Point2D patrolPoint) {
        this.patrolPoint = patrolPoint;
    }

    @Override
    public void onUpdate(Entity entity, long now) {

        Physics physics = entity.getProperty("physics");
        boolean canMove = physics.moveX(entity, movingRight ? Speed.ENEMY_PATROL : -Speed.ENEMY_PATROL);


        Point2D velocity = entity.getProperty("velocity");
        velocity = velocity.add(0, Speed.GRAVITY_ACCEL);
        if (velocity.getY() > Speed.GRAVITY_MAX)
            velocity = new Point2D(velocity.getX(), Speed.GRAVITY_MAX);

        entity.setProperty("velocity", velocity);

        physics.moveY(entity, (int)velocity.getY());

        if (!canMove || patrolPoint.distance(entity.getPosition()) >= Config.PATROL_RADIUS) {
            movingRight = !movingRight;
        }
    }
}

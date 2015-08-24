package com.almasb.consume.ai;

import javafx.geometry.Point2D;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.Physics;
import com.almasb.consume.Types.Property;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;

/**
 * Allows moving an entity with physics collision
 * rules, including gravity effect
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 * @version 1.0
 *
 */
public class PhysicsControl extends AbstractControl {

    private Physics physics;
    private Point2D velocity = new Point2D(0, 0);
    private int jumpStr;

    public PhysicsControl(Physics physics) {
        this.physics = physics;
        jumpStr = Speed.PLAYER_JUMP;
    }

    @Override
    protected void initEntity(Entity entity) {
        entity.setProperty("jumping", false);
        entity.setProperty(Property.ENABLE_GRAVITY, true);
    }

    @Override
    public void onUpdate(Entity entity, long now) {
    	physics.moveX(entity, (int)velocity.getX());
        if (entity.<Boolean>getProperty(Property.ENABLE_GRAVITY)) {
            velocity = velocity.add(0, Speed.GRAVITY_ACCEL);
            if (velocity.getY() > Speed.GRAVITY_MAX)
                velocity = new Point2D(velocity.getX(), Speed.GRAVITY_MAX);
            
        }
        physics.moveY(entity, (int)velocity.getY());
        
    }

    public void moveX(int value) {
        velocity = new Point2D(value, velocity.getY());
    }

    public void moveY(int value) {
    	velocity = new Point2D(velocity.getX(), value);
    }

    public void jump() {
        if (entity.<Boolean>getProperty("jumping"))
            return;

        entity.setProperty("jumping", true);
        velocity = new Point2D(velocity.getX(), -jumpStr);
    }
    
    public void setJump(int jump){
    	jumpStr = jump;
    }
    
    public int getJump(){
    	return jumpStr;
    }
    
    public void climb(int clSpd) {
    	if(entity.getProperty("climbing") != null && entity.<Boolean>getProperty("climbing")){
    		physics.moveY(entity, clSpd);
    	}
    }

    public Point2D getVelocity() {
        return velocity;
    }
}

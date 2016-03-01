package com.almasb.consume.ai;

import javafx.geometry.Point2D;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.Physics;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;

public class PatrolControl implements Control {

	private boolean movingRight = true;
	private Point2D patrolPoint;

	public PatrolControl(Point2D patrolPoint) {
		this.patrolPoint = patrolPoint;
	}

	private int frames = 10;
	@Override
	public void onUpdate(Entity entity, long now){
		frames++;
		if(frames >= 5){
			actualUpdate(entity, now);
			frames = 0;
		}
	}
	
	public void actualUpdate(Entity entity, long now) {
		Physics physics = entity.getProperty("physics");
		boolean canMove = physics.moveX(entity, movingRight ? Speed.ENEMY_PATROL : -Speed.ENEMY_PATROL);

		if (!canMove || patrolPoint.distance(entity.getPosition()) >= Config.PATROL_RADIUS) {
			movingRight = !movingRight;
		}
	}
}

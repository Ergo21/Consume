package com.almasb.consume.ai;

import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Point2D;

public class CameraControl extends AbstractControl {

	private Point2D target;

	public CameraControl(Point2D tarPo) {
		this.target = tarPo;
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		if(target.distance(entity.getPosition()) < 2){
			entity.setPosition(target);
		}
		
		if(target.getX() > entity.getPosition().getX()){
			entity.setPosition(entity.getPosition().add(2,0));
		}
		else if(target.getX() < entity.getPosition().getX()){
			entity.setPosition(entity.getPosition().add(-2,0));
		}
		
		if(target.getY() > entity.getPosition().getY()){
			entity.setPosition(entity.getPosition().add(0,2));
		}
		else if(target.getY() < entity.getPosition().getY()){
			entity.setPosition(entity.getPosition().add(0,-2));
		}
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
	}

}
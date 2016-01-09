package com.almasb.consume.ai;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

import javafx.geometry.Rectangle2D;

public class SpearProjectileControl extends AbstractControl {
	private boolean facingRight;
	private Entity player;
	private boolean customVelocity;
	private int moveX;
	private int moveY;
	private Texture curTex;
	private int curFra = -1;

	public SpearProjectileControl(Entity pl) {
		facingRight = pl.getProperty("facingRight");
		player = pl;
		customVelocity = false;
	}
	
	public SpearProjectileControl(Entity pl, int mX, int mY) {
		facingRight = pl.getProperty("facingRight");
		player = pl;
		moveX = mX;
		moveY = mY;
		customVelocity = true;
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		PhysicsControl pc = entity.getControl(PhysicsControl.class);
		if(pc != null && curTex != null){
			if(pc.getVelocity().getY() < -1 && curFra != 0){
				Texture t = curTex.subTexture(new Rectangle2D(0,0,300,200));
				t.setPreserveRatio(true);
				t.setFitWidth(30);
				if(!facingRight){
					t.setScaleX(t.getScaleX()*-1);
				}
				entity.setGraphics(t);
				curFra = 0;
			}
			else if(pc.getVelocity().getY() >= -1 && pc.getVelocity().getY() < 1 && curFra != 1){
				Texture t = curTex.subTexture(new Rectangle2D(300,0,300,200));
				t.setPreserveRatio(true);
				t.setFitWidth(30);
				if(!facingRight){
					t.setScaleX(t.getScaleX()*-1);
				}
				entity.setGraphics(t);
				curFra = 1;
			}
			else if(pc.getVelocity().getY() >= 1 && curFra != 2){
				Texture t = curTex.subTexture(new Rectangle2D(600,0,300,200));
				t.setPreserveRatio(true);
				t.setFitWidth(30);
				if(!facingRight){
					t.setScaleX(t.getScaleX()*-1);
				}
				entity.setGraphics(t);
				curFra = 2;
			}
		}
		
		if (Math.abs(entity.getTranslateX() - player.getTranslateX()) >= 350) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}

	@Override
	protected void initEntity(Entity entity) {
		PhysicsControl pc = entity.getControl(PhysicsControl.class);
		if(customVelocity){
			pc.moveX(facingRight ? moveX : -moveX);
			pc.moveY(moveY);
		}
		else{
			pc.moveX(facingRight ? Speed.PROJECTILE : -Speed.PROJECTILE);
			pc.moveY(-Speed.PROJECTILE);
		}
		
	}

	public void addTexture(Texture t) {
		curTex = t;
	}
}

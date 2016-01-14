package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

import javafx.geometry.Point2D;

public class SandProjectileControl extends AbstractControl {

	private boolean facingRight;
	private Entity player;
	private boolean diagonal;
	private long created;
	private boolean positioned = false;

	public SandProjectileControl(Entity player, boolean diag) {
		this.player = player;
		diagonal = diag;
		created = 0;
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		PhysicsControl control = entity.getControl(PhysicsControl.class);
		if (created == 0) {
			created = now;
		}
		if (now - created > TimerManager.toNanos(Config.SAND_DELAY)) {
			if(!positioned){
				if(player == null){
					entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
					positioned = true;
				}
				else{
					if(player.getProperty("facingRight") != null){
						this.facingRight = player.getProperty("facingRight");
					}
					else{
						facingRight = false;
					}
					
					Point2D p = player.getPosition();
					if (facingRight) {
						p = p.add(player.getWidth(), 0);
					} else {
						p = p.add(-entity.getWidth(), 0);
					}
					entity.setPosition(p);
					positioned = true;
				}
			}
			if (diagonal) {
				control.moveX(facingRight ? (Speed.PLAYER_MOVE + 1) : -(Speed.PLAYER_MOVE + 1));
				control.moveY(-Speed.PROJECTILE / 4);
			} else {
				control.moveX(facingRight ? (Speed.PLAYER_MOVE + 1) : -(Speed.PLAYER_MOVE + 1));
			}
		}

		if (Math.abs(entity.getTranslateX() - player.getTranslateX()) >= 350 || now - created > TimerManager.toNanos(Config.SAND_DECAY)) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}
}

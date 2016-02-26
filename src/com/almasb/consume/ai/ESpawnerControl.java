package com.almasb.consume.ai;

import java.util.ArrayList;
import java.util.function.Function;

import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

import javafx.geometry.Point2D;
import javafx.util.Duration;

public class ESpawnerControl extends AbstractControl {

	private ConsumeApp consApp;
	private Function<Point2D, Entity> spawnMethod;
	private ArrayList<Entity> enemiesSpawned;
	private int maxEnemies;
	private long countDown;
	
	public ESpawnerControl(ConsumeApp cApp, Function<Point2D, Entity> sMethod, int maxEne) {
		consApp = cApp;
		spawnMethod = sMethod;
		maxEnemies = maxEne;
		countDown = 0;
		enemiesSpawned = new ArrayList<Entity>();
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		for(Entity e : enemiesSpawned){
			if(!e.isAlive()){
				consApp.getTimerManager().runOnceAfter(() -> enemiesSpawned.remove(e), Duration.seconds(0.01));
			}
		}
		
		if(enemiesSpawned.size() < maxEnemies){
			if(isTargetInRange() && now - countDown > TimerManager.secondsToNanos(1)){
				entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
				countDown = now;
			}
		}
		else{
			countDown = now;
		}
	}
	
	public Entity spawnEnemy(){
		enemiesSpawned.add(spawnMethod.apply(entity.getPosition()));
		return enemiesSpawned.get(enemiesSpawned.size()-1);
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub

	}

	private boolean isTargetInRange() {
		return (consApp.player.getPosition().getX() - entity.getPosition().getX() > 380 || consApp.player.getPosition().getX() - entity.getPosition().getX() < -340) &&
				(consApp.player.getPosition().getY() - entity.getPosition().getY() < 200 || consApp.player.getPosition().getY() - entity.getPosition().getY() > -200);
	}
}
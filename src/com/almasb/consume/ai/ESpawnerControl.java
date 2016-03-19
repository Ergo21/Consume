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
import javafx.util.Pair;

public class ESpawnerControl extends AbstractControl {

	private ConsumeApp consApp;
	private Function<Point2D, Pair<Entity,Entity>> spawnMethod;
	private ArrayList<Pair<Pair<Entity,Entity>, Boolean>> enemies;
	private int maxEnemies;
	private long countDown;
	private boolean generated = false;
	
	public ESpawnerControl(ConsumeApp cApp, Function<Point2D, Pair<Entity,Entity>> sMethod, int maxEne) {
		consApp = cApp;
		spawnMethod = sMethod;
		maxEnemies = maxEne;
		countDown = 0;
		enemies = new ArrayList<Pair<Pair<Entity,Entity>, Boolean>>();
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
		if(!generated){
			while(enemies.size() < maxEnemies){
				enemies.add(generateEnemy());
			}
			generated = true;
		}
		
		for(Pair<Pair<Entity,Entity>, Boolean> p : enemies){
			if(!p.getKey().getKey().isAlive() && p.getValue()){
				consApp.getTimerManager().runOnceAfter(() -> enemies.remove(p), Duration.seconds(0.01));
			}
		}
		
		if(enemies.isEmpty()){
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
		
		int spawned = 0;
		for(Pair<Pair<Entity,Entity>, Boolean> p : enemies){
			if(p.getValue()){
				spawned++;
			}
		}
		
		if(spawned < maxEnemies){
			if(isTargetInRange() && now - countDown > TimerManager.secondsToNanos(1)){
				entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
				countDown = now;
			}
		}
		else{
			countDown = now;
		}
	}
	
	private Pair<Pair<Entity,Entity>, Boolean> generateEnemy(){
		return new Pair<Pair<Entity,Entity>, Boolean>(spawnMethod.apply(entity.getPosition()), false);
	}
	
	public Pair<Entity,Entity> spawnEnemy(){
		for(Pair<Pair<Entity,Entity>, Boolean> p : enemies){
			if(!p.getValue()){
				enemies.add(new Pair<Pair<Entity,Entity>, Boolean>(p.getKey(),true));
				enemies.remove(p);
				return enemies.get(enemies.size()-1).getKey();
			}
		}
		
		return null;
	}

	@Override
	protected void initEntity(Entity entity) {
		
	}

	private boolean isTargetInRange() {
		double difX = consApp.camera.getPosition().getX() - entity.getPosition().getX();
		double difY = consApp.camera.getPosition().getY() - entity.getPosition().getY();
		return(((difX > 380 && difX < 460) || (difX < -340 && difX > -420)) &&
			(difY < 200 && difY > -200));
		
	}
}
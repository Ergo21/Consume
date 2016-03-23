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
import javafx.util.Pair;

public class ESpawnerControl extends AbstractControl {

	private ConsumeApp consApp;
	private Point2D spawnPoint;
	private Function<Point2D, Pair<Entity,Entity>> spawnMethod;
	private int maxEnemies;
	private long countDown;
	private ArrayList<Pair<Entity,Entity>> enemies;
	private boolean generated = false;
	
	public ESpawnerControl(ConsumeApp cApp, Point2D spPoi, Function<Point2D, Pair<Entity,Entity>> sMethod, int maxEne) {
		consApp = cApp;
		spawnPoint = spPoi;
		spawnMethod = sMethod;
		maxEnemies = maxEne;
		countDown = 0;
		enemies = new ArrayList<>();
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
		if(enemies.size() == 0 && !generated){
			generateEnemies();
			generated = true;
		}
		else if(enemies.size() == 0){
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
		else if(isTargetInRange() && now - countDown > TimerManager.secondsToNanos(1)){
			entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
			countDown = now;
		}
	}
	
	private void generateEnemies(){
		for(int i = 0; i <maxEnemies; i++){
			enemies.add(spawnMethod.apply(spawnPoint));
		}
	}
	
	public Pair<Entity,Entity> spawnEnemy(){
		return enemies.remove(0);
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
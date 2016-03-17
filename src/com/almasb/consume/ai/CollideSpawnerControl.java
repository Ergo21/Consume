package com.almasb.consume.ai;

import java.util.ArrayList;
import java.util.function.Function;

import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class CollideSpawnerControl extends AbstractControl {

	private Function<Point2D, Pair<Entity,Entity>> spawnMethod;
	private int numEnemies;
	private Point2D[] spawnPoints;
	private ArrayList<Pair<Entity,Entity>> enemies;
	
	public CollideSpawnerControl(Function<Point2D, Pair<Entity,Entity>> sMethod, int numEne, Point2D... s) {
		spawnMethod = sMethod;
		numEnemies = numEne;
		spawnPoints = s;
		enemies = new ArrayList<Pair<Entity,Entity>>();
		spawnEnemy();
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
	
	public void actualUpdate(Entity entity, long now){
		for(Pair<Entity,Entity> enemy : enemies){
			if(!enemy.getKey().isAlive()){
				enemies.remove(enemy);
			}
		}
		
		if(enemies.size() < numEnemies){
			spawnEnemy();
		}
	}
	
	public ArrayList<Pair<Entity,Entity>> getEnemies(){
		return enemies;
	}
	
	private ArrayList<Pair<Entity,Entity>> spawnEnemy(){
		while(enemies.size() < numEnemies){
			if(spawnPoints.length == 0){
				enemies.add(spawnMethod.apply(entity.getPosition()));
			}
			for(Point2D sp : spawnPoints){
				enemies.add(spawnMethod.apply(sp));
			}
		}
		
		return enemies;
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub

	}
}
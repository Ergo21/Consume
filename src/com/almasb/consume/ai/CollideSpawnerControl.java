package com.almasb.consume.ai;

import java.util.ArrayList;
import java.util.function.Function;

import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Point2D;

public class CollideSpawnerControl extends AbstractControl {

	private Function<Point2D, Entity> spawnMethod;
	private int numEnemies;
	private Point2D[] spawnPoints;
	
	public CollideSpawnerControl(Function<Point2D, Entity> sMethod, int numEne, Point2D... s) {
		spawnMethod = sMethod;
		numEnemies = numEne;
		spawnPoints = s;
	}

	@Override
	public void onUpdate(Entity entity, long now){
		
	}
	
	public void actualUpdate(Entity entity, long now) {
		
	}
	
	public ArrayList<Entity> spawnEnemy(){
		ArrayList<Entity> enemies = new ArrayList<Entity>();
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
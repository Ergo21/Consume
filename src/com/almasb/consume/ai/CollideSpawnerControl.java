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
	private boolean generated = false;
	
	public CollideSpawnerControl(Function<Point2D, Pair<Entity,Entity>> sMethod, int numEne, Point2D... s) {
		spawnMethod = sMethod;
		numEnemies = numEne;
		spawnPoints = s;
		enemies = new ArrayList<>();
	}

	@Override
	public void onUpdate(Entity entity, long now){
		if(enemies.size() == 0 && !generated){
			generateEnemies();
			generated = true;
		}
	}
	
	private void generateEnemies(){
		while(enemies.size() < numEnemies){
			if(spawnPoints.length == 0){
				enemies.add(spawnMethod.apply(new Point2D(0,0)));
			}
			for(Point2D sp : spawnPoints){
				enemies.add(spawnMethod.apply(sp));
			}
		}
	}
	
	public ArrayList<Pair<Entity,Entity>> spawnEnemies(){
		ArrayList<Pair<Entity,Entity>> tEne = new ArrayList<>();
		tEne.addAll(enemies);
		enemies.clear();
		return tEne;
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub

	}
	
	
}
package com.almasb.consume;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.almasb.consume.Types.Type;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class Physics {

	private ConsumeApp app;
	private List<Entity> platforms;
	private int curLev = -1;
	private HashMap<Integer, HashMap<Integer, Entity>> grid;
	
	public Physics(ConsumeApp app) {
		this.app = app;
		platforms = new ArrayList<>();
		grid = new HashMap<>();
	}
	
	private void updatePlatforms(){
		List<Entity> nPla = app.getSceneManager().getEntities(Type.PLATFORM);
		if(nPla == null || nPla.isEmpty()){
			return;
		}
		
		if(platforms.size() != nPla.size()){
			platforms = nPla;
			curLev = app.playerData.getCurrentLevel();
			gridPlatforms();
			return;
		}
		else{
			for(Entity e : nPla){
				if(!platforms.contains(e)){
					platforms = nPla;
					curLev = app.playerData.getCurrentLevel();
					gridPlatforms();
					return;
				}
			}
		}
	}

	private void gridPlatforms(){
		grid.clear();
		
		for(Entity e : platforms){
			int x = (int)e.getPosition().getX()/Config.BLOCK_SIZE;
			int y = (int)e.getPosition().getY()/Config.BLOCK_SIZE;
			if(!grid.containsKey(x)){
				grid.put(x, new HashMap<>());
			}
			grid.get(x).put(y, e);
		}
	}
	
	private ArrayList<Entity> getLocalPlatforms(Entity e){
		ArrayList<Entity> platToCheck = new ArrayList<>();
		int xMin = (int)e.getPosition().getX()/Config.BLOCK_SIZE;
		int xMax = (int)(e.getPosition().getX()+e.getWidth())/Config.BLOCK_SIZE;
		int yMin = (int)e.getPosition().getY()/Config.BLOCK_SIZE;
		int yMax = (int)(e.getPosition().getY()+e.getHeight())/Config.BLOCK_SIZE;
		
		//TopLeft
		if(grid.containsKey(xMin) && grid.get(xMin).containsKey(yMin)){
			platToCheck.add(grid.get(xMin).get(yMin));
		}
		if(grid.containsKey(xMin-1) && grid.get(xMin-1).containsKey(yMin)){
			platToCheck.add(grid.get(xMin-1).get(yMin));
		}
		if(grid.containsKey(xMin) && grid.get(xMin).containsKey(yMin-1)){
			platToCheck.add(grid.get(xMin).get(yMin));
		}
		
		//TopRight
		if(grid.containsKey(xMax) && grid.get(xMax).containsKey(yMin)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax).get(yMin)))){
				platToCheck.add(grid.get(xMax).get(yMin));
			}		
		}
		if(grid.containsKey(xMax+1) && grid.get(xMax+1).containsKey(yMin)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax+1).get(yMin)))){
				platToCheck.add(grid.get(xMax+1).get(yMin));
			}
		}
		if(grid.containsKey(xMax) && grid.get(xMax).containsKey(yMin-1)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax).get(yMin-1)))){
				platToCheck.add(grid.get(xMax).get(yMin-1));
			}	
		}
		
		//BottomLeft
		if(grid.containsKey(xMin) && grid.get(xMin).containsKey(yMax)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMin).get(yMax)))){
				platToCheck.add(grid.get(xMin).get(yMax));
			}		
		}
		if(grid.containsKey(xMin-1) && grid.get(xMin-1).containsKey(yMax)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMin-1).get(yMax)))){
				platToCheck.add(grid.get(xMin-1).get(yMax));
			}		
		}
		if(grid.containsKey(xMin) && grid.get(xMin).containsKey(yMax+1)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMin).get(yMax+1)))){
				platToCheck.add(grid.get(xMin).get(yMax+1));
			}		
		}
		
		//BottomRight
		if(grid.containsKey(xMax) && grid.get(xMax).containsKey(yMax)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax).get(yMax)))){
				platToCheck.add(grid.get(xMax).get(yMax));
			}		
		}
		if(grid.containsKey(xMax+1) && grid.get(xMax+1).containsKey(yMax)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax+1).get(yMax)))){
				platToCheck.add(grid.get(xMax+1).get(yMax));
			}		
		}
		if(grid.containsKey(xMax) && grid.get(xMax).containsKey(yMax+1)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax).get(yMax+1)))){
				platToCheck.add(grid.get(xMax).get(yMax+1));
			}		
		}
		
		if(e == app.player){
			System.out.println("Platforms to check: " + platToCheck.size());
		}
		
		return platToCheck;
	}
	
	/**
	 * Returns true iff entity has moved value units
	 *
	 * @param e
	 * @param value
	 * @return
	 */
	public boolean moveX(Entity e, int value) {
		if(curLev != app.playerData.getCurrentLevel()){
			updatePlatforms();
		}
		
		boolean movingRight = value > 0;
		
		for (int i = 0; i < Math.abs(value); i++) {
			for (Entity platform : getLocalPlatforms(e)) {
				if(e.getBoundsInParent().intersects(platform.getBoundsInParent())){
					if (movingRight) {
						if (e.getTranslateX() + e.getWidth() == platform.getTranslateX()) {
							e.fireFXGLEvent(new FXGLEvent(Event.COLLIDED_PLATFORM, platform));
							e.translate(-1, 0);
							return false;
						}
					} else {
						if (e.getTranslateX() == platform.getTranslateX() + platform.getWidth()) {
							e.fireFXGLEvent(new FXGLEvent(Event.COLLIDED_PLATFORM, platform));
							e.translate(1, 0);
							return false;
						}
					}
				}
			}
			e.setTranslateX(e.getTranslateX() + (movingRight ? 1 : -1));
		}

		return true;
	}

	public void moveY(Entity e, int value) {
		if(curLev != app.playerData.getCurrentLevel()){
			updatePlatforms();
		}
		
		boolean movingDown = value > 0;

		for (int i = 0; i < Math.abs(value); i++) {
			for (Entity platform : getLocalPlatforms(e)) {
				if(e.getBoundsInParent().intersects(platform.getBoundsInParent())){
					if (movingDown) {
						if (e.getTranslateY() + e.getHeight() == platform.getTranslateY()) {
							e.fireFXGLEvent(new FXGLEvent(Event.COLLIDED_PLATFORM, platform));
							e.setTranslateY(e.getTranslateY() - 1);
							e.setProperty("jumping", false);
							return;
						}
					} else {
						if (e.getTranslateY() == platform.getTranslateY() + platform.getHeight()) {
							e.fireFXGLEvent(new FXGLEvent(Event.COLLIDED_PLATFORM, platform));
							return;
						}
					}
				
				}
			
					
			}
			e.setTranslateY(e.getTranslateY() + (movingDown ? 1 : -1));
			e.setProperty("jumping", true);
		}
	}
}

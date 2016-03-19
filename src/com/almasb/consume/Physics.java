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
		
		platforms = nPla;
		gridPlatforms();
		app.getSceneManager().setPlatformsChanged(false);
	}

	private void gridPlatforms(){
		grid.clear();
		
		for(Entity e : platforms){
			int x = (int)e.getPosition().getX()/Config.BLOCK_SIZE;
			int y = (int)e.getPosition().getY()/Config.BLOCK_SIZE;
			int w = (int)e.getWidth()/Config.BLOCK_SIZE;
			int h = (int)e.getHeight()/Config.BLOCK_SIZE;
			
			for(int i = 0; i < w; i++){
				for(int j = 0; j < h; j++){
					if(!grid.containsKey(x+i)){
						grid.put(x+i, new HashMap<>());
					}
					grid.get(x+i).put(y+j, e);
				}
			}
			
		}
	}
	
	private ArrayList<Entity> getLocalPlatforms(Entity e){
		ArrayList<Entity> platToCheck = new ArrayList<>();
		int xMin = (int)e.getPosition().getX()/Config.BLOCK_SIZE;
		int xMax = (int)(e.getPosition().getX()+e.getWidth())/Config.BLOCK_SIZE;
		int yMin = (int)e.getPosition().getY()/Config.BLOCK_SIZE;
		int yMax = (int)(e.getPosition().getY()+e.getHeight())/Config.BLOCK_SIZE;
		
		//TopLeft
		if(grid.containsKey(xMin) && grid.get(xMin).containsKey(yMin) && 
				grid.get(xMin).get(yMin) != null){
			platToCheck.add(grid.get(xMin).get(yMin));
		}
		if(grid.containsKey(xMin-1) && grid.get(xMin-1).containsKey(yMin) &&
				grid.get(xMin-1).get(yMin) != null){
			platToCheck.add(grid.get(xMin-1).get(yMin));
		}
		if(grid.containsKey(xMin) && grid.get(xMin).containsKey(yMin-1) &&
				grid.get(xMin).get(yMin) != null){
			platToCheck.add(grid.get(xMin).get(yMin-1));
		}
		
		//TopRight
		if(grid.containsKey(xMax) && grid.get(xMax).containsKey(yMin)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax).get(yMin))) && 
					grid.get(xMax).get(yMin) != null){
				platToCheck.add(grid.get(xMax).get(yMin));
			}		
		}
		if(grid.containsKey(xMax+1) && grid.get(xMax+1).containsKey(yMin)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax+1).get(yMin))) &&
					grid.get(xMax+1).get(yMin) != null){
				platToCheck.add(grid.get(xMax+1).get(yMin));
			}
		}
		if(grid.containsKey(xMax) && grid.get(xMax).containsKey(yMin-1)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax).get(yMin-1))) &&
					grid.get(xMax).get(yMin-1) != null){
				platToCheck.add(grid.get(xMax).get(yMin-1));
			}	
		}
		
		//BottomLeft
		if(grid.containsKey(xMin) && grid.get(xMin).containsKey(yMax)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMin).get(yMax))) && 
					grid.get(xMin).get(yMax) != null){
				platToCheck.add(grid.get(xMin).get(yMax));
			}		
		}
		if(grid.containsKey(xMin-1) && grid.get(xMin-1).containsKey(yMax)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMin-1).get(yMax))) &&
					grid.get(xMin-1).get(yMax) != null){
				platToCheck.add(grid.get(xMin-1).get(yMax));
			}		
		}
		if(grid.containsKey(xMin) && grid.get(xMin).containsKey(yMax+1)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMin).get(yMax+1))) &&
					grid.get(xMin).get(yMax+1) != null){
				platToCheck.add(grid.get(xMin).get(yMax+1));
			}		
		}
		
		//BottomRight
		if(grid.containsKey(xMax) && grid.get(xMax).containsKey(yMax)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax).get(yMax))) &&
					grid.get(xMax).get(yMax) != null){
				platToCheck.add(grid.get(xMax).get(yMax));
			}		
		}
		if(grid.containsKey(xMax+1) && grid.get(xMax+1).containsKey(yMax)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax+1).get(yMax))) &&
					grid.get(xMax+1).get(yMax) != null){
				platToCheck.add(grid.get(xMax+1).get(yMax));
			}		
		}
		if(grid.containsKey(xMax) && grid.get(xMax).containsKey(yMax+1)){
			if(!platToCheck.contains(platToCheck.add(grid.get(xMax).get(yMax+1))) &&
					grid.get(xMax).get(yMax+1) != null){
				platToCheck.add(grid.get(xMax).get(yMax+1));
			}		
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
		if(app.getSceneManager().getPlatformsChanged()){
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
		if(app.getSceneManager().getPlatformsChanged()){
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

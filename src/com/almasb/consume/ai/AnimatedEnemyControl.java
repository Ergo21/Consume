package com.almasb.consume.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.almasb.consume.Types;
import com.almasb.consume.Types.AnimationActions;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.asset.StaticAnimatedTexture;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;

import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class AnimatedEnemyControl implements Control {
	private HashMap<Types.AnimationActions, Texture> animations = new HashMap<>();
	private Entity enemy;
	private boolean prevVisible;
	private AnimationActions curAnimation = null; 

	public AnimatedEnemyControl(Entity e, HashMap<Types.AnimationActions, Double> hashMap, String sD, Assets as) {
		enemy = e;
		Set<String> kS = as.TextureKeySet();
		
		AnimationActions defAct;
				
		if(kS.contains(sD + "/IDLE_1.png")){
			defAct = AnimationActions.IDLE;
		}
		else{
			defAct = AnimationActions.MOVE;
		}
		
		for(Types.AnimationActions aA : Types.AnimationActions.values()){
			ArrayList<Texture> texs = new ArrayList<>();
			for(int i = 1; i < 5; i++){
				if(kS.contains(sD + "/" + aA + "_" + i + ".png")){
					texs.add(as.getTexture(sD + "/" + aA + "_" + i + ".png"));
				}
				else if(i == 1){
					texs.add(as.getTexture(sD + "/" + defAct + "_" + i + ".png"));
					break;
				}
				else{
					break;
				}
			}
				
			if(texs.size() > 1){
				Texture[] tS = new Texture[texs.size()];
				texs.toArray(tS);
				StaticAnimatedTexture sAT = new StaticAnimatedTexture(enemy.getWidth(), Duration.seconds(hashMap.get(aA)),
						tS);
				animations.put(aA, sAT);
			}
			else{
				Texture t = texs.get(0);
				t.setPreserveRatio(true);
		       	t.setFitHeight(t.getImage().getHeight()/4);
		        		
		       	Translate off = new Translate();
		       	off.setX(-(t.getImage().getWidth()/4 - enemy.getWidth())/2);
		       	t.getTransforms().add(off);
				animations.put(aA, t);
			}
		}
		
	}
	
	private int frames = 10;
	@Override
	public void onUpdate(Entity entity, long now){
		frames++;
		if(frames >= 2){
			actualUpdate(entity, now);
			frames = 0;
		}
	}

	public void actualUpdate(Entity entity, long now) {
		if(enemy != null && enemy.getProperty("beenHit") != null && enemy.<Boolean>getProperty("beenHit")){
			prevVisible = true;
			entity.setVisible(!entity.isVisible());
		}
		else if(enemy != null && enemy.getProperty("beenHit") != null && !enemy.<Boolean>getProperty("beenHit") &&
				!entity.isVisible() && prevVisible){
			entity.setVisible(true);
		}
		Texture t = getCurrentImage();
		if(t != null){
			entity.setGraphics(t);
		}
		
		if(entity != null && enemy != null && enemy.getProperty("facingRight") != null && 
				((enemy.<Boolean> getProperty("facingRight") && entity.getScaleX() < 0) ||
				(!enemy.<Boolean> getProperty("facingRight") && entity.getScaleX() > 0))){
			entity.setScaleX(entity.getScaleX()*-1);
		}
	}
	
	private Texture getCurrentImage() {
		Texture t = null;
		
		PhysicsControl ec = enemy.getControl(PhysicsControl.class);
		if (enemy != null && ec != null) {
			if(enemy.<Boolean> getProperty("jumping") && animations.get(AnimationActions.JUMP) != null){
				if(enemy.<Boolean> getProperty("attacking") && animations.get(AnimationActions.JATK) != null){
					if(curAnimation != AnimationActions.JATK){
						t = animations.get(AnimationActions.JATK);
						if(curAnimation != AnimationActions.JATK && t.getClass() == StaticAnimatedTexture.class){
							((StaticAnimatedTexture) t).getTimeline().stop();
							((StaticAnimatedTexture) t).getTimeline().play();
						}
						
						curAnimation = AnimationActions.JATK;
						t.setPreserveRatio(true);
						t.setTranslateX(0);
						t.setTranslateY(-40);
						t.setFitHeight(75);
					}
				}
				else{
					if(curAnimation != AnimationActions.JUMP){
						t = animations.get(AnimationActions.JUMP);
						if(curAnimation != AnimationActions.JUMP && t.getClass() == StaticAnimatedTexture.class){
							((StaticAnimatedTexture) t).getTimeline().stop();
							((StaticAnimatedTexture) t).getTimeline().play();
						}
						curAnimation = AnimationActions.JUMP;
						t.setPreserveRatio(true);
						t.setTranslateX(0);
						t.setTranslateY(-40);
						t.setFitHeight(75);
					}
				}
			}
			else if (ec.getVelocity().getX() != 0 && animations.get(AnimationActions.MOVE) != null) {
				if(enemy.<Boolean> getProperty("attacking") && animations.get(AnimationActions.MATK) != null){
					if(curAnimation != AnimationActions.MATK){
						t = animations.get(AnimationActions.MATK);
						if(curAnimation != AnimationActions.MATK && t.getClass() == StaticAnimatedTexture.class){
							((StaticAnimatedTexture) t).getTimeline().stop();
							((StaticAnimatedTexture) t).getTimeline().play();
						}
						curAnimation = AnimationActions.MATK;
						t.setPreserveRatio(true);
						t.setTranslateX(0);
						t.setTranslateY(-40);
						t.setFitHeight(75);	
					}
				}
				else{
					if(curAnimation != AnimationActions.MOVE){
						t = animations.get(AnimationActions.MOVE);
						if(curAnimation != AnimationActions.MOVE && t.getClass() == StaticAnimatedTexture.class){
							((StaticAnimatedTexture) t).getTimeline().stop();
							((StaticAnimatedTexture) t).getTimeline().play();
						}
						curAnimation = AnimationActions.MOVE;
						t.setPreserveRatio(true);
						t.setTranslateX(0);
						t.setTranslateY(-40);
						t.setFitHeight(75);	
					}
				}
			} 
			else if(enemy.<Boolean> getProperty("attacking") && animations.get(AnimationActions.ATK) != null){
				if(curAnimation != AnimationActions.ATK){
					t = animations.get(AnimationActions.ATK);
					if(t.getClass() == StaticAnimatedTexture.class){
						((StaticAnimatedTexture) t).getTimeline().stop();
						((StaticAnimatedTexture) t).getTimeline().play();
					}
					curAnimation = AnimationActions.ATK;
					t.setPreserveRatio(true);
					t.setTranslateX(0);
					t.setTranslateY(-40);
					t.setFitHeight(75);	
				}
			}
			else if(animations.get(AnimationActions.IDLE) != null) {
				if(curAnimation != AnimationActions.IDLE){
					t = animations.get(AnimationActions.IDLE);
					if(t.getClass() == StaticAnimatedTexture.class){
						((StaticAnimatedTexture) t).getTimeline().stop();
						((StaticAnimatedTexture) t).getTimeline().play();
					}
					curAnimation = AnimationActions.IDLE;
					t.setPreserveRatio(true);
					t.setTranslateX(0);
					t.setTranslateY(-40);
					t.setFitHeight(75);
				}
			}
		}
		
		return t;
	}
}

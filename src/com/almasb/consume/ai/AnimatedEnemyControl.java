package com.almasb.consume.ai;

import java.util.HashMap;

import com.almasb.consume.Types;
import com.almasb.consume.Types.AnimationActions;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

public class AnimatedEnemyControl implements Control {

	public static class AnimationDetails{
		public final Rectangle2D area;
		public final int frames;
		public final double period;
		public final boolean startAnimation;
		
		public AnimationDetails(Rectangle2D are, int fra, double per, boolean staAnim){
			area = are;
			frames = fra;
			period = per;
			startAnimation = staAnim;
		}
	}
	
	private HashMap<Types.AnimationActions, Texture> animations = new HashMap<>();
	private HashMap<Types.AnimationActions, AnimationDetails> aniDetails;
	private Entity enemy;
	private Texture spriteSheet;
	private boolean prevVisible;
	private AnimationActions curAnimation = null; 

	public AnimatedEnemyControl(Entity e, HashMap<Types.AnimationActions, AnimationDetails> hashMap, Texture sS) {
		enemy = e;
		aniDetails = hashMap;
		spriteSheet = sS;
		
		for(Types.AnimationActions aA : aniDetails.keySet()){
			AnimationDetails val = aniDetails.get(aA);
			if(val != null){
				if(val.startAnimation){
					animations.put(aA, spriteSheet.subTexture(val.area).
							toStaticAnimatedTexture(val.frames, Duration.seconds(val.period)));
				}
				else{
					animations.put(aA, spriteSheet.subTexture(val.area));
				}
			}
		}
		
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		if(entity.getProperty("beenHit") != null && entity.<Boolean>getProperty("beenHit")){
			prevVisible = true;
			entity.setVisible(!entity.isVisible());
		}
		else if(entity.getProperty("beenHit") != null && !entity.<Boolean>getProperty("beenHit") &&
				!entity.isVisible() && prevVisible){
			entity.setVisible(true);
		}
		Texture t = getCurrentImage();
		if(t != null){
			entity.setGraphics(t);
		}
		
		if(entity != null && ((enemy.<Boolean> getProperty("facingRight") && entity.getScaleX() < 0) ||
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
						if(!aniDetails.get(AnimationActions.JATK).startAnimation && curAnimation != AnimationActions.JATK){
							t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.JATK).frames, 
								Duration.seconds(aniDetails.get(AnimationActions.JATK).period));
						}
						curAnimation = AnimationActions.JATK;
						t.setPreserveRatio(true);
						t.setTranslateX(-30);
						t.setTranslateY(-40);
						t.setFitHeight(75);
					}
				}
				else{
					if(curAnimation != AnimationActions.JUMP){
						t = animations.get(AnimationActions.JUMP);
						if(!aniDetails.get(AnimationActions.JUMP).startAnimation && curAnimation != AnimationActions.JUMP){
							t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.JUMP).frames, 
								Duration.seconds(aniDetails.get(AnimationActions.JUMP).period));
						}
						curAnimation = AnimationActions.JUMP;
						t.setPreserveRatio(true);
						t.setTranslateX(-30);
						t.setTranslateY(-40);
						t.setFitHeight(75);
					}
				}
			}
			else if (ec.getVelocity().getX() != 0 && animations.get(AnimationActions.MOVE) != null) {
				if(enemy.<Boolean> getProperty("attacking") && animations.get(AnimationActions.MATK) != null){
					if(curAnimation != AnimationActions.MATK){
						t = animations.get(AnimationActions.MATK);
						if(!aniDetails.get(AnimationActions.MATK).startAnimation && curAnimation != AnimationActions.MATK){
							t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.MATK).frames, 
								Duration.seconds(aniDetails.get(AnimationActions.MATK).period));
						}
						curAnimation = AnimationActions.MATK;
						t.setPreserveRatio(true);
						t.setTranslateX(-30);
						t.setTranslateY(-40);
						t.setFitHeight(75);	
					}
				}
				else{
					if(curAnimation != AnimationActions.MOVE){
						t = animations.get(AnimationActions.MOVE);
						if(!aniDetails.get(AnimationActions.MOVE).startAnimation && curAnimation != AnimationActions.MOVE){
							t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.MOVE).frames, 
								Duration.seconds(aniDetails.get(AnimationActions.MOVE).period));
						}
						curAnimation = AnimationActions.MOVE;
						t.setPreserveRatio(true);
						t.setTranslateX(-30);
						t.setTranslateY(-40);
						t.setFitHeight(75);	
					}
				}
			} 
			else if(enemy.<Boolean> getProperty("attacking") && animations.get(AnimationActions.ATK) != null){
				if(curAnimation != AnimationActions.ATK){
					t = animations.get(AnimationActions.ATK);
					if(!aniDetails.get(AnimationActions.ATK).startAnimation){
						t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.ATK).frames, 
							Duration.seconds(aniDetails.get(AnimationActions.ATK).period));
					}
					curAnimation = AnimationActions.ATK;
					t.setPreserveRatio(true);
					t.setTranslateX(-30);
					t.setTranslateY(-40);
					t.setFitHeight(75);	
				}
			}
			else if(animations.get(AnimationActions.IDLE) != null) {
				if(curAnimation != AnimationActions.IDLE){
					t = animations.get(AnimationActions.IDLE);
					if(!aniDetails.get(AnimationActions.IDLE).startAnimation){
						t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.IDLE).frames, 
							Duration.seconds(aniDetails.get(AnimationActions.IDLE).period));
					}
					curAnimation = AnimationActions.IDLE;
					t.setPreserveRatio(true);
					t.setTranslateX(-30);
					t.setTranslateY(-40);
					t.setFitHeight(75);
				}
			}
		}
		
		return t;
	}
}
